package org.sopt.sopkathon.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.sopkathon.domain.place.entity.Place;

/** 장소 좌표는 내려주지 않는다. 거리와 함께 주면 회원 위치를 역산할 수 있다. */
@Schema(description = "제휴 장소")
public record PlaceResponse(
    @Schema(description = "장소 ID. 목록 key, 중복 제거 기준", example = "1") Long placeId,
    @Schema(description = "장소 이름", example = "인사동찻집") String name,
    @Schema(description = "장소 사진 URL. null이면 기본 이미지", nullable = true) String imageUrl,
    @Schema(description = "도로명 주소. null이면 주소 줄 숨김", nullable = true, example = "서울 종로구 인사동길 33-1")
        String address,
    @Schema(description = "내 위치와의 거리 (미터, 100m 단위로 올림). 1000 이상이면 km로 표시", example = "400") int distance,
    @Schema(description = "제휴 혜택 문구. null이면 혜택 뱃지 숨김", nullable = true, example = "시니어 커플 방문 시 전통차 10% 할인")
        String benefitText,
    @Schema(description = "네이버 지도 링크. null이면 지도 버튼 숨김", nullable = true) String naverMapUrl) {

  public static PlaceResponse from(Place place, int distance) {
    return new PlaceResponse(
        place.getId(),
        place.getName(),
        place.getImageUrl(),
        place.getAddress(),
        distance,
        place.getBenefitText(),
        place.getNaverMapUrl());
  }
}
