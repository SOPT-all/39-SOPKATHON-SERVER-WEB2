package org.sopt.sopkathon.domain.callrequest.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.callrequest.code.CallRequestErrorCode;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestConnectResponse;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.sopt.sopkathon.domain.callrequest.repository.CallRequestRepository;
import org.sopt.sopkathon.global.common.code.GlobalErrorCode;
import org.sopt.sopkathon.global.exception.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CallRequestService {

  private final CallRequestRepository callRequestRepository;

  /** 받은 사람만 통화 완료로 바꿀 수 있다. 이미 CONNECTED여도 그대로 성공한다. */
  @Transactional
  public CallRequestConnectResponse connect(Long userId, Long requestId) {
    CallRequest callRequest = callRequestRepository
        .findById(requestId)
        .orElseThrow(() -> new BaseException(CallRequestErrorCode.CALL_REQUEST_NOT_FOUND));
    if (!callRequest.getReceiver().getId().equals(userId)) {
      throw new BaseException(GlobalErrorCode.FORBIDDEN);
    }
    callRequest.connect();
    return CallRequestConnectResponse.from(callRequest);
  }

  @Transactional(readOnly = true)
  public List<Long> findPartnerIds(Long userId) {
    return callRequestRepository.findPartnerIds(userId);
  }
}
