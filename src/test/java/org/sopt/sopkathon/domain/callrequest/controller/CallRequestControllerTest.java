package org.sopt.sopkathon.domain.callrequest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CallRequestControllerTest {

  private static final String URL = "/api/v1/call-requests";

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private CallRequestRepository callRequestRepository;

  private User me;
  private User receiver;

  @BeforeEach
  void setUp() {
    me = onboardedUser("01000000000", Gender.MALE, "37.550000");
    receiver = onboardedUser("01011111111", Gender.FEMALE, "37.551000");
  }

  @Test
  void 요청을_보내면_201과_함께_PENDING으로_저장된다() throws Exception {
    send(me.getId(), "{\"receiverId\": %d, \"message\": \"산책 같이 하실래요?\"}".formatted(receiver.getId()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value("GLB-S002"))
        .andExpect(jsonPath("$.data.requestId").isNumber())
        .andExpect(jsonPath("$.data.status").value("PENDING"));

    CallRequest saved = callRequestRepository.findAll().getFirst();
    assertThat(saved.getRequester().getId()).isEqualTo(me.getId());
    assertThat(saved.getReceiver().getId()).isEqualTo(receiver.getId());
    assertThat(saved.getMessage()).isEqualTo("산책 같이 하실래요?");
  }

  @Test
  void 쪽지를_생략하면_쪽지_없이_저장된다() throws Exception {
    send(me.getId(), "{\"receiverId\": %d}".formatted(receiver.getId()))
        .andExpect(status().isCreated());

    assertThat(callRequestRepository.findAll().getFirst().getMessage()).isNull();
  }

  @Test
  void 쪽지가_공백뿐이면_쪽지_없이_저장된다() throws Exception {
    send(me.getId(), "{\"receiverId\": %d, \"message\": \"   \"}".formatted(receiver.getId()))
        .andExpect(status().isCreated());

    assertThat(callRequestRepository.findAll().getFirst().getMessage()).isNull();
  }

  @Test
  void 쪽지는_앞뒤_공백을_빼고_50자까지_보낼_수_있다() throws Exception {
    String fifty = "가".repeat(50);
    send(me.getId(), "{\"receiverId\": %d, \"message\": \"  %s  \"}".formatted(receiver.getId(), fifty))
        .andExpect(status().isCreated());

    assertThat(callRequestRepository.findAll().getFirst().getMessage()).isEqualTo(fifty);
  }

  @Test
  void 쪽지가_50자를_넘으면_400() throws Exception {
    send(me.getId(), "{\"receiverId\": %d, \"message\": \"%s\"}".formatted(receiver.getId(), "가".repeat(51)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"))
        .andExpect(jsonPath("$.data.message").value("쪽지는 50자 이하여야 합니다."));

    assertThat(callRequestRepository.count()).isZero();
  }

  @Test
  void 요청을_보내면_서로의_매칭_후보에서_빠진다() throws Exception {
    send(me.getId(), "{\"receiverId\": %d}".formatted(receiver.getId()))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/v1/matches").header("X-User-Id", me.getId()))
        .andExpect(jsonPath("$.data.users", hasSize(0)));
    mockMvc
        .perform(get("/api/v1/matches").header("X-User-Id", receiver.getId()))
        .andExpect(jsonPath("$.data.users", hasSize(0)));
  }

  @Test
  void 요청을_보내도_다른_후보는_그대로_보인다() throws Exception {
    User other = onboardedUser("01022222222", Gender.FEMALE, "37.552000");

    send(me.getId(), "{\"receiverId\": %d}".formatted(receiver.getId()))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/v1/matches").header("X-User-Id", me.getId()))
        .andExpect(jsonPath("$.data.users[*].userId").value(contains(other.getId().intValue())));
  }

  @Test
  void 자기_자신에게_요청하면_400() throws Exception {
    send(me.getId(), "{\"receiverId\": %d}".formatted(me.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("REQ-E001"));
  }

  @Test
  void 같은_상대에게_다시_요청하면_409() throws Exception {
    send(me.getId(), "{\"receiverId\": %d}".formatted(receiver.getId()))
        .andExpect(status().isCreated());

    send(me.getId(), "{\"receiverId\": %d}".formatted(receiver.getId()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("REQ-E002"));

    assertThat(callRequestRepository.count()).isEqualTo(1);
  }

  @Test
  void 상대가_이미_나에게_요청했으면_409() throws Exception {
    send(receiver.getId(), "{\"receiverId\": %d}".formatted(me.getId()))
        .andExpect(status().isCreated());

    send(me.getId(), "{\"receiverId\": %d}".formatted(receiver.getId()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("REQ-E002"));
  }

  @Test
  void 없는_상대에게_요청하면_404() throws Exception {
    send(me.getId(), "{\"receiverId\": 999999}")
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USR-E002"));
  }

  @Test
  void 온보딩_전인_상대에게_요청하면_404() throws Exception {
    User notOnboarded = userRepository.save(User.create("01033333333"));

    send(me.getId(), "{\"receiverId\": %d}".formatted(notOnboarded.getId()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USR-E002"));
  }

  @Test
  void 없는_유저가_요청하면_404() throws Exception {
    send(999_999L, "{\"receiverId\": %d}".formatted(receiver.getId()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USR-E002"));
  }

  @Test
  void 온보딩_전인_유저가_요청하면_400() throws Exception {
    User notOnboarded = userRepository.save(User.create("01044444444"));

    send(notOnboarded.getId(), "{\"receiverId\": %d}".formatted(receiver.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("USR-E003"));
  }

  @Test
  void receiverId가_없으면_400() throws Exception {
    send(me.getId(), "{\"message\": \"안녕하세요\"}")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"))
        .andExpect(jsonPath("$.data.receiverId").value("요청을 받을 사람을 지정해주세요."));
  }

  @Test
  void X_User_Id_헤더가_없으면_400() throws Exception {
    mockMvc
        .perform(post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"receiverId\": %d}".formatted(receiver.getId())))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }

  @Test
  void JSON_형식이_깨지면_400() throws Exception {
    send(me.getId(), "{\"receiverId\": ")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }

  private ResultActions send(Long userId, String body) throws Exception {
    return mockMvc.perform(post(URL)
        .header("X-User-Id", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(body));
  }

  private User onboardedUser(String phoneNumber, Gender gender, String latitude) {
    User user = User.create(phoneNumber);
    user.completeOnboarding(OnboardingProfile.builder()
        .nickname("회원" + phoneNumber.substring(7))
        .gender(gender)
        .age(70)
        .profileImageUrl("https://example.com/" + phoneNumber + ".jpg")
        .latitude(new BigDecimal(latitude))
        .longitude(new BigDecimal("126.920000"))
        .build());
    return userRepository.save(user);
  }
}
