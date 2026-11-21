package org.sopt.sopkathon.domain.match.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "매칭 상대 목록")
public record MatchListResponse(
    @Schema(description = "매칭 상대 목록 (가까운 순). 없으면 빈 배열") List<MatchUserResponse> users,
    @Schema(description = "이번에 받은 상대 말고도 보여줄 상대가 더 있는지", example = "true")
        boolean hasNext) {

  public static MatchListResponse of(List<MatchUserResponse> users, boolean hasNext) {
    return new MatchListResponse(users, hasNext);
  }
}
