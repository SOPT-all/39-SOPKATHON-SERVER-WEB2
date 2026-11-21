package org.sopt.sopkathon.domain.callrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequestStatus;

@Schema(description = "생성된 전화 요청")
public record CallRequestCreateResponse(
    @Schema(description = "생성된 요청 ID", example = "15") Long requestId,
    @Schema(description = "생성 직후 상태. 항상 PENDING", example = "PENDING") CallRequestStatus status) {

  public static CallRequestCreateResponse from(CallRequest callRequest) {
    return new CallRequestCreateResponse(callRequest.getId(), callRequest.getStatus());
  }
}
