package org.sopt.sopkathon.domain.match.service;

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
import org.sopt.sopkathon.domain.match.repository.MatchSkipRepository;
import org.sopt.sopkathon.domain.user.entity.Gender;
import org.sopt.sopkathon.domain.user.entity.OnboardingProfile;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

/**
 * 같은 상대를 동시에 넘기는 상황을 실제 DB 커밋으로 확인한다.
 *
 * <p>{@link org.sopt.sopkathon.domain.auth.service.AuthServiceConcurrencyTest}와 같은 이유로 트랜잭션 없이
 * 실행하고, H2 인메모리 DB인지 먼저 확인하며, 이 테스트가 만든 데이터만 지운다.
 */
@SpringBootTest
@ActiveProfiles("test")
class MatchSkipConcurrencyTest {

  private static final int THREAD_COUNT = 10;
  private static final String ME_PHONE = "01090000001";
  private static final String TARGET_PHONE = "01090000002";

  @Autowired private MatchService matchService;
  @Autowired private UserRepository userRepository;
  @Autowired private MatchSkipRepository matchSkipRepository;
  @Autowired private DataSource dataSource;

  private User me;
  private User target;

  @BeforeEach
  void setUp() throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      assertThat(connection.getMetaData().getURL())
          .as("H2 인메모리 DB가 아니면 데이터를 쓰지 않고 중단한다")
          .startsWith("jdbc:h2:mem:");
    }
    me = onboardedUser(ME_PHONE, Gender.MALE);
    target = onboardedUser(TARGET_PHONE, Gender.FEMALE);
  }

  @AfterEach
  void tearDown() {
    matchSkipRepository.findAll().stream()
        .filter(skip -> skip.getUser().getId().equals(me.getId()))
        .forEach(matchSkipRepository::delete);
    userRepository.findByPhoneNumber(ME_PHONE).ifPresent(userRepository::delete);
    userRepository.findByPhoneNumber(TARGET_PHONE).ifPresent(userRepository::delete);
  }

  @Test
  void 같은_상대를_동시에_넘기면_성공_또는_무결성_위반만_나오고_기록은_하나다() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<?>> futures = new ArrayList<>();

    try {
      for (int i = 0; i < THREAD_COUNT; i++) {
        futures.add(executor.submit(() -> {
          ready.countDown();
          start.await();
          matchService.skip(me.getId(), target.getId());
          return null;
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
          // 전역 핸들러가 409(GLB-E009)로 바꾸는 예외만 허용한다. 그 외(락 타임아웃 등)는 500이 되므로 실패
          assertThat(e.getCause()).isInstanceOf(DataIntegrityViolationException.class);
        }
      }

      assertThat(succeeded).isGreaterThanOrEqualTo(1);
      assertThat(matchSkipRepository.existsByUserIdAndTargetId(me.getId(), target.getId())).isTrue();
      assertThat(matchSkipRepository.findTargetIdsByUserId(me.getId())).hasSize(1);
    } finally {
      executor.shutdownNow();
    }
  }

  private User onboardedUser(String phoneNumber, Gender gender) {
    User user = User.create(phoneNumber);
    user.completeOnboarding(OnboardingProfile.builder()
        .nickname("동시성")
        .gender(gender)
        .age(30)
        .profileImageUrl("https://example.com/a.jpg")
        .latitude(new BigDecimal("37.550000"))
        .longitude(new BigDecimal("126.920000"))
        .build());
    return userRepository.save(user);
  }
}
