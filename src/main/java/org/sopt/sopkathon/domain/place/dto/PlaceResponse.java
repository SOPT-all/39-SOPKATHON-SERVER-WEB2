package org.sopt.sopkathon.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.sopt.sopkathon.domain.place.entity.Place;

@Schema(description = "제휴 장소")
public record PlaceResponse(
    @Schema(description = "장소 ID", example = "3") Long placeId,
    @Schema(description = "장소 이름", example = "소소한 찻집") String name,
    @Schema(description = "장소 이미지 URL. 없으면 null", nullable = true) String imageUrl,
    @Schema(description = "주소. 없으면 null", nullable = true, example = "서울시 마포구 와우산로 00")
        String address,
    @Schema(description = "위도", example = "37.551234") BigDecimal latitude,
    @Schema(description = "경도", example = "126.923456") BigDecimal longitude,
    @Schema(description = "내 위치와의 직선 거리 (미터, 정수)", example = "350") int distance,
    @Schema(description = "제휴 혜택 문구. 없으면 null", nullable = true, example = "시니어 커플 10% 할인")
        String benefitText,
    @Schema(description = "네이버 지도 링크. 없으면 null", nullable = true) String naverMapUrl) {

  public static PlaceResponse from(Place place, int distance) {
    return new PlaceResponse(
        place.getId(),
        place.getName(),
        place.getImageUrl(),
        place.getAddress(),
        place.getLatitude(),
        place.getLongitude(),
        distance,
        place.getBenefitText(),
        place.getNaverMapUrl());
  }
}
