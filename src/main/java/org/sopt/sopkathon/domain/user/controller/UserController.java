package org.sopt.sopkathon.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.user.dto.OnboardingRequest;
import org.sopt.sopkathon.domain.user.dto.OnboardingResponse;
import org.sopt.sopkathon.domain.user.service.OnboardingService;
import org.sopt.sopkathon.global.common.code.GlobalSuccessCode;
import org.sopt.sopkathon.global.response.BaseResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserApi {

  private final OnboardingService onboardingService;

  @Override
  @PostMapping(value = "/me/onboarding", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public BaseResponse<OnboardingResponse> completeOnboarding(
      @RequestHeader("X-User-Id") Long userId,
      @RequestPart("profileImage") MultipartFile profileImage,
      @Valid @RequestPart("request") OnboardingRequest request) {
    return BaseResponse.success(
        GlobalSuccessCode.OK, onboardingService.completeOnboarding(userId, profileImage, request));
  }
}
