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
 * 여러 스레드가 실제 DB에 동시에 저장하는 상황을 확인한다.
 *
 * <p>테스트 클래스의 {@code @Transactional}은 다른 스레드에 적용되지 않아 롤백되지 않으므로, 이 클래스는 트랜잭션 없이
 * 실행하고 끝난 뒤 데이터를 직접 지운다. 실제 DB에 커밋하는 테스트라 두 가지로 방어한다.
 *
 * <ul>
 *   <li>시작 전에 접속한 DB가 H2 인메모리인지 확인하고, 아니면 아무것도 쓰기 전에 실패시킨다 (환경 변수
 *       {@code SPRING_DATASOURCE_URL} 등은 테스트 프로필 설정보다 우선하므로 다른 DB로 붙을 수 있다)
 *   <li>정리할 때 전체 삭제({@code deleteAll}) 대신 이 테스트가 만든 번호만 지운다
 * </ul>
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
