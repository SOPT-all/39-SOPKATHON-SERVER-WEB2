package org.sopt.sopkathon.domain.match.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.sopt.sopkathon.domain.match.dto.MatchListResponse;
import org.sopt.sopkathon.global.response.BaseResponse;

/** 파라미터 검증 제약(@Min, @Max)은 여기에만 선언한다. 구현 메서드에서는 제약을 추가할 수 없다. */
@Tag(name = "Match", description = "매칭 상대 조회 · 넘기기")
public interface MatchApi {

  String DEFAULT_LIMIT = "10";
  int MAX_LIMIT = 50;

  @Operation(
      summary = "매칭 상대 조회",
      description = """
          매칭(S-003)에서 내 위치를 기준으로 **가까운 순서대로 매칭 상대를 최대 limit명** 조회합니다.
          기준 위치는 온보딩 때 저장한 좌표라 위치를 보낼 필요가 없습니다.

          **정렬**

          ① 직선 거리가 가까운 순이고, 거리가 같으면 **userId**가 작은 순입니다.

          ② 반경 제한은 없습니다.

          **제외 대상**

          ① 나 자신, 온보딩을 완료하지 않은 회원

          ② 나와 같은 성별인 회원 (다른 성별만 조회)

          ③ 내가 넘긴 회원 (POST /api/v1/matches/{targetId}/skip)

          ④ 요청을 주고받은 회원 (내가 보냈거나, 상대가 나에게 보낸 경우 모두)

          **화면에서 쓰는 방법**

          ① 받은 **users**를 목록으로 들고 있으면서 맨 앞 1명만 카드로 보여줍니다.

          ② 다음 사람 보기 또는 전화 요청을 하면 목록에서 그 사람을 빼고 다음 사람을 보여줍니다.

          ③ 목록을 다 쓰면 **hasNext**로 다음 동작을 정합니다. true면 이 API를 다시 호출, false면 빈 화면('아직 근처에 만날 분이 없어요')

          넘기거나 요청한 상대는 서버에서 바로 빠지므로, 페이지 번호 없이 다시 호출하면 다음 상대들이 옵니다.
          목록이 남아 있을 때 다시 호출하면 아직 넘기지 않은 상대가 또 포함되니, 목록을 다 쓴 뒤에만 다시 호출해 주세요.

          **응답 참고**

          ① 상대의 전화번호와 좌표는 내려주지 않습니다.

          ② **distance**는 미터 단위이고 **100m 단위로 올림**합니다 (예: 750m → 800). 표시 형식(0.8km 등)은 프론트에서 변환합니다.
          """)
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "조회 성공 (보여줄 상대가 없어도 200)",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "상대가 있음",
                      value = """
                          {
                            "success": true,
                            "code": "GLB-S001",
                            "message": "요청이 성공했습니다.",
                            "data": {
                              "users": [
                                {
                                  "userId": 7,
                                  "nickname": "민지",
                                  "age": 24,
                                  "profileImageUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/xxx.jpg",
                                  "distance": 800
                                },
                                {
                                  "userId": 12,
                                  "nickname": "서연",
                                  "age": 26,
                                  "profileImageUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/profile/yyy.jpg",
                                  "distance": 1500
                                }
                              ],
                              "hasNext": true
                            }
                          }
                          """),
                  @ExampleObject(
                      name = "보여줄 상대가 없음",
                      value = """
                          {
                            "success": true,
                            "code": "GLB-S001",
                            "message": "요청이 성공했습니다.",
                            "data": { "users": [], "hasNext": false }
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
                      name = "GLB-E001 limit 범위 밖 (1 ~ " + MAX_LIMIT + ")",
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
  BaseResponse<MatchListResponse> getMatches(
      @Parameter(
          name = "X-User-Id",
          in = ParameterIn.HEADER,
          required = true,
          description = "로그인 응답의 userId",
          example = "1")
      Long userId,
      @Parameter(
          name = "limit",
          in = ParameterIn.QUERY,
          description = "한 번에 받을 상대 수 (1 ~ " + MAX_LIMIT + ", 기본값 " + DEFAULT_LIMIT + ")",
          schema = @Schema(
              type = "integer", minimum = "1", maximum = "" + MAX_LIMIT, defaultValue = DEFAULT_LIMIT))
      @Min(1) @Max(MAX_LIMIT) int limit);
}
