package org.sopt.sopkathon.domain.callrequest.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.callrequest.code.CallRequestErrorCode;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestCreateRequest;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestCreateResponse;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.sopt.sopkathon.domain.callrequest.repository.CallRequestRepository;
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

    if (callRequestRepository.existsByRequesterIdAndReceiverId(requesterId, receiverId)
        || callRequestRepository.existsByRequesterIdAndReceiverId(receiverId, requesterId)) {
      throw new BaseException(CallRequestErrorCode.ALREADY_REQUESTED);
    }
    CallRequest saved =
        callRequestRepository.save(CallRequest.create(requester, receiver, request.message()));
    return CallRequestCreateResponse.from(saved);
  }

  @Transactional(readOnly = true)
  public List<Long> findPartnerIds(Long userId) {
    return callRequestRepository.findPartnerIds(userId);
  }
}
