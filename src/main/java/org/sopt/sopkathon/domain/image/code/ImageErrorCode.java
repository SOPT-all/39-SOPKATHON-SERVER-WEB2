package org.sopt.sopkathon.domain.image.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.global.common.code.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {

  EMPTY_IMAGE(
      "IMG-E001",
      HttpStatus.BAD_REQUEST,
      "업로드할 이미지가 없습니다."
  ),

  INVALID_IMAGE_TYPE(
      "IMG-E002",
      HttpStatus.BAD_REQUEST,
      "지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, webp)"
  ),

  IMAGE_UPLOAD_FAILED(
      "IMG-E003",
      HttpStatus.INTERNAL_SERVER_ERROR,
      "이미지 업로드에 실패했습니다."
  );

  private final String code;
  private final HttpStatus httpStatus;
  private final String message;
}
