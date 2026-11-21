package org.sopt.sopkathon.domain.place.controller;

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
import org.sopt.sopkathon.domain.place.dto.PlaceListResponse;
import org.sopt.sopkathon.global.response.BaseResponse;

/** 파라미터 검증 제약(@Min, @Max)은 여기에만 선언한다. 구현 메서드에서는 제약을 추가할 수 없다. */
@Tag(name = "Place", description = "근처 제휴 장소")
public interface PlaceApi {

  // 기본값·범위는 컨트롤러의 @RequestParam과 Swagger 문서가 함께 참조한다
  String DEFAULT_PAGE = "0";
  String DEFAULT_SIZE = "20";
  int MAX_SIZE = 50;

  @Operation(
      summary = "근처 장소 리스트 조회",
      description = """
          내 위치(온보딩 때 저장한 좌표)에서 **가까운 순**으로 제휴 장소를 조회합니다. 위치는 보내지 않습니다.

          ### 무한스크롤
          **page=0**부터 시작해 **hasNext가 true일 때만 page + 1**을 요청하고, 받은 목록을 뒤에 이어 붙입니다.

          요청 중에는 다음 요청을 막고, 이어 붙일 때 **placeId로 중복을 걸러** 주세요.

          ### 거리
          distance는 **100m 단위로 올림**한 값입니다 (350m → 400). 정렬은 실제 거리 기준이라 앞에 있는 장소가 더 가깝습니다.
          """)
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "조회 성공. 장소가 없거나 마지막 페이지를 넘으면 빈 목록 + hasNext: false",
          content = @Content(
              mediaType = "application/json",
              examples = {
                  @ExampleObject(
                      name = "장소 있음",
                      value = """
                          {
                            "success": true,
                            "code": "GLB-S001",
                            "message": "요청이 성공했습니다.",
                            "data": {
                              "places": [
                                {
                                  "placeId": 1,
                                  "name": "인사동찻집",
                                  "imageUrl": "https://spokerthon-39.s3.ap-northeast-2.amazonaws.com/dummy/places/tea_01.png",
                                  "address": "서울 종로구 인사동길 33-1",
                                  "distance": 400,
                                  "benefitText": "시니어 커플 방문 시 전통차 10% 할인",
                                  "naverMapUrl": "https://map.naver.com/p/entry/place/13151902"
                                },
                                {
                                  "placeId": 7,
                                  "name": "공원 산책로 벤치 카페",
                                  "imageUrl": null,
                                  "address": null,
                                  "distance": 1200,
                                  "benefitText": null,
                                  "naverMapUrl": null
                                }
                              ],
                              "page": 0,
                              "size": 20,
                              "hasNext": true
                            }
                          }
                          """),
                  @ExampleObject(
                      name = "더 이상 장소 없음",
                      value = """
                          {
                            "success": true,
                            "code": "GLB-S001",
                            "message": "요청이 성공했습니다.",
                            "data": { "places": [], "page": 3, "size": 20, "hasNext": false }
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
                      name = "GLB-E001 헤더 누락, page·size 범위 밖 (개발 중에만 발생)",
                      value = """
                          {
                            "success": false,
                            "code": "GLB-E001",
                            "message": "잘못된 요청입니다."
                          }
                          """),
                  @ExampleObject(
                      name = "USR-E003 온보딩 미완료 → 온보딩 화면으로 이동",
                      value = """
                          {
                            "success": false,
                            "code": "USR-E003",
                            "message": "온보딩을 완료하지 않은 유저입니다."
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
          responseCode = "500",
          description = "서버 내부 오류 → 잠시 후 다시 시도해주세요",
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
  BaseResponse<PlaceListResponse> getNearbyPlaces(
      @Parameter(
          name = "X-User-Id",
          in = ParameterIn.HEADER,
          required = true,
          description = "로그인 응답의 userId",
          example = "1")
      Long userId,
      @Parameter(
          name = "page",
          in = ParameterIn.QUERY,
          description = "페이지 번호 (0부터, 기본값 " + DEFAULT_PAGE + ")",
          schema = @Schema(type = "integer", minimum = "0", defaultValue = DEFAULT_PAGE))
      @Min(0) int page,
      @Parameter(
          name = "size",
          in = ParameterIn.QUERY,
          description = "한 페이지당 장소 수 (1 ~ " + MAX_SIZE + ", 기본값 " + DEFAULT_SIZE + ")",
          schema = @Schema(
              type = "integer", minimum = "1", maximum = "" + MAX_SIZE, defaultValue = DEFAULT_SIZE))
      @Min(1) @Max(MAX_SIZE) int size);
}
