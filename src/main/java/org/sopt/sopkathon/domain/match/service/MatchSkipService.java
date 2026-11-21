package org.sopt.sopkathon.domain.match.service;

import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.match.repository.MatchSkipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 다른 도메인에서 넘긴 기록을 조회할 때 쓰는 창구. MatchService가 CallRequestService에 의존하므로, CallRequestService가
 * MatchService를 주입하면 순환 의존이 생겨 따로 둔다.
 */
@Service
@RequiredArgsConstructor
public class MatchSkipService {

  private final MatchSkipRepository matchSkipRepository;

  @Transactional(readOnly = true)
  public boolean hasSkipped(Long userId, Long targetId) {
    return matchSkipRepository.existsByUserIdAndTargetId(userId, targetId);
  }
}
