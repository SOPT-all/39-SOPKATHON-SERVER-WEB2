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

/** {@link UserController}의 Swagger 명세. 컨트롤러에는 동작 코드만 두고 문서는 여기서 관리한다. */
@Tag(name = "User", description = "온보딩")
public interface UserApi {

  @Operation(
      summary = "온보딩",
      description = """
          온보딩(S-002)에서 셀카, 이름, 성별, 나이, 위치를 **한 번에** 저장하고 온보딩을 완료합니다. **모든 값이 필수**입니다.
          로그인 응답이 onboarded: false일 때 이 화면으로 오고, 성공하면 매칭 화면(S-003)으로 이동합니다.

          **보내는 방법 (꼭 읽어주세요)**

          ① FormData에 **profileImage**(셀카 파일)와 **request**(나머지 정보 JSON) 두 파트를 담아 보냅니다.

          ② request 파트는 반드시 **Blob에 type: "application/json"** 을 지정해서 넣습니다. 문자열로 그냥 append하면 서버가 읽지 못해 **415**가 납니다.

          ③ 요청 헤더에 Content-Type을 **직접 넣지 않습니다.** 직접 넣으면 boundary가 빠져서 요청이 깨집니다. 브라우저가 자동으로 넣게 두세요. (axios도 동일)

          **request (JSON) 입력 규칙**

          ① nickname: 앞뒤 공백을 뺀 **1 ~ 10자**. 앞뒤 공백은 서버가 지우고 저장합니다.

          ② gender: MALE 또는 FEMALE / age: **1 ~ 120** 정수

          ③ latitude: -90 ~ 90 / longitude: -180 ~ 180. 소수점 6자리까지 저장되며 navigator.geolocation 값을 그대로 보내면 됩니다.

          ④ **위치 권한을 거부하면 좌표가 없으므로 "시작하기" 버튼을 비활성화**해 주세요.

          **profileImage (파일) 규칙**

          ① 형식은 **jpg, jpeg, png, webp**, 크기는 **최대 5MB**입니다.

          ② 파일 이름의 확장자, 파일 타입, **실제 파일 내용**이 모두 같은 형식이어야 합니다. 확장자만 바꾼 파일(png를 .jpg로 이름만 변경 등)은 거절됩니다.

          ③ gif, heic(아이폰 원본)는 지원하지 않습니다. 필요하면 프론트에서 jpg로 변환하고, 5MB가 넘는 사진은 업로드 전에 리사이즈·압축해 주세요.

          **응답과 에러 처리**

          ① 200 응답에는 저장된 정보와 셀카 URL이 담깁니다. 위치와 전화번호는 응답에 없습니다.

          ② 입력값 규칙 위반은 400 GLB-E001이고, **data에 필드 이름별 메시지**가 담깁니다. 해당 입력란 아래에 그대로 보여주면 됩니다.

          ③ 409 USR-E004(이미 온보딩함)는 매칭 화면으로, 404 USR-E002(없는 회원)는 로그인 화면으로 이동하면 됩니다.

          ④ 에러 분기는 HTTP 상태보다 **code 값으로** 하는 것을 권장합니다.

          **참고**

          온보딩은 **한 번만** 할 수 있고 프로필 수정 기능은 없습니다. "시작하기"를 여러 번 눌러도 한 번만 저장되고 나머지 요청은 409로 응답하니, 전송 중에는 버튼을 비활성화해 주세요.
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
