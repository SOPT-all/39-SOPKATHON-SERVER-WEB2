package org.sopt.sopkathon.global.common.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

  INVALID_REQUEST(
      "GLB-E001",
      HttpStatus.BAD_REQUEST,
      "잘못된 요청입니다."
  ),

  UNAUTHORIZED(
      "GLB-E002",
      HttpStatus.UNAUTHORIZED,
      "인증이 필요합니다."
  ),

  FORBIDDEN(
      "GLB-E003",
      HttpStatus.FORBIDDEN,
      "접근 권한이 없습니다."
  ),

  RESOURCE_NOT_FOUND(
      "GLB-E004",
      HttpStatus.NOT_FOUND,
      "리소스를 찾을 수 없습니다."
  ),

  INTERNAL_SERVER_ERROR(
      "GLB-E005",
      HttpStatus.INTERNAL_SERVER_ERROR,
      "서버 내부 오류가 발생했습니다."
  ),

  METHOD_NOT_ALLOWED(
      "GLB-E006",
      HttpStatus.METHOD_NOT_ALLOWED,
      "지원하지 않는 HTTP 메서드입니다."
  ),

  TOO_MANY_REQUESTS(
      "GLB-E007",
      HttpStatus.TOO_MANY_REQUESTS,
      "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
  ),

  PAYLOAD_TOO_LARGE(
      "GLB-E008",
      HttpStatus.CONTENT_TOO_LARGE,
      "요청 크기가 허용 범위를 초과했습니다."
  );

  private final String code;
  private final HttpStatus httpStatus;
  private final String message;
}
