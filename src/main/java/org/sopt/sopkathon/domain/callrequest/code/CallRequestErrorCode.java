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
  ),

  SAME_GENDER(
      "REQ-E003",
      HttpStatus.BAD_REQUEST,
      "같은 성별에게는 요청할 수 없습니다."
  ),

  SKIPPED_TARGET(
      "REQ-E004",
      HttpStatus.BAD_REQUEST,
      "넘긴 상대에게는 요청할 수 없습니다."
  ),

  CALL_REQUEST_NOT_FOUND(
      "REQ-E005",
      HttpStatus.NOT_FOUND,
      "존재하지 않는 전화 요청입니다."
  );

  private final String code;
  private final HttpStatus httpStatus;
  private final String message;
}
