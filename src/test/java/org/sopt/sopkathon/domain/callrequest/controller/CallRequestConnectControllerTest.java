package org.sopt.sopkathon.domain.callrequest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequestStatus;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CallRequestConnectControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private CallRequestRepository callRequestRepository;
  @Autowired private EntityManager entityManager;

  private User requester;
  private User receiver;
  private User stranger;
  private CallRequest callRequest;

  @BeforeEach
  void setUp() {
    requester = onboardedUser("01011111111", Gender.MALE);
    receiver = onboardedUser("01022222222", Gender.FEMALE);
    stranger = onboardedUser("01033333333", Gender.FEMALE);
    callRequest = callRequestRepository.save(CallRequest.create(requester, receiver, null));
  }

  @Test
  void 받은_사람이_호출하면_통화_완료로_바뀐다() throws Exception {
    connect(receiver.getId(), callRequest.getId())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("GLB-S001"))
        .andExpect(jsonPath("$.data.requestId").value(callRequest.getId()))
        .andExpect(jsonPath("$.data.status").value("CONNECTED"));

    assertThat(statusInDb()).isEqualTo(CallRequestStatus.CONNECTED);
  }

  @Test
  void 이미_통화_완료한_요청에_다시_호출해도_200() throws Exception {
    connect(receiver.getId(), callRequest.getId()).andExpect(status().isOk());

    connect(receiver.getId(), callRequest.getId())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("CONNECTED"));
  }

  @Test
  void 보낸_사람이_호출하면_403이고_상태는_그대로다() throws Exception {
    connect(requester.getId(), callRequest.getId())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("GLB-E003"));

    assertThat(statusInDb()).isEqualTo(CallRequestStatus.PENDING);
  }

  @Test
  void 관계없는_회원이_호출하면_403() throws Exception {
    connect(stranger.getId(), callRequest.getId())
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("GLB-E003"));

    assertThat(statusInDb()).isEqualTo(CallRequestStatus.PENDING);
  }

  @Test
  void 존재하지_않는_요청이면_404() throws Exception {
    connect(receiver.getId(), 999_999L)
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("REQ-E005"));
  }

  @Test
  void X_User_Id_헤더가_없으면_400() throws Exception {
    mockMvc.perform(patch("/api/v1/call-requests/{id}/connect", callRequest.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }

  @Test
  void requestId가_숫자가_아니면_400() throws Exception {
    mockMvc.perform(patch("/api/v1/call-requests/abc/connect").header("X-User-Id", receiver.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }

  @Test
  void 통화_완료_후에도_서로_매칭_후보에서_빠진다() throws Exception {
    connect(receiver.getId(), callRequest.getId()).andExpect(status().isOk());

    mockMvc.perform(get("/api/v1/matches").header("X-User-Id", receiver.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.users[*].userId")
            .value(not(hasItem(requester.getId().intValue()))));
  }

  private ResultActions connect(Long userId, Long requestId) throws Exception {
    return mockMvc.perform(
        patch("/api/v1/call-requests/{id}/connect", requestId).header("X-User-Id", userId));
  }

  private CallRequestStatus statusInDb() {
    entityManager.flush();
    entityManager.clear();
    return callRequestRepository.findById(callRequest.getId()).orElseThrow().getStatus();
  }

  private User onboardedUser(String phoneNumber, Gender gender) {
    User user = User.create(phoneNumber);
    user.completeOnboarding(OnboardingProfile.builder()
        .nickname("회원")
        .gender(gender)
        .age(65)
        .profileImageUrl("https://example.com/" + phoneNumber + ".jpg")
        .latitude(new BigDecimal("37.550000"))
        .longitude(new BigDecimal("126.920000"))
        .build());
    return userRepository.save(user);
  }
}
