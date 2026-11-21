package org.sopt.sopkathon.domain.callrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequestStatus;

@Schema(description = "전화 완료 결과")
public record CallRequestConnectResponse(
    @Schema(description = "처리한 요청 ID", example = "15") Long requestId,
    @Schema(description = "변경 후 상태. 항상 CONNECTED", example = "CONNECTED") CallRequestStatus status) {

  public static CallRequestConnectResponse from(CallRequest callRequest) {
    return new CallRequestConnectResponse(callRequest.getId(), callRequest.getStatus());
  }
}
