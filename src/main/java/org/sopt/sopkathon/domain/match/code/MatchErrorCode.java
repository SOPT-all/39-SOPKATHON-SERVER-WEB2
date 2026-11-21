package org.sopt.sopkathon.domain.match.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.global.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MatchErrorCode implements ErrorCode {

  CANNOT_SKIP_SELF(
      "MAT-E001",
      HttpStatus.BAD_REQUEST,
      "자기 자신은 넘길 수 없습니다."
  );

  private final String code;
  private final HttpStatus httpStatus;
  private final String message;
}
