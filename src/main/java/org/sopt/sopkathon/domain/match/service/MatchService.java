package org.sopt.sopkathon.domain.match.service;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.match.dto.MatchListResponse;
import org.sopt.sopkathon.domain.match.dto.MatchUserResponse;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.service.UserService;
import org.sopt.sopkathon.global.common.util.DistanceCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

  private final UserService userService;

  /**
   * 가까운 순으로 최대 limit명을 반환한다. 거리가 같으면 userId 오름차순. 회원 수가 적은 해커톤 단계라 후보 전체를 한 번에 조회한 뒤
   * 서버에서 거리를 계산해 정렬하고 자른다.
   */
  public MatchListResponse getMatches(Long userId, int limit) {
    User me = userService.getOnboardedUser(userId);

    List<MatchUserResponse> sorted =
        userService.findMatchCandidates(me).stream()
            .map(candidate -> MatchUserResponse.from(candidate, distanceBetween(me, candidate)))
            .sorted(
                Comparator.comparingInt(MatchUserResponse::distance)
                    .thenComparing(MatchUserResponse::userId))
            .toList();

    boolean hasNext = sorted.size() > limit;
    return MatchListResponse.of(sorted.subList(0, Math.min(limit, sorted.size())), hasNext);
  }

  private int distanceBetween(User me, User other) {
    return DistanceCalculator.distanceInMeters(
        me.getLatitude(), me.getLongitude(), other.getLatitude(), other.getLongitude());
  }
}
