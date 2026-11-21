package org.sopt.sopkathon.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.sopkathon.domain.auth.dto.LoginRequest;
import org.sopt.sopkathon.domain.auth.dto.LoginResponse;
import org.sopt.sopkathon.global.response.BaseResponse;

/** {@link AuthController}의 Swagger 명세. 컨트롤러에는 동작 코드만 두고 문서는 여기서 관리한다. */
@Tag(name = "Auth", description = "전화번호 로그인")
public interface AuthApi {

  @Operation(
      summary = "전화번호 로그인",
      description = """
          처음 입력한 번호면 **회원가입 + 로그인**, 이미 있는 번호면 **로그인**합니다. 둘 다 200으로 응답합니다.
          인증번호(SMS) 확인은 없습니다.

          **로그인 후 할 일**

          ① 응답의 **userId**를 저장하고, 이후 모든 요청 헤더에 **X-User-Id**로 넣습니다.

          ② 응답의 **onboarded**로 다음 화면을 정합니다. false면 온보딩(S-002), true면 매칭(S-003)

          **전화번호 입력 규칙**

          ① 하이픈·공백이 있어도 됩니다. 서버가 숫자만 남겨 처리합니다. (010-1234-5678, 01012345678은 같은 회원)

          ② 01로 시작하는 10~11자리 휴대폰 번호만 받습니다. 일반 전화(02-...), 국제번호 형식(+82-...)은 400입니다.

          로그인 버튼을 여러 번 눌러도 회원은 하나만 생기고, 모두 같은 userId로 응답합니다.
          """)
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      content = @Content(
          mediaType = "application/json",
          examples = {
              @ExampleObject(
                  name = "하이픈 포함",
                  value = """
                      { "phoneNumber": "010-1234-5678" }
                      """),
              @ExampleObject(
                  name = "숫자만",
                  value = """
                      { "phoneNumber": "01012345678" }
                      """)
          }))
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "로그인 성공 (신규 가입 포함)",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "온보딩 전 (신규 가입 또는 온보딩 미완료)",
                      value = """
                          {
                            "success": true,
                            "code": "GLB-S001",
                            "message": "요청이 성공했습니다.",
                            "data": { "userId": 1, "onboarded": false }
                          }
                          """),
                  @ExampleObject(
                      name = "온보딩 완료",
                      value = """
                          {
                            "success": true,
                            "code": "GLB-S001",
                            "message": "요청이 성공했습니다.",
                            "data": { "userId": 1, "onboarded": true }
                          }
                          """)
              })),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "USR-E001 휴대폰 번호 형식 아님",
                      value = """
                          {
                            "success": false,
                            "code": "USR-E001",
                            "message": "올바른 휴대폰 번호 형식이 아닙니다."
                          }
                          """),
                  @ExampleObject(
                      name = "GLB-E001 번호 누락·빈 값",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E001",
                            "message": "잘못된 요청입니다.",
                            "data": { "phoneNumber": "전화번호를 입력해주세요." }
                          }
                          """),
                  @ExampleObject(
                      name = "GLB-E001 JSON 형식 오류",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E001",
                            "message": "잘못된 요청입니다."
                          }
                          """)
              })),
      @ApiResponse(
          responseCode = "500",
          description = "서버 내부 오류",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  name = "GLB-E005",
                  value = """
                      {
                        "success": false,
                        "code": "GLB-E005",
                        "message": "서버 내부 오류가 발생했습니다."
                      }
                      """)))
  })
  BaseResponse<LoginResponse> login(LoginRequest request);
}
