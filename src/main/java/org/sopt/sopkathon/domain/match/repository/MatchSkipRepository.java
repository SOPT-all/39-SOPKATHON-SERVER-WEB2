package org.sopt.sopkathon.domain.match.repository;

import java.util.List;
import org.sopt.sopkathon.domain.match.entity.MatchSkip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchSkipRepository extends JpaRepository<MatchSkip, Long> {

  boolean existsByUserIdAndTargetId(Long userId, Long targetId);

  @Query("select s.target.id from MatchSkip s where s.user.id = :userId")
  List<Long> findTargetIdsByUserId(@Param("userId") Long userId);
}
