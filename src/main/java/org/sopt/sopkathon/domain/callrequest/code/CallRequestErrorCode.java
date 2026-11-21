package org.sopt.sopkathon.domain.callrequest.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.global.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CallRequestErrorCode implements ErrorCode {

  CALL_REQUEST_NOT_FOUND(
      "REQ-E001",
      HttpStatus.NOT_FOUND,
      "존재하지 않는 전화 요청입니다."
  );

  private final String code;
  private final HttpStatus httpStatus;
  private final String message;
}
