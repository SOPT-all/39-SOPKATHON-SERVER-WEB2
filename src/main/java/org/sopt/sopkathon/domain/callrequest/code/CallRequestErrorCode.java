package org.sopt.sopkathon.domain.callrequest.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.global.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CallRequestErrorCode implements ErrorCode {

  CANNOT_REQUEST_SELF(
      "REQ-E001",
      HttpStatus.BAD_REQUEST,
      "자기 자신에게는 요청할 수 없습니다."
  ),

  ALREADY_REQUESTED(
      "REQ-E002",
      HttpStatus.CONFLICT,
      "이미 요청을 주고받은 상대입니다."
  );

  private final String code;
  private final HttpStatus httpStatus;
  private final String message;
}
