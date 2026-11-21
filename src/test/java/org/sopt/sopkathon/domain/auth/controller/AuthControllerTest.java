package org.sopt.sopkathon.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
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
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;

  private ResultActions login(String phoneNumber) throws Exception {
    return mockMvc.perform(
        post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"phoneNumber\": \"" + phoneNumber + "\"}"));
  }

  @Test
  void 처음_입력한_번호면_회원을_만들고_온보딩_전으로_응답한다() throws Exception {
    login("01012345678")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userId").isNumber())
        .andExpect(jsonPath("$.data.onboarded").value(false));

    assertThat(userRepository.findByPhoneNumber("01012345678")).isPresent();
  }

  @Test
  void 이미_있는_번호면_같은_회원으로_로그인한다() throws Exception {
    User user = userRepository.save(User.create("01012345678"));

    login("01012345678")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.userId").value(user.getId()));

    assertThat(userRepository.count()).isEqualTo(1);
  }

  @Test
  void 하이픈이_있어도_같은_회원으로_로그인한다() throws Exception {
    User user = userRepository.save(User.create("01012345678"));

    login("010-1234-5678")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.userId").value(user.getId()));

    assertThat(userRepository.count()).isEqualTo(1);
  }

  @Test
  void 휴대폰_번호_형식이_아니면_400을_응답한다() throws Exception {
    login("02-123-4567")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("USR-E001"));

    assertThat(userRepository.count()).isZero();
  }

  @Test
  void 전화번호가_비어있으면_400을_응답한다() throws Exception {
    login(" ")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }
}
