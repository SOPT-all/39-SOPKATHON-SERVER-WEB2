package org.sopt.sopkathon.domain.user.repository;

import java.util.Collection;
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
   * 온보딩을 마치고 좌표가 있는 회원 중 해당 성별인 회원을 조회한다. excludedIds에 담긴 회원은 제외한다. excludedIds는 비어 있으면
   * 안 된다 (빈 IN 절).
   */
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
