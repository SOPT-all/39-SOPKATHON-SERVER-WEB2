package org.sopt.sopkathon.domain.auth.dto;

import org.sopt.sopkathon.domain.user.entity.User;

public record LoginResponse(
    Long userId,
    boolean onboarded
) {

  public static LoginResponse from(User user) {
    return new LoginResponse(user.getId(), user.isOnboarded());
  }
}
