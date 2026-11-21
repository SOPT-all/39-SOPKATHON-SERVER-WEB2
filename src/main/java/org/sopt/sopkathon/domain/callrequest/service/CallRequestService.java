package org.sopt.sopkathon.domain.callrequest.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.callrequest.repository.CallRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CallRequestService {

  private final CallRequestRepository callRequestRepository;

  /** 나와 요청을 주고받은 상대의 ID (내가 보냈거나, 나에게 보낸 경우 모두). */
  @Transactional(readOnly = true)
  public List<Long> findPartnerIds(Long userId) {
    return callRequestRepository.findPartnerIds(userId);
  }
}
