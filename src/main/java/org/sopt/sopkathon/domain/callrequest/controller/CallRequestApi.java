package org.sopt.sopkathon.domain.callrequest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestCreateRequest;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestCreateResponse;
import org.sopt.sopkathon.domain.callrequest.dto.ReceivedRequestListResponse;
import org.sopt.sopkathon.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;

/** {@link CallRequestController}의 Swagger 명세. 컨트롤러에는 동작 코드만 두고 문서는 여기서 관리한다. */
@Tag(name = "CallRequest", description = "전화 요청 보내기 · 받은 요청")
public interface CallRequestApi {

  @Operation(
      summary = "요청 보내기 (번호 주기)",
      description = """
          매칭(S-003)에서 **쪽지 보내기**를 누르면 상대에게 전화 요청을 보냅니다.
          요청을 받은 사람에게 **내 전화번호가 공개**되고, 상대의 받은 요청 목록(S-005)에 내 요청이 생깁니다.

          **요청 규칙**

          ① 쪽지는 선택이며 최대 50자입니다. 생략하거나 공백뿐이면 쪽지 없이 번호만 보냅니다.

          ② 요청을 보낸 뒤에는 서로의 매칭 후보에서 빠집니다. (양방향 제외)

          ③ 같은 상대에게 다시 요청하거나, 상대가 이미 나에게 요청을 보낸 경우에는 409입니다.

          ④ 같은 성별이거나 내가 넘긴 상대에게는 요청할 수 없습니다. (400, 매칭 목록에 나온 상대라면 생기지 않음)

          **화면에서 쓰는 방법**

          ① 성공하면 들고 있던 매칭 목록에서 그 사람을 빼고 번호 주기 완료(S-004)로 이동합니다.

          ② 저장이 끝날 때까지 버튼을 비활성화해 주세요.

          ③ 409(REQ-E002, GLB-E009)는 이미 요청된 상태이므로, 요청 완료로 보여주고 다음 상대로 넘어가면 됩니다.
          """)
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      content = @Content(
          mediaType = "application/json",
          examples = {
              @ExampleObject(
                  name = "쪽지 포함",
                  value = """
                      { "receiverId": 12, "message": "산책 같이 하실래요?" }
                      """),
              @ExampleObject(
                  name = "번호만 보내기",
                  value = """
                      { "receiverId": 12 }
                      """)
          }))
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "요청 생성 성공",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  name = "GLB-S002",
                  value = """
                      {
                        "success": true,
                        "code": "GLB-S002",
                        "message": "리소스가 생성되었습니다.",
                        "data": { "requestId": 15, "status": "PENDING" }
                      }
                      """))),
      @ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "REQ-E001 자기 자신에게 요청",
                      value = """
                          {
                            "success": false,
                            "code": "REQ-E001",
                            "message": "자기 자신에게는 요청할 수 없습니다."
                          }
                          """),
                  @ExampleObject(
                      name = "REQ-E003 같은 성별에게 요청",
                      value = """
                          {
                            "success": false,
                            "code": "REQ-E003",
                            "message": "같은 성별에게는 요청할 수 없습니다."
                          }
                          """),
                  @ExampleObject(
                      name = "REQ-E004 넘긴 상대에게 요청",
                      value = """
                          {
                            "success": false,
                            "code": "REQ-E004",
                            "message": "넘긴 상대에게는 요청할 수 없습니다."
                          }
                          """),
                  @ExampleObject(
                      name = "USR-E003 온보딩 미완료",
                      value = """
                          {
                            "success": false,
                            "code": "USR-E003",
                            "message": "온보딩을 완료하지 않은 유저입니다."
                          }
                          """),
                  @ExampleObject(
                      name = "GLB-E001 receiverId 누락·쪽지 50자 초과",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E001",
                            "message": "잘못된 요청입니다.",
                            "data": { "message": "쪽지는 50자 이하여야 합니다." }
                          }
                          """),
                  @ExampleObject(
                      name = "GLB-E001 X-User-Id 헤더 누락·JSON 형식 오류",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E001",
                            "message": "잘못된 요청입니다."
                          }
                          """)
              })),
      @ApiResponse(
          responseCode = "404",
          description = "존재하지 않는 유저 (나 또는 상대, 상대가 온보딩 전인 경우 포함)",
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
          description = "이미 요청된 상태 (요청 완료로 처리)",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "REQ-E002 이미 요청을 주고받은 상대",
                      value = """
                          {
                            "success": false,
                            "code": "REQ-E002",
                            "message": "이미 요청을 주고받은 상대입니다."
                          }
                          """),
                  @ExampleObject(
                      name = "GLB-E009 같은 요청이 동시에 두 번 들어옴",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E009",
                            "message": "이미 처리된 요청입니다."
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
  ResponseEntity<BaseResponse<CallRequestCreateResponse>> send(
      @Parameter(
          name = "X-User-Id",
          in = ParameterIn.HEADER,
          required = true,
          description = "로그인 응답의 userId (요청을 보내는 사람)",
          example = "1")
      Long userId,
      CallRequestCreateRequest request);

  @Operation(
      summary = "받은 요청 리스트 조회",
      description = """
          받은 요청(S-005)에서 나에게 전화 요청을 보낸 사람 목록을 **최신순**으로 조회합니다.
          요청을 보낸 사람은 번호 공개에 동의한 것이므로 **보낸 사람의 전화번호**가 포함됩니다.

          **필터 (called)**

          ① false면 전화 안 건 사람(PENDING)만, true면 전화 건 사람(CONNECTED)만, 생략하면 전체를 조회합니다.

          ② 받은 요청 탭에 들어오면 called=false로 '전화 안 건 사람'을 먼저 보여주고, 상단 전환으로 called=true를 조회합니다.

          ③ 하단 탭 배지에는 called=false 응답의 **requests** 개수를 쓰면 됩니다.

          **정렬 · 개수**

          ① 요청을 받은 시각이 최신인 순이고, 같으면 **requestId**가 큰 순입니다.

          ② 페이지네이션 없이 전체를 반환합니다. 받은 요청이 없으면 빈 배열입니다.

          **응답 참고**

          ① **phoneNumber**는 하이픈 없이 숫자만 내려갑니다. 전화 걸기는 tel: 링크로 연결합니다.

          ② **distance**는 미터 단위이며 100m 단위로 올린 값입니다.

          ③ **message**가 없으면 null이며, 키는 항상 내려갑니다. 목록에서는 한 줄로 줄이고 사진 크게 보기에서 전문을 보여줍니다.
          """)
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "조회 성공 (받은 요청이 없어도 200)",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "받은 요청이 있음 (전체)",
                      value = """
                          {
                            "success": true,
                            "code": "GLB-S001",
                            "message": "요청이 성공했습니다.",
                            "data": {
                              "requests": [
                                {
                                  "requestId": 15,
                                  "status": "PENDING",
                                  "message": "산책 같이 하실래요?",
                                  "createdAt": "2026-11-21T14:30:00",
                                  "requester": {
                                    "userId": 4,
                                    "nickname": "김영희",
                                    "gender": "FEMALE",
                                    "age": 67,
                                    "profileImageUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/xxx.jpg",
                                    "phoneNumber": "01012345678",
                                    "distance": 500
                                  }
                                },
                                {
                                  "requestId": 9,
                                  "status": "CONNECTED",
                                  "message": null,
                                  "createdAt": "2026-11-21T10:05:00",
                                  "requester": {
                                    "userId": 2,
                                    "nickname": "박철수",
                                    "gender": "MALE",
                                    "age": 71,
                                    "profileImageUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/yyy.jpg",
                                    "phoneNumber": "01098765432",
                                    "distance": 2300
                                  }
                                }
                              ]
                            }
                          }
                          """),
                  @ExampleObject(
                      name = "받은 요청이 없음",
                      value = """
                          {
                            "success": true,
                            "code": "GLB-S001",
                            "message": "요청이 성공했습니다.",
                            "data": { "requests": [] }
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
                      name = "USR-E003 온보딩 미완료",
                      value = """
                          {
                            "success": false,
                            "code": "USR-E003",
                            "message": "온보딩을 완료하지 않은 유저입니다."
                          }
                          """),
                  @ExampleObject(
                      name = "GLB-E001 X-User-Id 헤더 누락·숫자 아님",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E001",
                            "message": "잘못된 요청입니다."
                          }
                          """),
                  @ExampleObject(
                      name = "GLB-E001 called가 true/false가 아님",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E001",
                            "message": "잘못된 요청입니다."
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
  BaseResponse<ReceivedRequestListResponse> getReceived(
      @Parameter(
          name = "X-User-Id",
          in = ParameterIn.HEADER,
          required = true,
          description = "로그인 응답의 userId (요청을 받은 사람)",
          example = "1")
      Long userId,
      @Parameter(
          name = "called",
          in = ParameterIn.QUERY,
          description = "true: 전화 건 사람(CONNECTED)만, false: 전화 안 건 사람(PENDING)만, 생략: 전체",
          example = "false")
      Boolean called);
}
