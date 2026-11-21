package org.sopt.sopkathon.domain.callrequest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequestStatus;

@Schema(description = "받은 요청")
public record ReceivedRequestResponse(
    @Schema(description = "요청 ID. 전화 완료 API의 requestId로 사용", example = "15") Long requestId,
    @Schema(description = "PENDING(통화 전) / CONNECTED(통화 완료)", example = "PENDING")
        CallRequestStatus status,
    @Schema(description = "함께 보낸 쪽지 (최대 50자). 없으면 null", example = "산책 같이 하실래요?", nullable = true)
        String message,
    @Schema(description = "요청을 받은 시각 (yyyy-MM-ddTHH:mm:ss)", example = "2026-11-21T14:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
    @Schema(description = "요청을 보낸 사람") RequesterResponse requester) {

  public static ReceivedRequestResponse from(CallRequest callRequest, Integer distance) {
    return new ReceivedRequestResponse(
        callRequest.getId(),
        callRequest.getStatus(),
        callRequest.getMessage(),
        callRequest.getCreatedAt(),
        RequesterResponse.from(callRequest.getRequester(), distance));
  }
}
