package org.sopt.sopkathon.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.auth.dto.LoginRequest;
import org.sopt.sopkathon.domain.auth.dto.LoginResponse;
import org.sopt.sopkathon.domain.auth.service.AuthService;
import org.sopt.sopkathon.global.common.code.GlobalSuccessCode;
import org.sopt.sopkathon.global.response.BaseResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "로그인")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Operation(
      summary = "전화번호 로그인",
      description = "처음 입력한 번호면 회원을 만들고, 이미 있는 번호면 로그인한다. "
          + "응답의 userId는 이후 요청의 X-User-Id 헤더로, onboarded로 다음 화면(온보딩/매칭)을 정한다.")
  @PostMapping("/login")
  public BaseResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return BaseResponse.success(GlobalSuccessCode.OK, authService.login(request));
  }
}
