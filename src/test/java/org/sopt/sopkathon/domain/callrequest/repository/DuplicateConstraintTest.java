package org.sopt.sopkathon.domain.callrequest.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.domain.callrequest.entity.CallRequest;
import org.sopt.sopkathon.domain.match.entity.MatchSkip;
import org.sopt.sopkathon.domain.match.repository.MatchSkipRepository;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

/** 동시 요청으로 exists 체크를 통과해도 DB 유니크 제약이 중복 저장을 막는지 확인한다. */
@DataJpaTest
class DuplicateConstraintTest {

  @Autowired private UserRepository userRepository;
  @Autowired private CallRequestRepository callRequestRepository;
  @Autowired private MatchSkipRepository matchSkipRepository;

  private User requester;
  private User receiver;

  @BeforeEach
  void setUp() {
    requester = userRepository.save(User.create("01011112222"));
    receiver = userRepository.save(User.create("01033334444"));
  }

  @Test
  void 같은_사람에게_전화_요청을_두_번_저장하면_예외가_발생한다() {
    callRequestRepository.saveAndFlush(CallRequest.create(requester, receiver, null));

    assertThatThrownBy(
            () ->
                callRequestRepository.saveAndFlush(
                    CallRequest.create(requester, receiver, null)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void 같은_사람을_두_번_넘긴_기록을_저장하면_예외가_발생한다() {
    matchSkipRepository.saveAndFlush(MatchSkip.create(requester, receiver));

    assertThatThrownBy(
            () -> matchSkipRepository.saveAndFlush(MatchSkip.create(requester, receiver)))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void 같은_전화번호로_유저를_두_번_저장하면_예외가_발생한다() {
    assertThatThrownBy(() -> userRepository.saveAndFlush(User.create("01011112222")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}
