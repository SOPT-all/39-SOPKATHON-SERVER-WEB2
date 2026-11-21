package org.sopt.sopkathon.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "근처 장소 목록 (한 페이지)")
public record PlaceListResponse(
    @Schema(description = "이번 페이지의 장소 (가까운 순). 없으면 빈 배열") List<PlaceResponse> places,
    @Schema(description = "이번 응답의 페이지 번호", example = "0") int page,
    @Schema(description = "요청한 페이지 크기", example = "20") int size,
    @Schema(description = "다음 페이지가 있는지. false면 더 요청하지 않음", example = "true") boolean hasNext) {

  public static PlaceListResponse of(List<PlaceResponse> places, int page, int size, boolean hasNext) {
    return new PlaceListResponse(places, page, size, hasNext);
  }
}
