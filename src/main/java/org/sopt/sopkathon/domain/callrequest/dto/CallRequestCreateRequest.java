package org.sopt.sopkathon.domain.callrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 쪽지는 앞뒤 공백을 제거하고, 비어 있으면 null로 바꾼 뒤 길이를 검증한다. */
@Schema(description = "전화 요청 보내기")
public record CallRequestCreateRequest(
    @Schema(description = "요청을 받을 사람의 userId (매칭 상대 조회 응답의 users[].userId)", example = "12")
    @NotNull(message = "요청을 받을 사람을 지정해주세요.")
    Long receiverId,

    @Schema(description = "함께 보낼 쪽지 (선택, 최대 50자). 생략하거나 공백뿐이면 쪽지 없이 보냄", example = "산책 같이 하실래요?")
    @Size(max = 50, message = "쪽지는 50자 이하여야 합니다.")
    String message
) {

  public CallRequestCreateRequest {
    message = (message == null || message.isBlank()) ? null : message.strip();
  }
}
