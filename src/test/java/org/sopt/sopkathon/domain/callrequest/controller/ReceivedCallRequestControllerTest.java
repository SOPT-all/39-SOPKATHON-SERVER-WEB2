package org.sopt.sopkathon.domain.callrequest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.sopt.sopkathon.domain.callrequest.repository.CallRequestRepository;
import org.sopt.sopkathon.domain.user.entity.Gender;
import org.sopt.sopkathon.domain.user.entity.OnboardingProfile;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReceivedCallRequestControllerTest {

  private static final String URL = "/api/v1/call-requests/received";

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private CallRequestRepository callRequestRepository;
  @Autowired private EntityManager entityManager;
  @Autowired private EntityManagerFactory entityManagerFactory;

  private User me;
  private User first;
  private User second;

  @BeforeEach
  void setUp() {
    me = onboardedUser("01000000000", "나", Gender.FEMALE, "37.550000");
    first = onboardedUser("01011111111", "먼저보낸사람", Gender.MALE, "37.551000");
    second = onboardedUser("01022222222", "나중보낸사람", Gender.MALE, "37.560000");
  }

  @Test
  void 받은_요청을_최신순으로_보낸_사람_정보와_함께_조회한다() throws Exception {
    CallRequest older = callRequestRepository.save(CallRequest.create(first, me, null));
    CallRequest newer = callRequestRepository.save(CallRequest.create(second, me, "산책 같이 하실래요?"));

    getReceived(me.getId(), null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("GLB-S001"))
        .andExpect(jsonPath("$.data.requests[*].requestId")
            .value(contains(newer.getId().intValue(), older.getId().intValue())))
        .andExpect(jsonPath("$.data.requests[0].status").value("PENDING"))
        .andExpect(jsonPath("$.data.requests[0].message").value("산책 같이 하실래요?"))
        .andExpect(jsonPath("$.data.requests[0].createdAt")
            .value(matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}")))
        .andExpect(jsonPath("$.data.requests[0].requester.userId").value(second.getId()))
        .andExpect(jsonPath("$.data.requests[0].requester.nickname").value("나중보낸사람"))
        .andExpect(jsonPath("$.data.requests[0].requester.gender").value("MALE"))
        .andExpect(jsonPath("$.data.requests[0].requester.age").value(70))
        .andExpect(jsonPath("$.data.requests[0].requester.phoneNumber").value("01022222222"))
        .andExpect(jsonPath("$.data.requests[0].requester.distance").value(1200))
        .andExpect(jsonPath("$.data.requests[1].requester.distance").value(200))
        .andExpect(jsonPath("$.data.requests[1]", hasKey("message")))
        .andExpect(jsonPath("$.data.requests[1].message").value(nullValue()));
  }

  @Test
  void called가_false면_통화_전_true면_통화_완료만_조회한다() throws Exception {
    CallRequest pending = callRequestRepository.save(CallRequest.create(first, me, null));
    CallRequest connected = callRequestRepository.save(CallRequest.create(second, me, null));
    connected.connect();

    getReceived(me.getId(), "false")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.requests[*].requestId").value(contains(pending.getId().intValue())));
    getReceived(me.getId(), "true")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.requests[*].requestId").value(contains(connected.getId().intValue())))
        .andExpect(jsonPath("$.data.requests[0].status").value("CONNECTED"));
  }

  @Test
  void 다른_사람이_받은_요청과_내가_보낸_요청은_보이지_않는다() throws Exception {
    User other = onboardedUser("01033333333", "다른사람", Gender.FEMALE, "37.550000");
    callRequestRepository.save(CallRequest.create(first, other, null));
    callRequestRepository.save(CallRequest.create(me, second, null));

    getReceived(me.getId(), null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.requests", hasSize(0)));
  }

  @Test
  void 요청_ID보다_받은_시각이_정렬_기준이다() throws Exception {
    CallRequest smallerId = callRequestRepository.save(CallRequest.create(first, me, null));
    CallRequest biggerId = callRequestRepository.save(CallRequest.create(second, me, null));
    entityManager.flush();
    entityManager
        .createNativeQuery("update call_requests set created_at = created_at + interval '1' hour where id = :id")
        .setParameter("id", smallerId.getId())
        .executeUpdate();
    entityManager.clear();

    getReceived(me.getId(), null)
        .andExpect(jsonPath("$.data.requests[*].requestId")
            .value(contains(smallerId.getId().intValue(), biggerId.getId().intValue())));
  }

  @Test
  void 보낸_사람의_좌표가_없어도_요청은_보이고_거리만_null이다() throws Exception {
    CallRequest request = callRequestRepository.save(CallRequest.create(first, me, null));
    entityManager.flush();
    entityManager
        .createNativeQuery("update users set latitude = null, longitude = null where id = :id")
        .setParameter("id", first.getId())
        .executeUpdate();
    entityManager.clear();

    getReceived(me.getId(), null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.requests[*].requestId").value(contains(request.getId().intValue())))
        .andExpect(jsonPath("$.data.requests[0].requester.phoneNumber").value("01011111111"))
        .andExpect(jsonPath("$.data.requests[0].requester", hasKey("distance")))
        .andExpect(jsonPath("$.data.requests[0].requester.distance").value(nullValue()));
  }

  @Test
  void 받은_요청이_없으면_빈_목록을_200으로_준다() throws Exception {
    getReceived(me.getId(), null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.requests", hasSize(0)));
  }

  @Test
  void 보낸_사람이_여러_명이어도_쿼리는_두_번만_실행된다() throws Exception {
    User third = onboardedUser("01044444444", "세번째", Gender.MALE, "37.552000");
    callRequestRepository.save(CallRequest.create(first, me, null));
    callRequestRepository.save(CallRequest.create(second, me, null));
    callRequestRepository.save(CallRequest.create(third, me, null));
    entityManager.flush();
    entityManager.clear();

    Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    statistics.clear();

    getReceived(me.getId(), null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.requests", hasSize(3)));

    // 나 조회 1번 + 받은 요청(requester fetch join) 1번
    assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
  }

  @Test
  void called가_true_false가_아니면_400() throws Exception {
    getReceived(me.getId(), "abc")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }

  @Test
  void X_User_Id_헤더가_없으면_400() throws Exception {
    mockMvc
        .perform(get(URL))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }

  @Test
  void 존재하지_않는_유저면_404() throws Exception {
    getReceived(999_999L, null)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USR-E002"));
  }

  @Test
  void 온보딩을_마치지_않은_유저면_400() throws Exception {
    User notOnboarded = userRepository.save(User.create("01055555555"));

    getReceived(notOnboarded.getId(), null)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("USR-E003"));
  }

  private ResultActions getReceived(Long userId, String called) throws Exception {
    var request = get(URL).header("X-User-Id", userId);
    if (called != null) {
      request.param("called", called);
    }
    return mockMvc.perform(request);
  }

  private User onboardedUser(String phoneNumber, String nickname, Gender gender, String latitude) {
    User user = User.create(phoneNumber);
    user.completeOnboarding(OnboardingProfile.builder()
        .nickname(nickname)
        .gender(gender)
        .age(70)
        .profileImageUrl("https://example.com/" + phoneNumber + ".jpg")
        .latitude(new BigDecimal(latitude))
        .longitude(new BigDecimal("126.920000"))
        .build());
    return userRepository.save(user);
  }
}
