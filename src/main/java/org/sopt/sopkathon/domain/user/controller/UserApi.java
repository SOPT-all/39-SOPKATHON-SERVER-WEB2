package org.sopt.sopkathon.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.sopkathon.domain.user.dto.OnboardingRequest;
import org.sopt.sopkathon.domain.user.dto.OnboardingResponse;
import org.sopt.sopkathon.global.response.BaseResponse;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "온보딩")
public interface UserApi {

  @Operation(
      summary = "온보딩",
      description = """
          셀카·이름·성별·나이·위치를 한 번에 저장하고 온보딩을 완료합니다. **모든 값 필수**

          ### 보내는 방법
          **① request 파트는 Blob에 type "application/json" 지정** (문자열로 넣으면 415)

          **② 요청 헤더에 Content-Type을 직접 넣지 않기** (boundary가 빠짐)

          ### 입력 규칙
          **이름** 1~10자 · **성별** MALE / FEMALE · **나이** 1~120 · **사진** jpg, png, webp / 5MB 이하

          입력값을 어기면 400과 함께 **data에 필드별 메시지**가 옵니다.
          """)
  // request 파트를 application/json으로 보내도록 지정 (없으면 Swagger UI가 text/plain으로 보내 415가 남)
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = @Content(
          mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
          encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)))
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "온보딩 완료",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "success": true,
                        "code": "GLB-S001",
                        "message": "요청이 성공했습니다.",
                        "data": {
                          "userId": 1,
                          "nickname": "순자",
                          "gender": "MALE",
                          "age": 65,
                          "profileImageUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/0566e0e8-0ff5-4623-b11a-f8ed61040320.jpg"
                        }
                      }
                      """))),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "GLB-E001 입력값 규칙 위반 (data에 필드별 메시지)",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E001",
                            "message": "잘못된 요청입니다.",
                            "data": {
                              "nickname": "이름은 10자 이하여야 합니다.",
                              "age": "나이는 1 이상 120 이하여야 합니다."
                            }
                          }
                          """),
                  @ExampleObject(
                      name = "GLB-E001 파트·헤더 누락, JSON 형식 오류 (개발 중에만 발생)",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E001",
                            "message": "잘못된 요청입니다."
                          }
                          """),
                  @ExampleObject(
                      name = "IMG-E001 빈 사진 파일 → 사진을 다시 선택해주세요",
                      value = """
                          {
                            "success": false,
                            "code": "IMG-E001",
                            "message": "업로드할 이미지가 없습니다."
                          }
                          """),
                  @ExampleObject(
                      name = "IMG-E002 지원하지 않는 형식 (확장자·타입·파일 내용 불일치 포함)",
                      value = """
                          {
                            "success": false,
                            "code": "IMG-E002",
                            "message": "지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, webp)"
                          }
                          """)
              })),
      @ApiResponse(
          responseCode = "404",
          description = "존재하지 않는 회원 → 로그인 화면으로 이동",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  name = "USR-E002",
                  value = """
                      {
                        "success": false,
                        "code": "USR-E002",
                        "message": "존재하지 않는 유저입니다."
                      }
                      """))),
      @ApiResponse(
          responseCode = "409",
          description = "이미 온보딩을 완료한 회원 → 매칭 화면으로 이동",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  name = "USR-E004",
                  value = """
                      {
                        "success": false,
                        "code": "USR-E004",
                        "message": "이미 온보딩을 완료한 유저입니다."
                      }
                      """))),
      @ApiResponse(
          responseCode = "413",
          description = "사진 5MB 초과 → 5MB 이하 사진을 올려주세요",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  name = "GLB-E008",
                  value = """
                      {
                        "success": false,
                        "code": "GLB-E008",
                        "message": "요청 크기가 허용 범위를 초과했습니다."
                      }
                      """))),
      @ApiResponse(
          responseCode = "415",
          description = "request 파트에 application/json을 지정하지 않음 (개발 중에만 발생)",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  name = "GLB-E010",
                  value = """
                      {
                        "success": false,
                        "code": "GLB-E010",
                        "message": "지원하지 않는 Content-Type입니다."
                      }
                      """))),
      @ApiResponse(
          responseCode = "500",
          description = "사진 업로드 실패 또는 서버 내부 오류",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "IMG-E003 사진 업로드 실패",
                      value = """
                          {
                            "success": false,
                            "code": "IMG-E003",
                            "message": "이미지 업로드에 실패했습니다."
                          }
                          """),
                  @ExampleObject(
                      name = "GLB-E005",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E005",
                            "message": "서버 내부 오류가 발생했습니다."
                          }
                          """)
              }))
  })
  BaseResponse<OnboardingResponse> completeOnboarding(
      @Parameter(
          name = "X-User-Id",
          in = ParameterIn.HEADER,
          required = true,
          description = "로그인 응답의 userId",
          example = "1")
      Long userId,
      // 파일 파트에는 Content-Type을 지정하지 않는다 (octet-stream으로 보내면 이미지 형식 검증에서 IMG-E002)
      @Parameter(description = "셀카 이미지 (jpg, jpeg, png, webp / 최대 5MB)")
      MultipartFile profileImage,
      @Parameter(
          description = "온보딩 정보 (JSON 파트, Content-Type: application/json)",
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
      OnboardingRequest request);
}
