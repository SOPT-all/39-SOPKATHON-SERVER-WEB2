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

  /** 하이픈 등을 제거한 휴대폰 번호 (010xxxxxxxx, 011xxxxxxx 등) */
  private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^01\\d{8,9}$");

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

  private User(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public static User create(String phoneNumber) {
    return new User(normalizePhoneNumber(phoneNumber));
  }

  /**
   * 숫자만 남겨 저장 형식을 통일한다. 010-1234-5678과 01012345678이 같은 유저로 취급되도록, 저장뿐 아니라 조회
   * ({@code findByPhoneNumber})할 때도 이 메서드를 거친 값을 사용한다.
   */
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

  /** 거리 계산에 필요한 좌표가 모두 있는지. */
  public boolean hasLocation() {
    return latitude != null && longitude != null;
  }

  /**
   * 온보딩 정보를 저장하고 완료 처리한다. 이미 온보딩한 회원은 다시 할 수 없다. 값의 필수 여부는 {@link OnboardingProfile}이
   * 보장한다.
   */
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
