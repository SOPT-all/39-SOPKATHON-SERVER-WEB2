package org.sopt.sopkathon.domain.user.repository;

import java.util.List;
import java.util.Optional;
import org.sopt.sopkathon.domain.user.entity.Gender;
import org.sopt.sopkathon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByPhoneNumber(String phoneNumber);

  /**
   * 매칭 후보를 한 번에 조회한다. 나 자신, 온보딩 미완료, 같은 성별, 내가 넘긴 상대, 요청을 주고받은 상대(양방향)는 제외한다. 거리
   * 계산·정렬은 서비스에서 한다.
   */
  @Query(
      """
      select u from User u
      where u.id <> :userId
        and u.onboardedAt is not null
        and u.gender <> :gender
        and not exists (
          select 1 from MatchSkip s
          where s.user.id = :userId and s.target.id = u.id)
        and not exists (
          select 1 from CallRequest c
          where (c.requester.id = :userId and c.receiver.id = u.id)
             or (c.requester.id = u.id and c.receiver.id = :userId))
      """)
  List<User> findMatchCandidates(@Param("userId") Long userId, @Param("gender") Gender gender);
}
