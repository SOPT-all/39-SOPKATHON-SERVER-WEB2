package org.sopt.sopkathon.domain.match.service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.callrequest.service.CallRequestService;
import org.sopt.sopkathon.domain.match.code.MatchErrorCode;
import org.sopt.sopkathon.domain.match.dto.MatchListResponse;
import org.sopt.sopkathon.domain.match.dto.MatchUserResponse;
import org.sopt.sopkathon.domain.match.entity.MatchSkip;
import org.sopt.sopkathon.domain.match.repository.MatchSkipRepository;
import org.sopt.sopkathon.domain.user.entity.Gender;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.service.UserService;
import org.sopt.sopkathon.global.common.util.DistanceCalculator;
import org.sopt.sopkathon.global.exception.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchService {

  private final UserService userService;
  private final CallRequestService callRequestService;
  private final MatchSkipRepository matchSkipRepository;

  /** 해커톤 규모라 후보 전체를 조회해 서버에서 거리 계산·정렬한다. 회원이 많아지면 쿼리로 옮긴다. */
  public MatchListResponse getMatches(Long userId, int limit) {
    User me = userService.getOnboardedUser(userId);

    List<MatchUserResponse> sorted =
        userService.findOnboardedByGenderExcluding(oppositeOf(me.getGender()), excludedIds(me))
            .stream()
            .map(candidate -> new Candidate(candidate, distanceBetween(me, candidate)))
            .sorted(
                Comparator.comparingInt(Candidate::distance)
                    .thenComparing(candidate -> candidate.user().getId()))
            .map(candidate -> MatchUserResponse.from(
                candidate.user(), DistanceCalculator.toDisplayDistance(candidate.distance())))
            .toList();

    boolean hasNext = sorted.size() > limit;
    return MatchListResponse.of(sorted.subList(0, Math.min(limit, sorted.size())), hasNext);
  }

  /** 동시에 같은 요청이 들어와 둘 다 중복 확인을 통과하면 유니크 제약이 막고 409로 응답한다. */
  @Transactional
  public void skip(Long userId, Long targetId) {
    if (userId.equals(targetId)) {
      throw new BaseException(MatchErrorCode.CANNOT_SKIP_SELF);
    }
    User me = userService.getOnboardedUser(userId);
    User target = userService.getUser(targetId);

    if (matchSkipRepository.existsByUserIdAndTargetId(me.getId(), target.getId())) {
      return;
    }
    matchSkipRepository.save(MatchSkip.create(me, target));
  }

  private Set<Long> excludedIds(User me) {
    Set<Long> excludedIds = new HashSet<>();
    excludedIds.add(me.getId());
    excludedIds.addAll(matchSkipRepository.findTargetIdsByUserId(me.getId()));
    excludedIds.addAll(callRequestService.findPartnerIds(me.getId()));
    return excludedIds;
  }

  private Gender oppositeOf(Gender gender) {
    return gender == Gender.MALE ? Gender.FEMALE : Gender.MALE;
  }

  private record Candidate(User user, int distance) {}

  private int distanceBetween(User me, User other) {
    return DistanceCalculator.distanceInMeters(
        me.getLatitude(), me.getLongitude(), other.getLatitude(), other.getLongitude());
  }
}
