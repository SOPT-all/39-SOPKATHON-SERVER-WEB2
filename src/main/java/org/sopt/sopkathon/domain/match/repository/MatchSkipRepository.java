package org.sopt.sopkathon.domain.match.repository;

import org.sopt.sopkathon.domain.match.entity.MatchSkip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchSkipRepository extends JpaRepository<MatchSkip, Long> {

  boolean existsByUserIdAndTargetId(Long userId, Long targetId);
}
