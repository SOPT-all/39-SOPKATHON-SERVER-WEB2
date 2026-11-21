package org.sopt.sopkathon.domain.callrequest.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.callrequest.code.CallRequestErrorCode;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestCreateRequest;
import org.sopt.sopkathon.domain.callrequest.dto.CallRequestCreateResponse;
import org.sopt.sopkathon.domain.callrequest.dto.ReceivedRequestListResponse;
import org.sopt.sopkathon.domain.callrequest.dto.ReceivedRequestResponse;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequestStatus;
import org.sopt.sopkathon.domain.callrequest.repository.CallRequestRepository;
import org.sopt.sopkathon.domain.match.service.MatchSkipService;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.service.UserService;
import org.sopt.sopkathon.global.common.util.DistanceCalculator;
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

  /** called가 null이면 전체, true면 통화 완료, false면 통화 전만 조회한다. 거리는 fetch join한 requester 좌표로 계산해 추가 쿼리가 없다. */
  @Transactional(readOnly = true)
  public ReceivedRequestListResponse getReceived(Long userId, Boolean called) {
    User me = userService.getOnboardedUser(userId);
    CallRequestStatus status =
        called == null ? null : (called ? CallRequestStatus.CONNECTED : CallRequestStatus.PENDING);

    List<ReceivedRequestResponse> requests =
        callRequestRepository.findReceived(me.getId(), status).stream()
            .map(callRequest -> ReceivedRequestResponse.from(
                callRequest, displayDistance(me, callRequest.getRequester())))
            .toList();
    return ReceivedRequestListResponse.of(requests);
  }

  @Transactional(readOnly = true)
  public List<Long> findPartnerIds(Long userId) {
    return callRequestRepository.findPartnerIds(userId);
  }

  private int displayDistance(User me, User other) {
    return DistanceCalculator.toDisplayDistance(DistanceCalculator.distanceInMeters(
        me.getLatitude(), me.getLongitude(), other.getLatitude(), other.getLongitude()));
  }
}
