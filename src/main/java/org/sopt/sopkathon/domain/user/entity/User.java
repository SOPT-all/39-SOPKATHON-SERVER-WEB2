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
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.sopkathon.domain.user.code.UserErrorCode;
import org.sopt.sopkathon.global.common.entity.BaseEntity;
import org.sopt.sopkathon.global.exception.BaseException;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "users",
    indexes = @Index(name = "idx_users_location", columnList = "latitude, longitude"))
public class User extends BaseEntity {

  private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^01\\d{8,9}$");

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 전화 요청 시 상대에게 공개된다. */
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

  /** 로그인 시점에는 비어 있고 온보딩에서 채운다. */
  @Column(precision = 9, scale = 6)
  private BigDecimal latitude;

  @Column(precision = 9, scale = 6)
  private BigDecimal longitude;

  @Column(name = "onboarded_at")
  private LocalDateTime onboardedAt;

  private User(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public static User create(String phoneNumber) {
    return new User(normalizePhoneNumber(phoneNumber));
  }

  /** 숫자만 남겨 형식을 통일한다. 저장뿐 아니라 조회할 때도 이 값을 사용해야 같은 회원으로 찾는다. */
  public static String normalizePhoneNumber(String phoneNumber) {
    String digits = phoneNumber == null ? "" : phoneNumber.replaceAll("\\D", "");
    if (!PHONE_NUMBER_PATTERN.matcher(digits).matches()) {
      throw new BaseException(UserErrorCode.INVALID_PHONE_NUMBER);
    }
    return digits;
  }

  public boolean isOnboarded() {
    return onboardedAt != null;
  }

  public boolean hasLocation() {
    return latitude != null && longitude != null;
  }

  public void completeOnboarding(OnboardingProfile profile) {
    if (isOnboarded()) {
      throw new BaseException(UserErrorCode.ALREADY_ONBOARDED);
    }
    this.nickname = profile.nickname();
    this.gender = profile.gender();
    this.age = profile.age();
    this.profileImageUrl = profile.profileImageUrl();
    this.latitude = profile.latitude();
    this.longitude = profile.longitude();
    this.onboardedAt = LocalDateTime.now();
  }
}
