package org.sopt.sopkathon.domain.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageDirectory {

  PROFILE("profile");

  private final String path;
}
