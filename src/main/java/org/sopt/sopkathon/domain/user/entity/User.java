package org.sopt.sopkathon.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.sopt.sopkathon.global.common.entity.BaseEntity;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "users",
    indexes = @Index(name = "idx_users_location", columnList = "latitude, longitude"))
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 입장 키. 전화 요청 시 상대에게 공개된다. */
  @Column(name = "phone_number", nullable = false, unique = true, length = 20)
  private String phoneNumber;

  @Column(length = 30)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private Gender gender;

  private Integer age;

  @Column(name = "profile_image_url", length = 500)
  private String profileImageUrl;

  /** 로그인 시점에는 비어 있고 온보딩에서 필수로 받는다. 좌표만 저장한다. */
  @Column(precision = 9, scale = 6)
  private BigDecimal latitude;

  @Column(precision = 9, scale = 6)
  private BigDecimal longitude;

  /** null이면 온보딩 미완료. */
  @Column(name = "onboarded_at")
  private LocalDateTime onboardedAt;

  public boolean isOnboarded() {
    return onboardedAt != null;
  }

  public void completeOnboarding(
      String nickname,
      Gender gender,
      Integer age,
      String profileImageUrl,
      BigDecimal latitude,
      BigDecimal longitude) {
    this.nickname = nickname;
    this.gender = gender;
    this.age = age;
    this.profileImageUrl = profileImageUrl;
    this.latitude = latitude;
    this.longitude = longitude;
    this.onboardedAt = LocalDateTime.now();
  }
}
