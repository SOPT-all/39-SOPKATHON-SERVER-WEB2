package org.sopt.sopkathon.domain.callrequest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestConnectResponse;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestCreateRequest;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestCreateResponse;
import org.sopt.sopkathon.domain.callrequest.service.CallRequestService;
import org.sopt.sopkathon.global.common.code.GlobalSuccessCode;
import org.sopt.sopkathon.global.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/call-requests")
public class CallRequestController implements CallRequestApi {

  private final CallRequestService callRequestService;

  @Override
  @PostMapping
  public ResponseEntity<BaseResponse<CallRequestCreateResponse>> send(
      @RequestHeader("X-User-Id") Long userId,
      @Valid @RequestBody CallRequestCreateRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(BaseResponse.success(GlobalSuccessCode.CREATED, callRequestService.send(userId, request)));
  }

  @Override
  @PatchMapping("/{requestId}/connect")
  public BaseResponse<CallRequestConnectResponse> connect(
      @RequestHeader("X-User-Id") Long userId,
      @PathVariable Long requestId) {
    return BaseResponse.success(GlobalSuccessCode.OK, callRequestService.connect(userId, requestId));
  }
}
