package org.sopt.sopkathon.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.sopkathon.domain.place.entity.Place;

/** 장소 좌표는 내려주지 않는다. 거리와 함께 주면 회원 위치를 역산할 수 있다. */
@Schema(description = "제휴 장소")
public record PlaceResponse(
    @Schema(description = "장소 ID", example = "3") Long placeId,
    @Schema(description = "장소 이름", example = "소소한 찻집") String name,
    @Schema(description = "장소 이미지 URL. 없으면 null", nullable = true) String imageUrl,
    @Schema(description = "주소. 없으면 null", nullable = true, example = "서울시 마포구 와우산로 00")
        String address,
    @Schema(description = "내 위치와의 직선 거리 (미터, 100m 단위로 올림)", example = "400") int distance,
    @Schema(description = "제휴 혜택 문구. 없으면 null", nullable = true, example = "시니어 커플 10% 할인")
        String benefitText,
    @Schema(description = "네이버 지도 링크. 없으면 null", nullable = true) String naverMapUrl) {

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
