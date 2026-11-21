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

  @Operation(
      summary = "근처 장소 리스트 조회",
      description = """
          내 위치(온보딩 때 저장한 좌표)에서 **가까운 순**으로 제휴 장소를 조회합니다. 위치는 보내지 않습니다.

          ### 무한스크롤
          **page=0**으로 시작해서, **hasNext가 true일 때만 page + 1**로 다음 페이지를 요청합니다.

          거리가 같으면 placeId 순이라 페이지를 넘겨도 순서가 바뀌지 않습니다. 반경 제한은 없습니다.

          ### 참고
          imageUrl, address, benefitText, naverMapUrl은 **없으면 null**로 내려옵니다. distance는 미터 단위이고 **100m 단위로 올림**합니다 (예: 350m → 400).
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
                                  "placeId": 3,
                                  "name": "소소한 찻집",
                                  "imageUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/place/xxx.jpg",
                                  "address": "서울시 마포구 와우산로 00",
                                  "latitude": 37.551234,
                                  "longitude": 126.923456,
                                  "distance": 400,
                                  "benefitText": "시니어 커플 10% 할인",
                                  "naverMapUrl": "https://naver.me/xxxx"
                                },
                                {
                                  "placeId": 7,
                                  "name": "공원 산책로 벤치 카페",
                                  "imageUrl": null,
                                  "address": "서울시 마포구 양화로 00",
                                  "latitude": 37.554321,
                                  "longitude": 126.920001,
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
                      name = "GLB-E001 헤더 누락, page·size 범위 밖",
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
          description = "페이지 번호 (0부터, 기본값 0)",
          schema = @Schema(type = "integer", minimum = "0", defaultValue = "0"))
      @Min(0) int page,
      @Parameter(
          name = "size",
          in = ParameterIn.QUERY,
          description = "한 페이지당 장소 수 (1 ~ 50, 기본값 20)",
          schema = @Schema(type = "integer", minimum = "1", maximum = "50", defaultValue = "20"))
      @Min(1) @Max(50) int size);
}
