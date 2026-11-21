package org.sopt.sopkathon.domain.callrequest.repository;

import java.util.List;
import java.util.Optional;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CallRequestRepository extends JpaRepository<CallRequest, Long> {

  /** 내가 받은 요청만 조회한다. 남의 요청도 없는 요청과 똑같이 비어 있어 요청 존재 여부가 드러나지 않는다. */
  Optional<CallRequest> findByIdAndReceiverId(Long id, Long receiverId);

  /** 두 회원 사이에 어느 방향으로든 요청이 있는지 한 번의 쿼리로 확인한다. */
  @Query(
      """
      select case when count(c) > 0 then true else false end
      from CallRequest c
      where (c.requester.id = :userId and c.receiver.id = :otherId)
         or (c.requester.id = :otherId and c.receiver.id = :userId)
      """)
  boolean existsBetween(@Param("userId") Long userId, @Param("otherId") Long otherId);

  /** 나와 요청을 주고받은 상대의 ID. 내가 보낸 요청이면 받은 사람, 나에게 온 요청이면 보낸 사람. */
  @Query(
      """
      select case when c.requester.id = :userId then c.receiver.id else c.requester.id end
      from CallRequest c
      where c.requester.id = :userId or c.receiver.id = :userId
      """)
  List<Long> findPartnerIds(@Param("userId") Long userId);
}
