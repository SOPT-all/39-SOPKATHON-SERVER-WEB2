package org.sopt.sopkathon.domain.place.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.domain.place.entity.Place;
import org.sopt.sopkathon.domain.place.repository.PlaceRepository;
import org.sopt.sopkathon.domain.user.entity.Gender;
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
class PlaceControllerTest {

  private static final String URL = "/api/v1/places";
  private static final String BASE_LAT = "37.550000";
  private static final String BASE_LON = "126.920000";

  @Autowired private MockMvc mockMvc;
  @Autowired private UserRepository userRepository;
  @Autowired private PlaceRepository placeRepository;
  @Autowired private EntityManager entityManager;

  private User me;

  @BeforeEach
  void setUp() {
    me = User.create("01000000000");
    me.completeOnboarding(
        "나", Gender.MALE, 65, "https://example.com/me.jpg",
        new BigDecimal(BASE_LAT), new BigDecimal(BASE_LON));
    userRepository.save(me);
  }

  @Test
  void 가까운_순으로_활성_장소만_조회한다() throws Exception {
    Place far = place("먼곳", "37.560000");
    Place near = place("가까운곳", "37.551000");
    Place inactive = place("비활성", "37.550100");
    deactivate(inactive);

    getPlaces(null, null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("GLB-S001"))
        .andExpect(jsonPath("$.data.places[*].placeId").value(contains(
            near.getId().intValue(), far.getId().intValue())))
        .andExpect(jsonPath("$.data.places[0].name").value("가까운곳"))
        .andExpect(jsonPath("$.data.places[0].distance").value(111))
        .andExpect(jsonPath("$.data.places[1].distance").value(1112))
        .andExpect(jsonPath("$.data.page").value(0))
        .andExpect(jsonPath("$.data.size").value(20))
        .andExpect(jsonPath("$.data.hasNext").value(false));
  }

  @Test
  void 거리가_같으면_placeId_순이다() throws Exception {
    Place first = place("A", "37.551000");
    Place second = place("B", "37.551000");

    getPlaces(null, null)
        .andExpect(jsonPath("$.data.places[*].placeId").value(contains(
            first.getId().intValue(), second.getId().intValue())));
  }

  @Test
  void 페이지를_나눠서_조회하고_hasNext로_다음_페이지_여부를_알려준다() throws Exception {
    Place p1 = place("1", "37.551000");
    Place p2 = place("2", "37.552000");
    Place p3 = place("3", "37.553000");

    getPlaces(0, 2)
        .andExpect(jsonPath("$.data.places[*].placeId").value(contains(
            p1.getId().intValue(), p2.getId().intValue())))
        .andExpect(jsonPath("$.data.hasNext").value(true));

    getPlaces(1, 2)
        .andExpect(jsonPath("$.data.places[*].placeId").value(contains(p3.getId().intValue())))
        .andExpect(jsonPath("$.data.page").value(1))
        .andExpect(jsonPath("$.data.hasNext").value(false));
  }

  @Test
  void 마지막_페이지를_넘으면_빈_목록이다() throws Exception {
    place("1", "37.551000");

    getPlaces(5, 20)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.places", hasSize(0)))
        .andExpect(jsonPath("$.data.hasNext").value(false));

    getPlaces(Integer.MAX_VALUE, 50)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.places", hasSize(0)));
  }

  @Test
  void 장소가_없으면_빈_목록을_200으로_준다() throws Exception {
    getPlaces(null, null)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.places", hasSize(0)))
        .andExpect(jsonPath("$.data.hasNext").value(false));
  }

  @Test
  void 값이_없는_선택_필드도_null로_키가_내려간다() throws Exception {
    placeRepository.save(Place.builder()
        .name("정보없음")
        .latitude(new BigDecimal("37.551000"))
        .longitude(new BigDecimal(BASE_LON))
        .build());

    getPlaces(null, null)
        .andExpect(jsonPath("$.data.places[0].imageUrl").value(org.hamcrest.Matchers.nullValue()))
        .andExpect(jsonPath("$.data.places[0].address").value(org.hamcrest.Matchers.nullValue()))
        .andExpect(jsonPath("$.data.places[0].benefitText").value(org.hamcrest.Matchers.nullValue()))
        .andExpect(jsonPath("$.data.places[0].naverMapUrl").value(org.hamcrest.Matchers.nullValue()))
        .andExpect(jsonPath("$.data.places[0]").value(
            org.hamcrest.Matchers.hasKey("naverMapUrl")));
  }

  @Test
  void page나_size가_범위_밖이면_400() throws Exception {
    getPlaces(-1, 20).andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
    getPlaces(0, 0).andExpect(status().isBadRequest());
    getPlaces(0, 51).andExpect(status().isBadRequest());
  }

  @Test
  void X_User_Id_헤더가_없으면_400() throws Exception {
    mockMvc.perform(get(URL))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("GLB-E001"));
  }

  @Test
  void 존재하지_않는_유저면_404() throws Exception {
    mockMvc.perform(get(URL).header("X-User-Id", 999_999))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("USR-E002"));
  }

  @Test
  void 온보딩을_마치지_않은_유저면_400() throws Exception {
    User notOnboarded = userRepository.save(User.create("01055555555"));

    mockMvc.perform(get(URL).header("X-User-Id", notOnboarded.getId()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("USR-E003"));
  }

  private ResultActions getPlaces(Integer page, Integer size) throws Exception {
    var request = get(URL).header("X-User-Id", me.getId());
    if (page != null) {
      request.param("page", String.valueOf(page));
    }
    if (size != null) {
      request.param("size", String.valueOf(size));
    }
    return mockMvc.perform(request);
  }

  private Place place(String name, String latitude) {
    return placeRepository.save(Place.builder()
        .name(name)
        .imageUrl("https://example.com/" + name + ".jpg")
        .address("서울시 마포구")
        .latitude(new BigDecimal(latitude))
        .longitude(new BigDecimal(BASE_LON))
        .benefitText("10% 할인")
        .naverMapUrl("https://naver.me/" + name)
        .build());
  }

  /** 비활성 처리 기능이 없어서 운영자가 DB에서 바꾸는 것처럼 SQL로 바꾼다. */
  private void deactivate(Place place) {
    entityManager.flush();
    entityManager.createNativeQuery("update places set is_active = false where id = :id")
        .setParameter("id", place.getId())
        .executeUpdate();
    entityManager.clear();
  }
}
