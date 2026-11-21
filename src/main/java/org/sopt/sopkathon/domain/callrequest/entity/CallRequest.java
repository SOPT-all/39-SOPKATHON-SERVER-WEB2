package org.sopt.sopkathon.domain.callrequest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.global.common.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "call_requests",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_call_request_pair",
            columnNames = {"requester_id", "receiver_id"}),
    indexes = @Index(name = "idx_received_requests", columnList = "receiver_id, created_at"))
public class CallRequest extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 전화 요청을 보낸 사람 (번호를 준 사람) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "requester_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User requester;

  /** 받은 요청 목록에 뜨는 사람 (전화를 거는 사람) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User receiver;

  /** 요청과 함께 보내는 한 줄 메시지 (선택) */
  @Column(length = 100)
  private String message;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CallRequestStatus status = CallRequestStatus.PENDING;

  private CallRequest(User requester, User receiver, String message) {
    this.requester = requester;
    this.receiver = receiver;
    this.message = message;
  }

  public static CallRequest create(User requester, User receiver, String message) {
    return new CallRequest(requester, receiver, message);
  }

  /** 받은 사람이 전화를 건 뒤 호출한다. 이미 통화한 상대에게 다시 걸어도 CONNECTED가 유지된다. */
  public void connect() {
    this.status = CallRequestStatus.CONNECTED;
  }
}
