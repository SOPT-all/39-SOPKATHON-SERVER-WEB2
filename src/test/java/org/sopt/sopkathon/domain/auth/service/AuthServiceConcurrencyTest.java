package org.sopt.sopkathon.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.domain.auth.dto.LoginRequest;
import org.sopt.sopkathon.domain.auth.dto.LoginResponse;
import org.sopt.sopkathon.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 실제 DB에 커밋하는 테스트. 테스트 트랜잭션 롤백이 다른 스레드에는 적용되지 않으므로,
 * 시작 전에 H2 인메모리인지 확인하고 끝나면 이 테스트가 만든 데이터만 지운다.
 */
@SpringBootTest
@ActiveProfiles("test")
class AuthServiceConcurrencyTest {

  private static final int THREAD_COUNT = 10;
  private static final String PHONE_NUMBER = "010-1234-5678";

  @Autowired private AuthService authService;
  @Autowired private UserRepository userRepository;
  @Autowired private DataSource dataSource;

  @BeforeEach
  void checkInMemoryDatabase() throws Exception {
    try (Connection connection = dataSource.getConnection()) {
      assertThat(connection.getMetaData().getURL())
          .as("H2 인메모리 DB가 아니면 데이터를 쓰지 않고 중단한다")
          .startsWith("jdbc:h2:mem:");
    }
  }

  @AfterEach
  void tearDown() {
    userRepository.findByPhoneNumber("01012345678").ifPresent(userRepository::delete);
  }

  @Test
  void 같은_새_번호로_동시에_로그인해도_모두_성공하고_회원은_한_명만_생긴다() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
    CountDownLatch start = new CountDownLatch(1);
    List<Future<LoginResponse>> futures = new ArrayList<>();

    try {
      for (int i = 0; i < THREAD_COUNT; i++) {
        futures.add(executor.submit(() -> {
          ready.countDown();
          start.await();
          return authService.login(new LoginRequest(PHONE_NUMBER));
        }));
      }
      ready.await();
      start.countDown();

      List<Long> userIds = new ArrayList<>();
      for (Future<LoginResponse> future : futures) {
        userIds.add(future.get(10, TimeUnit.SECONDS).userId()); // 하나라도 예외면 여기서 실패
      }

      assertThat(userIds).containsOnly(userIds.getFirst());
      assertThat(userRepository.count()).isEqualTo(1);
    } finally {
      executor.shutdownNow();
    }
  }
}
