package org.sopt.sopkathon.domain.match.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.domain.match.repository.MatchSkipRepository;
import org.sopt.sopkathon.domain.user.entity.Gender;
import org.sopt.sopkathon.domain.user.entity.OnboardingProfile;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MatchSkipControllerTest {

  private static final String SKIP_URL = "/api/v1/matches/{targetId}/skip";

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private MatchSkipRepository matchSkipRepository;

  private User me;
  private User target;

  @BeforeEach
  void setUp() {
    me = onboardedUser("01000000000", "나", Gender.MALE, "37.550000");
    target = onboardedUser("01011111111", "상대", Gender.FEMALE, "37.551000");
  }

  @Test
  void 상대를_넘기면_201과_함께_기록이_저장된다() throws Exception {
    mockMvc
        .perform(post(SKIP_URL, target.getId()).header("X-User-Id", me.getId()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.code").value("GLB-S002"))
        .andExpect(jsonPath("$.data").doesNotExist());

    assertThat(matchSkipRepository.existsByUserIdAndTargetId(me.getId(), target.getId())).isTrue();
  }

  @Test
  void 이미_넘긴_상대를_다시_넘겨도_201이고_기록은_하나다() throws Exception {
    mockMvc
        .perform(post(SKIP_URL, target.getId()).header("X-User-Id", me.getId()))
        .andExpect(status().isCreated());
    mockMvc
        .perform(post(SKIP_URL, target.getId()).header("X-User-Id", me.getId()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value("GLB-S002"));

    assertThat(matchSkipRepository.count()).isEqualTo(1);
  }

  @Test
  void 넘긴_상대는_매칭_상대_조회에서_빠진다() throws Exception {
    User other = onboardedUser("01022222222", "다른상대", Gender.FEMALE, "37.552000");

    mockMvc
        .perform(post(SKIP_URL, target.getId()).header("X-User-Id", me.getId()))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/v1/matches").header("X-User-Id", me.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.users[*].userId").value(contains(other.getId().intValue())));
  }

  @Test
  void 자기_자신을_넘기면_400() throws Exception {
    mockMvc
        .perform(post(SKIP_URL, me.getId()).header("X-User-Id", me.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("MAT-E001"));

    assertThat(matchSkipRepository.count()).isZero();
  }

  @Test
  void 존재하지_않는_상대면_404() throws Exception {
    mockMvc
        .perform(post(SKIP_URL, 999_999).header("X-User-Id", me.getId()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USR-E002"));
  }

  @Test
  void 존재하지_않는_유저가_요청하면_404() throws Exception {
    mockMvc
        .perform(post(SKIP_URL, target.getId()).header("X-User-Id", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USR-E002"));
  }

  @Test
  void 온보딩을_마치지_않은_유저면_400() throws Exception {
    User notOnboarded = userRepository.save(User.create("01055555555"));

    mockMvc
        .perform(post(SKIP_URL, target.getId()).header("X-User-Id", notOnboarded.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("USR-E003"));
  }

  @Test
  void X_User_Id_헤더가_없으면_400() throws Exception {
    mockMvc
        .perform(post(SKIP_URL, target.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }

  @Test
  void targetId가_숫자가_아니면_400() throws Exception {
    mockMvc
        .perform(post(SKIP_URL, "abc").header("X-User-Id", me.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }

  private User onboardedUser(String phoneNumber, String nickname, Gender gender, String latitude) {
    User user = User.create(phoneNumber);
    user.completeOnboarding(OnboardingProfile.builder()
        .nickname(nickname)
        .gender(gender)
        .age(30)
        .profileImageUrl("https://example.com/" + phoneNumber + ".jpg")
        .latitude(new BigDecimal(latitude))
        .longitude(new BigDecimal("126.920000"))
        .build());
    return userRepository.save(user);
  }
}
