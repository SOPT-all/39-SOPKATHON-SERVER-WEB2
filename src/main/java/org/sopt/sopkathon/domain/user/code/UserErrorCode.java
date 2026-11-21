package org.sopt.sopkathon.domain.user.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.global.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

  INVALID_PHONE_NUMBER(
      "USR-E001",
      HttpStatus.BAD_REQUEST,
      "올바른 휴대폰 번호 형식이 아닙니다."
  ),

  USER_NOT_FOUND(
      "USR-E002",
      HttpStatus.NOT_FOUND,
      "존재하지 않는 유저입니다."
  ),

  USER_NOT_ONBOARDED(
      "USR-E003",
      HttpStatus.BAD_REQUEST,
      "온보딩을 완료하지 않은 유저입니다."
  );

  private final String code;
  private final HttpStatus httpStatus;
  private final String message;
}
