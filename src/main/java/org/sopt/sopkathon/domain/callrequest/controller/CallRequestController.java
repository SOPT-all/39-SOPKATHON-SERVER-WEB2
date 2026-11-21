package org.sopt.sopkathon.domain.callrequest.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestConnectResponse;
import org.sopt.sopkathon.domain.callrequest.service.CallRequestService;
import org.sopt.sopkathon.global.common.code.GlobalSuccessCode;
import org.sopt.sopkathon.global.response.BaseResponse;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/call-requests")
public class CallRequestController implements CallRequestApi {

  private final CallRequestService callRequestService;

  @Override
  @PatchMapping("/{requestId}/connect")
  public BaseResponse<CallRequestConnectResponse> connect(
      @RequestHeader("X-User-Id") Long userId,
      @PathVariable Long requestId) {
    return BaseResponse.success(GlobalSuccessCode.OK, callRequestService.connect(userId, requestId));
  }
}
