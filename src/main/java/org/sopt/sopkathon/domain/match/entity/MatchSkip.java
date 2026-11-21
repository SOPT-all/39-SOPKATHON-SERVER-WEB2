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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.global.common.entity.BaseEntity;

/** 넘긴 기록. 이미 본 사람을 다시 보여주지 않기 위해 사용한다. */
@Getter
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User target;

  private MatchSkip(User user, User target) {
    this.user = user;
    this.target = target;
  }

  public static MatchSkip create(User user, User target) {
    return new MatchSkip(user, target);
  }
}
