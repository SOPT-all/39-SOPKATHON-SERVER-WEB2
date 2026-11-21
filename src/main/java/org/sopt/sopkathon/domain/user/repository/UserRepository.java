package org.sopt.sopkathon.domain.user.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.sopt.sopkathon.domain.user.entity.Gender;
import org.sopt.sopkathon.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByPhoneNumber(String phoneNumber);

  /** 같은 회원의 동시 온보딩을 한 번만 성공시키기 위한 비관적 락 조회 */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from User u where u.id = :userId")
  Optional<User> findByIdForUpdate(@Param("userId") Long userId);

  /** excludedIds는 비어 있으면 안 된다 (빈 IN 절). */
  @Query(
      """
      select u from User u
      where u.onboardedAt is not null
        and u.latitude is not null
        and u.longitude is not null
        and u.gender = :gender
        and u.id not in :excludedIds
      """)
  List<User> findOnboardedByGenderExcluding(
      @Param("gender") Gender gender, @Param("excludedIds") Collection<Long> excludedIds);
}
