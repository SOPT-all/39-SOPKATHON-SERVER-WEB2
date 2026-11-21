package org.sopt.sopkathon.domain.match.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.callrequest.service.CallRequestService;
import org.sopt.sopkathon.domain.match.dto.MatchListResponse;
import org.sopt.sopkathon.domain.match.dto.MatchUserResponse;
import org.sopt.sopkathon.domain.match.repository.MatchSkipRepository;
import org.sopt.sopkathon.domain.user.entity.Gender;
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
  private final CallRequestService callRequestService;
  private final MatchSkipRepository matchSkipRepository;

  /**
   * 가까운 순으로 최대 limit명을 반환한다. 거리가 같으면 userId 오름차순. 회원 수가 적은 해커톤 단계라 후보 전체를 한 번에 조회한 뒤
   * 서버에서 거리를 계산해 정렬하고 자른다.
   */
  public MatchListResponse getMatches(Long userId, int limit) {
    User me = userService.getOnboardedUser(userId);

    List<MatchUserResponse> sorted =
        userService.findOnboardedByGenderExcluding(oppositeOf(me.getGender()), excludedIds(me))
            .stream()
            .map(candidate -> MatchUserResponse.from(candidate, distanceBetween(me, candidate)))
            .sorted(
                Comparator.comparingInt(MatchUserResponse::distance)
                    .thenComparing(MatchUserResponse::userId))
            .toList();

    boolean hasNext = sorted.size() > limit;
    return MatchListResponse.of(sorted.subList(0, Math.min(limit, sorted.size())), hasNext);
  }

  /** 매칭에서 빼는 회원: 나 자신, 내가 넘긴 상대, 요청을 주고받은 상대(양방향). */
  private Set<Long> excludedIds(User me) {
    Set<Long> excludedIds = new HashSet<>();
    excludedIds.add(me.getId());
    excludedIds.addAll(matchSkipRepository.findTargetIdsByUserId(me.getId()));
    excludedIds.addAll(callRequestService.findPartnerIds(me.getId()));
    return excludedIds;
  }

  /** 나와 다른 성별만 매칭한다. */
  private Gender oppositeOf(Gender gender) {
    return gender == Gender.MALE ? Gender.FEMALE : Gender.MALE;
  }

  private int distanceBetween(User me, User other) {
    return DistanceCalculator.distanceInMeters(
        me.getLatitude(), me.getLongitude(), other.getLatitude(), other.getLongitude());
  }
}
