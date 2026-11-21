package org.sopt.sopkathon.domain.callrequest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestConnectResponse;
import org.sopt.sopkathon.global.response.BaseResponse;

@Tag(name = "CallRequest", description = "전화 요청")
public interface CallRequestApi {

  @Operation(
      summary = "전화 완료",
      description = """
          받은 요청 → 전화 걸기 → "통화하셨나요?"에서 **예**를 누르면 호출합니다. 요청이 **통화 완료(CONNECTED)** 로 바뀝니다.

          ### 호출 규칙
          **아니요**를 누르면 호출하지 않습니다 (PENDING 유지).

          이미 통화한 상대에게 다시 전화한 경우 호출하지 않아도 되고, 호출해도 **200**입니다.

          **요청을 받은 사람만** 호출할 수 있습니다.
          """)
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "통화 완료 처리 (이미 완료된 요청 포함)",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  value = """
                      {
                        "success": true,
                        "code": "GLB-S001",
                        "message": "요청이 성공했습니다.",
                        "data": { "requestId": 15, "status": "CONNECTED" }
                      }
                      """))),
      @ApiResponse(
          responseCode = "400",
          description = "X-User-Id 누락·숫자 아님, requestId 숫자 아님 (개발 중에만 발생)",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  name = "GLB-E001",
                  value = """
                      {
                        "success": false,
                        "code": "GLB-E001",
                        "message": "잘못된 요청입니다."
                      }
                      """))),
      @ApiResponse(
          responseCode = "403",
          description = "내가 받은 요청이 아님 (보낸 사람이나 다른 회원이 호출)",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  name = "GLB-E003",
                  value = """
                      {
                        "success": false,
                        "code": "GLB-E003",
                        "message": "접근 권한이 없습니다."
                      }
                      """))),
      @ApiResponse(
          responseCode = "404",
          description = "존재하지 않는 요청",
          content = @Content(
              mediaType = "application/json",
              examples = @ExampleObject(
                  name = "REQ-E001",
                  value = """
                      {
                        "success": false,
                        "code": "REQ-E001",
                        "message": "존재하지 않는 전화 요청입니다."
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
  BaseResponse<CallRequestConnectResponse> connect(
      @Parameter(
          name = "X-User-Id",
          in = ParameterIn.HEADER,
          required = true,
          description = "로그인 응답의 userId (요청을 받은 사람)",
          example = "2")
      Long userId,
      @Parameter(
          name = "requestId",
          in = ParameterIn.PATH,
          required = true,
          description = "받은 요청 리스트 응답의 requestId",
          example = "15")
      Long requestId);
}
