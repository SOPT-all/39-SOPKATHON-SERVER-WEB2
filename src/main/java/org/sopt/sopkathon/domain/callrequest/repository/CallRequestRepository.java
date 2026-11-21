package org.sopt.sopkathon.domain.callrequest.repository;

import java.util.List;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CallRequestRepository extends JpaRepository<CallRequest, Long> {

  /** 두 회원 사이에 어느 방향으로든 요청이 있는지 한 번의 쿼리로 확인한다. */
  @Query(
      """
      select case when count(c) > 0 then true else false end
      from CallRequest c
      where (c.requester.id = :userId and c.receiver.id = :otherId)
         or (c.requester.id = :otherId and c.receiver.id = :userId)
      """)
  boolean existsBetween(@Param("userId") Long userId, @Param("otherId") Long otherId);

  /** 받은 요청 목록. requester를 fetch join으로 함께 가져와 N+1을 막는다. status가 null이면 전체를 조회한다. */
  @Query(
      """
      select c from CallRequest c
      join fetch c.requester
      where c.receiver.id = :receiverId
        and (:status is null or c.status = :status)
      order by c.createdAt desc, c.id desc
      """)
  List<CallRequest> findReceived(
      @Param("receiverId") Long receiverId, @Param("status") CallRequestStatus status);

  /** 나와 요청을 주고받은 상대의 ID. 내가 보낸 요청이면 받은 사람, 나에게 온 요청이면 보낸 사람. */
  @Query(
      """
      select case when c.requester.id = :userId then c.receiver.id else c.requester.id end
      from CallRequest c
      where c.requester.id = :userId or c.receiver.id = :userId
      """)
  List<Long> findPartnerIds(@Param("userId") Long userId);
}
