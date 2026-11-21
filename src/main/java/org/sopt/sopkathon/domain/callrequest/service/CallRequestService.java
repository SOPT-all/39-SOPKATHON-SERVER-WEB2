package org.sopt.sopkathon.domain.callrequest.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.callrequest.code.CallRequestErrorCode;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestConnectResponse;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestCreateRequest;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestCreateResponse;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.sopt.sopkathon.domain.callrequest.repository.CallRequestRepository;
import org.sopt.sopkathon.domain.match.service.MatchSkipService;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.service.UserService;
import org.sopt.sopkathon.global.exception.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CallRequestService {

  private final CallRequestRepository callRequestRepository;
  private final UserService userService;
  private final MatchSkipService matchSkipService;

  /**
   * 같은 방향 요청이 동시에 들어와 둘 다 중복 확인을 통과하면 uq_call_request_pair가 막고 409로 응답한다. 반대 방향(A→B, B→A)이
   * 동시에 들어오는 경우는 막지 못하며, 해커톤 범위에서는 허용한다.
   */
  @Transactional
  public CallRequestCreateResponse send(Long requesterId, CallRequestCreateRequest request) {
    Long receiverId = request.receiverId();
    if (requesterId.equals(receiverId)) {
      throw new BaseException(CallRequestErrorCode.CANNOT_REQUEST_SELF);
    }
    User requester = userService.getOnboardedUser(requesterId);
    User receiver = userService.getOnboardedTarget(receiverId);

    // 매칭 화면에는 다른 성별·넘기지 않은 상대만 나오지만, API를 직접 호출하는 경우를 막는다.
    if (requester.getGender() == receiver.getGender()) {
      throw new BaseException(CallRequestErrorCode.SAME_GENDER);
    }
    if (matchSkipService.hasSkipped(requesterId, receiverId)) {
      throw new BaseException(CallRequestErrorCode.SKIPPED_TARGET);
    }
    if (callRequestRepository.existsBetween(requesterId, receiverId)) {
      throw new BaseException(CallRequestErrorCode.ALREADY_REQUESTED);
    }
    CallRequest saved =
        callRequestRepository.save(CallRequest.create(requester, receiver, request.message()));
    return CallRequestCreateResponse.from(saved);
  }

  /** 이미 CONNECTED여도 에러 없이 성공한다 (다시 전화하거나 여러 번 눌러도 결과가 같음). */
  @Transactional
  public CallRequestConnectResponse connect(Long userId, Long requestId) {
    CallRequest callRequest = callRequestRepository
        .findByIdAndReceiverId(requestId, userId)
        .orElseThrow(() -> new BaseException(CallRequestErrorCode.CALL_REQUEST_NOT_FOUND));
    callRequest.connect();
    return CallRequestConnectResponse.from(callRequest);
  }

  @Transactional(readOnly = true)
  public List<Long> findPartnerIds(Long userId) {
    return callRequestRepository.findPartnerIds(userId);
  }
}
