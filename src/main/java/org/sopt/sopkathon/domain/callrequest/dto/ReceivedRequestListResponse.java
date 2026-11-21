package org.sopt.sopkathon.domain.callrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "받은 요청 목록")
public record ReceivedRequestListResponse(
    @Schema(description = "최신순 목록. 없으면 빈 배열") List<ReceivedRequestResponse> requests) {

  public static ReceivedRequestListResponse of(List<ReceivedRequestResponse> requests) {
    return new ReceivedRequestListResponse(requests);
  }
}
