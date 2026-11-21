package org.sopt.sopkathon.domain.callrequest.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.domain.callrequest.code.CallRequestErrorCode;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestCreateRequest;
import org.sopt.sopkathon.domain.callrequest.repository.CallRequestRepository;
import org.sopt.sopkathon.domain.user.entity.Gender;
import org.sopt.sopkathon.domain.user.entity.OnboardingProfile;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.repository.UserRepository;
import org.sopt.sopkathon.global.exception.BaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

/**
 * 실제 DB에 커밋하는 테스트. 테스트 트랜잭션 롤백이 다른 스레드에는 적용되지 않으므로,
 * 시작 전에 H2 인메모리인지 확인하고 끝나면 이 테스트가 만든 데이터만 지운다.
 */
@SpringBootTest
@ActiveProfiles("test")
class CallRequestConcurrencyTest {

  private static final int THREAD_COUNT = 10;
  private static final String REQUESTER_PHONE = "01091000001";
  private static final String RECEIVER_PHONE = "01091000002";

  @Autowired private CallRequestService callRequestService;
  @Autowired private CallRequestRepository callRequestRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private DataSource dataSource;

  private User requester;
  private User receiver;

  @BeforeEach
  void setUp() throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      assertThat(connection.getMetaData().getURL())
          .as("H2 인메모리 DB가 아니면 데이터를 쓰지 않고 중단한다")
          .startsWith("jdbc:h2:mem:");
    }
    requester = onboardedUser(REQUESTER_PHONE, Gender.MALE);
    receiver = onboardedUser(RECEIVER_PHONE, Gender.FEMALE);
  }

  @AfterEach
  void tearDown() {
    callRequestRepository.findAll().stream()
        .filter(request -> request.getRequester().getId().equals(requester.getId()))
        .forEach(callRequestRepository::delete);
    userRepository.findByPhoneNumber(REQUESTER_PHONE).ifPresent(userRepository::delete);
    userRepository.findByPhoneNumber(RECEIVER_PHONE).ifPresent(userRepository::delete);
  }

  @Test
  void 같은_요청이_동시에_들어와도_하나만_저장되고_나머지는_409로_응답할_예외만_난다() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<?>> futures = new ArrayList<>();

    try {
      for (int i = 0; i < THREAD_COUNT; i++) {
        futures.add(executor.submit(() -> {
          ready.countDown();
          start.await();
          return callRequestService.send(
              requester.getId(), new CallRequestCreateRequest(receiver.getId(), null));
        }));
      }
      ready.await();
      start.countDown();

      int succeeded = 0;
      for (Future<?> future : futures) {
        try {
          future.get(10, TimeUnit.SECONDS);
          succeeded++;
        } catch (ExecutionException e) {
          // 중복 확인에 걸리면 REQ-E002(409), 동시에 통과하면 유니크 제약 위반(409 GLB-E009). 그 외는 500이므로 실패
          Throwable cause = e.getCause();
          if (cause instanceof BaseException baseException) {
            assertThat(baseException.getErrorCode()).isEqualTo(CallRequestErrorCode.ALREADY_REQUESTED);
          } else {
            assertThat(cause).isInstanceOf(DataIntegrityViolationException.class);
          }
        }
      }

      assertThat(succeeded).isEqualTo(1);
      assertThat(callRequestRepository.findPartnerIds(requester.getId()))
          .containsExactly(receiver.getId());
    } finally {
      executor.shutdownNow();
    }
  }

  private User onboardedUser(String phoneNumber, Gender gender) {
    User user = User.create(phoneNumber);
    user.completeOnboarding(OnboardingProfile.builder()
        .nickname("동시성")
        .gender(gender)
        .age(70)
        .profileImageUrl("https://example.com/a.jpg")
        .latitude(new BigDecimal("37.550000"))
        .longitude(new BigDecimal("126.920000"))
        .build());
    return userRepository.save(user);
  }
}
