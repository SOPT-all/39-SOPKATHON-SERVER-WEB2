package org.sopt.sopkathon.domain.callrequest.repository;

import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallRequestRepository extends JpaRepository<CallRequest, Long> {

  boolean existsByRequesterIdAndReceiverId(Long requesterId, Long receiverId);
}
