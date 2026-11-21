package org.sopt.sopkathon.global.s3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * S3 버킷 내 이미지 저장 경로. 새 용도가 생기면 여기에 추가한다.
 */
@Getter
@RequiredArgsConstructor
public enum ImageDirectory {

  PROFILE("profile");

  private final String path;
}
