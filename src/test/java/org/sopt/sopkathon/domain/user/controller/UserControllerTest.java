package org.sopt.sopkathon.domain.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.domain.user.entity.Gender;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.repository.UserRepository;
import org.sopt.sopkathon.support.ImageFixtures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import tools.jackson.databind.ObjectMapper;

/** 실제 S3 대신 S3Client를 Mock으로 대체한다. 이미지 형식 검증은 실제 S3ImageService가 수행한다. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

  private static final String URL = "/api/v1/users/me/onboarding";

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private S3Client s3Client;

  private User user;

  @BeforeEach
  void setUp() {
    given(s3Client.utilities())
        .willReturn(S3Utilities.builder().region(Region.AP_NORTHEAST_2).build());
    user = userRepository.save(User.create("01012345678"));
  }

  @Test
  void 온보딩_정보를_저장하고_200으로_응답한다() throws Exception {
    Map<String, Object> body = validBody();
    body.put("nickname", "  정우  ");
    body.put("latitude", new BigDecimal("37.5512345678"));

    onboarding(user.getId(), jpeg(), json(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("GLB-S001"))
        .andExpect(jsonPath("$.data.userId").value(user.getId()))
        .andExpect(jsonPath("$.data.nickname").value("정우"))
        .andExpect(jsonPath("$.data.gender").value("MALE"))
        .andExpect(jsonPath("$.data.age").value(25))
        .andExpect(jsonPath("$.data.profileImageUrl").value(
            org.hamcrest.Matchers.startsWith(
                "https://test-bucket.s3.ap-northeast-2.amazonaws.com/profile/")))
        .andExpect(jsonPath("$.data.latitude").doesNotExist())
        .andExpect(jsonPath("$.data.phoneNumber").doesNotExist());

    User saved = userRepository.findById(user.getId()).orElseThrow();
    assertThat(saved.isOnboarded()).isTrue();
    assertThat(saved.getNickname()).isEqualTo("정우");
    assertThat(saved.getGender()).isEqualTo(Gender.MALE);
    assertThat(saved.getLatitude()).isEqualByComparingTo("37.551235");
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void 필드_검증에_실패하면_400과_필드별_메시지를_준다() throws Exception {
    Map<String, Object> body = validBody();
    body.put("nickname", "열한글자가넘는이름입니다");
    body.put("gender", "OTHER");
    body.put("age", 0);
    body.put("latitude", 91);
    body.remove("longitude");

    onboarding(user.getId(), jpeg(), json(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"))
        .andExpect(jsonPath("$.data.nickname").value("이름은 10자 이하여야 합니다."))
        .andExpect(jsonPath("$.data.gender").value("성별은 MALE 또는 FEMALE이어야 합니다."))
        .andExpect(jsonPath("$.data.age").exists())
        .andExpect(jsonPath("$.data.latitude").exists())
        .andExpect(jsonPath("$.data.longitude").value("위치 정보가 필요합니다."));

    assertNotOnboardedAndNotUploaded();
  }

  @Test
  void 이름이_공백뿐이면_400() throws Exception {
    Map<String, Object> body = validBody();
    body.put("nickname", "   ");

    onboarding(user.getId(), jpeg(), json(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data.nickname").value("이름을 입력해주세요."));
  }

  @Test
  void 사진_파트가_없으면_400() throws Exception {
    onboarding(user.getId(), null, json(validBody()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));

    assertNotOnboardedAndNotUploaded();
  }

  @Test
  void request_파트가_없으면_400() throws Exception {
    onboarding(user.getId(), jpeg(), null)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));

    assertNotOnboardedAndNotUploaded();
  }

  @Test
  void request_파트가_JSON이_아니면_415() throws Exception {
    MockMultipartFile textPart = new MockMultipartFile(
        "request", "", MediaType.TEXT_PLAIN_VALUE,
        objectMapper.writeValueAsBytes(validBody()));

    onboarding(user.getId(), jpeg(), textPart)
        .andExpect(status().isUnsupportedMediaType())
        .andExpect(jsonPath("$.code").value("GLB-E010"));

    assertNotOnboardedAndNotUploaded();
  }

  @Test
  void 빈_사진이면_IMG_E001() throws Exception {
    MockMultipartFile empty =
        new MockMultipartFile("profileImage", "selfie.jpg", "image/jpeg", new byte[0]);

    onboarding(user.getId(), empty, json(validBody()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("IMG-E001"));

    assertNotOnboardedAndNotUploaded();
  }

  @Test
  void 지원하지_않는_이미지_형식이면_IMG_E002() throws Exception {
    MockMultipartFile gif =
        new MockMultipartFile("profileImage", "selfie.gif", "image/gif", new byte[] {1, 2, 3});

    onboarding(user.getId(), gif, json(validBody()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("IMG-E002"));

    assertNotOnboardedAndNotUploaded();
  }

  @Test
  void 이미_온보딩한_유저면_업로드하지_않고_409() throws Exception {
    onboarding(user.getId(), jpeg(), json(validBody())).andExpect(status().isOk());

    onboarding(user.getId(), jpeg(), json(validBody()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("USR-E004"));

    // 첫 번째 요청에서만 업로드
    verify(s3Client, org.mockito.Mockito.times(1))
        .putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void 존재하지_않는_유저면_업로드하지_않고_404() throws Exception {
    onboarding(999_999L, jpeg(), json(validBody()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USR-E002"));

    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void X_User_Id_헤더가_없으면_400() throws Exception {
    MockMultipartHttpServletRequestBuilder request = multipart(URL);
    request.file(jpeg()).file(json(validBody()));

    mockMvc.perform(request)
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }

  private ResultActions onboarding(
      Long userId, MockMultipartFile image, MockMultipartFile requestPart) throws Exception {
    MockMultipartHttpServletRequestBuilder request = multipart(URL);
    if (image != null) {
      request.file(image);
    }
    if (requestPart != null) {
      request.file(requestPart);
    }
    request.header("X-User-Id", userId);
    return mockMvc.perform(request);
  }

  private void assertNotOnboardedAndNotUploaded() {
    assertThat(userRepository.findById(user.getId()).orElseThrow().isOnboarded()).isFalse();
    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  private Map<String, Object> validBody() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("nickname", "정우");
    body.put("gender", "MALE");
    body.put("age", 25);
    body.put("latitude", new BigDecimal("37.551234"));
    body.put("longitude", new BigDecimal("126.923456"));
    return body;
  }

  private MockMultipartFile json(Map<String, Object> body) throws Exception {
    return new MockMultipartFile(
        "request", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(body));
  }

  private MockMultipartFile jpeg() {
    return new MockMultipartFile("profileImage", "selfie.jpg", "image/jpeg", ImageFixtures.JPEG);
  }
}
