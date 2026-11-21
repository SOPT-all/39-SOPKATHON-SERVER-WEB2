package org.sopt.sopkathon.domain.match.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.sopt.sopkathon.domain.callrequest.repository.CallRequestRepository;
import org.sopt.sopkathon.domain.match.entity.MatchSkip;
import org.sopt.sopkathon.domain.match.repository.MatchSkipRepository;
import org.sopt.sopkathon.domain.user.entity.Gender;
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
class MatchControllerTest {

  private static final String BASE_LAT = "37.550000";
  private static final String BASE_LON = "126.920000";

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private MatchSkipRepository matchSkipRepository;
  @Autowired private CallRequestRepository callRequestRepository;
  @Autowired private EntityManager entityManager;

  private User me;

  @BeforeEach
  void setUp() {
    me = onboardedUser("01000000000", "나", Gender.MALE, BASE_LAT, BASE_LON);
  }

  @Test
  void 가까운_순으로_다른_성별의_온보딩한_상대만_조회한다() throws Exception {
    User far = onboardedUser("01011111111", "먼사람", Gender.FEMALE, "37.560000", BASE_LON);
    User near = onboardedUser("01022222222", "가까운사람", Gender.FEMALE, "37.551000", BASE_LON);
    onboardedUser("01033333333", "같은성별", Gender.MALE, "37.550100", BASE_LON);
    userRepository.save(User.create("01044444444")); // 온보딩 미완료

    mockMvc
        .perform(get("/api/v1/matches").header("X-User-Id", me.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("GLB-S001"))
        .andExpect(jsonPath("$.data.users[*].userId").value(contains(
            near.getId().intValue(), far.getId().intValue())))
        .andExpect(jsonPath("$.data.users[0].nickname").value("가까운사람"))
        .andExpect(jsonPath("$.data.users[0].distance").value(111))
        .andExpect(jsonPath("$.data.users[0].phoneNumber").doesNotExist())
        .andExpect(jsonPath("$.data.users[0].latitude").doesNotExist())
        .andExpect(jsonPath("$.data.hasNext").value(false));
  }

  @Test
  void 넘긴_상대와_요청을_주고받은_상대는_제외한다() throws Exception {
    User skipped = onboardedUser("01011111111", "넘긴사람", Gender.FEMALE, "37.551000", BASE_LON);
    User requestedByMe =
        onboardedUser("01022222222", "내가요청", Gender.FEMALE, "37.552000", BASE_LON);
    User requestedMe =
        onboardedUser("01033333333", "나에게요청", Gender.FEMALE, "37.553000", BASE_LON);
    User visible = onboardedUser("01044444444", "보이는사람", Gender.FEMALE, "37.554000", BASE_LON);

    matchSkipRepository.save(MatchSkip.create(me, skipped));
    callRequestRepository.save(CallRequest.create(me, requestedByMe, null));
    callRequestRepository.save(CallRequest.create(requestedMe, me, null));

    mockMvc
        .perform(get("/api/v1/matches").header("X-User-Id", me.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.users[*].userId").value(contains(visible.getId().intValue())));
  }

  @Test
  void 좌표가_없는_상대는_제외한다() throws Exception {
    User noLocation = onboardedUserWithoutLocation("01011111111", Gender.FEMALE);
    User visible = onboardedUser("01022222222", "보이는사람", Gender.FEMALE, "37.551000", BASE_LON);

    mockMvc
        .perform(get("/api/v1/matches").header("X-User-Id", me.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.users[*].userId").value(contains(visible.getId().intValue())));
  }

  @Test
  void 내_좌표가_없으면_400() throws Exception {
    User noLocation = onboardedUserWithoutLocation("01055555555", Gender.MALE);

    mockMvc
        .perform(get("/api/v1/matches").header("X-User-Id", noLocation.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("USR-E003"));
  }

  @Test
  void limit보다_후보가_많으면_limit명만_주고_hasNext가_true다() throws Exception {
    onboardedUser("01011111111", "a", Gender.FEMALE, "37.551000", BASE_LON);
    onboardedUser("01022222222", "b", Gender.FEMALE, "37.552000", BASE_LON);
    onboardedUser("01033333333", "c", Gender.FEMALE, "37.553000", BASE_LON);

    mockMvc
        .perform(get("/api/v1/matches").param("limit", "2").header("X-User-Id", me.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.users", hasSize(2)))
        .andExpect(jsonPath("$.data.hasNext").value(true));
  }

  @Test
  void 후보가_없으면_빈_목록을_200으로_준다() throws Exception {
    mockMvc
        .perform(get("/api/v1/matches").header("X-User-Id", me.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.users", hasSize(0)))
        .andExpect(jsonPath("$.data.hasNext").value(false));
  }

  @Test
  void X_User_Id_헤더가_없으면_400() throws Exception {
    mockMvc
        .perform(get("/api/v1/matches"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }

  @Test
  void limit이_범위_밖이면_400() throws Exception {
    mockMvc
        .perform(get("/api/v1/matches").param("limit", "51").header("X-User-Id", me.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }

  @Test
  void 존재하지_않는_유저면_404() throws Exception {
    mockMvc
        .perform(get("/api/v1/matches").header("X-User-Id", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USR-E002"));
  }

  @Test
  void 온보딩을_마치지_않은_유저면_400() throws Exception {
    User notOnboarded = userRepository.save(User.create("01055555555"));

    mockMvc
        .perform(get("/api/v1/matches").header("X-User-Id", notOnboarded.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("USR-E003"));
  }

  private User onboardedUser(
      String phoneNumber, String nickname, Gender gender, String latitude, String longitude) {
    User user = User.create(phoneNumber);
    user.completeOnboarding(
        nickname,
        gender,
        30,
        "https://example.com/" + phoneNumber + ".jpg",
        new BigDecimal(latitude),
        new BigDecimal(longitude));
    return userRepository.save(user);
  }

  /**
   * 온보딩은 했지만 좌표가 비어 있는 회원. 엔티티 메서드로는 만들 수 없어서(좌표 필수), DB에 직접 넣은 데이터처럼 SQL로 좌표를
   * 지운다.
   */
  private User onboardedUserWithoutLocation(String phoneNumber, Gender gender) {
    User user = onboardedUser(phoneNumber, "좌표없음", gender, BASE_LAT, BASE_LON);
    entityManager.flush();
    entityManager
        .createNativeQuery("update users set latitude = null, longitude = null where id = :id")
        .setParameter("id", user.getId())
        .executeUpdate();
    entityManager.clear();
    return user;
  }
}
