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
          온보딩(S-002)에서 셀카, 이름, 성별, 나이, 위치를 한 번에 저장하고 온보딩을 완료합니다.
          사진은 S3에 올라가고 URL이 저장됩니다. **모든 값이 필수**입니다.

          **요청 형식**

          ① multipart/form-data로 **profileImage**(파일)와 **request**(JSON) 두 파트를 보냅니다.

          ② request 파트는 반드시 Content-Type을 application/json으로 지정해야 합니다. FormData에 문자열로 그냥 넣으면 text/plain이 되어 415가 납니다. Blob에 type: "application/json"을 지정해 주세요.

          ③ 요청 전체의 Content-Type은 직접 넣지 않습니다. 브라우저가 boundary와 함께 자동으로 설정합니다.

          **입력 규칙**

          ① 이름은 앞뒤 공백을 뺀 1 ~ 10자이고, 저장할 때 앞뒤 공백을 제거합니다.

          ② 성별은 MALE 또는 FEMALE, 나이는 1 ~ 120입니다.

          ③ 위도는 -90 ~ 90, 경도는 -180 ~ 180이고 소수점 6자리까지 저장합니다. 위치 권한을 거부하면 좌표가 없으므로 온보딩할 수 없습니다.

          ④ 사진은 jpg, jpeg, png, webp만 가능하고 최대 5MB입니다.

          **응답**

          이미 있는 회원 정보를 채우는 것이라 200으로 응답합니다. 성공하면 매칭 화면(S-003)으로 이동합니다.
          좌표와 전화번호는 응답에 포함하지 않습니다. 이미 온보딩한 회원은 다시 할 수 없습니다(409).
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
                          "nickname": "정우",
                          "gender": "MALE",
                          "age": 25,
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
                      name = "GLB-E001 필드 검증 실패",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E001",
                            "message": "잘못된 요청입니다.",
                            "data": {
                              "nickname": "이름은 10자 이하여야 합니다.",
                              "gender": "성별은 MALE 또는 FEMALE이어야 합니다."
                            }
                          }
                          """),
                  @ExampleObject(
                      name = "GLB-E001 파트·헤더 누락, JSON 형식 오류",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E001",
                            "message": "잘못된 요청입니다."
                          }
                          """),
                  @ExampleObject(
                      name = "IMG-E001 빈 사진 파일",
                      value = """
                          {
                            "success": false,
                            "code": "IMG-E001",
                            "message": "업로드할 이미지가 없습니다."
                          }
                          """),
                  @ExampleObject(
                      name = "IMG-E002 지원하지 않는 이미지 형식",
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
          description = "존재하지 않는 유저",
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
          description = "이미 온보딩을 완료한 유저",
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
          description = "사진 5MB 초과 또는 요청 전체 8MB 초과",
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
          description = "요청이 multipart/form-data가 아니거나 request 파트가 application/json이 아님",
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
