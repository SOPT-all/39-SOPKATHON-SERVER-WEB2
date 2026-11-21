package org.sopt.sopkathon.domain.match.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.global.common.entity.BaseEntity;

/** "다음 사람 보기"로 넘긴 기록. 이미 본 사람을 다시 보여주지 않기 위해 사용한다. */
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "match_skips",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_match_skip_pair",
            columnNames = {"user_id", "target_id"}))
public class MatchSkip extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** "다음 사람 보기"를 누른 사람 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  /** 넘긴 상대 */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User target;
}
