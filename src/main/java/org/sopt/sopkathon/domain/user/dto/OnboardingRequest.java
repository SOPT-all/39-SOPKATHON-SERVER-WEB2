package org.sopt.sopkathon.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.sopt.sopkathon.domain.user.entity.Gender;
import org.sopt.sopkathon.domain.user.entity.OnboardingProfile;

/** 닉네임은 생성 시점에 앞뒤 공백을 제거하므로 길이 검증도 공백을 뺀 값 기준이다. */
@Schema(description = "온보딩 정보 (모든 값 필수)")
public record OnboardingRequest(
    @Schema(description = "이름(닉네임). 앞뒤 공백 제외 1 ~ 10자", example = "순자")
    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 10, message = "이름은 10자 이하여야 합니다.")
    String nickname,

    // enum으로 바로 받으면 잘못된 값이 역직렬화 단계에서 실패해 필드별 메시지를 줄 수 없어 문자열로 받는다.
    @Schema(description = "성별", allowableValues = {"MALE", "FEMALE"}, example = "MALE")
    @NotBlank(message = "성별을 선택해주세요.")
    @Pattern(regexp = "MALE|FEMALE", message = "성별은 MALE 또는 FEMALE이어야 합니다.")
    String gender,

    @Schema(description = "나이 (1 ~ 120)", example = "65")
    @NotNull(message = "나이를 입력해주세요.")
    @Min(value = 1, message = "나이는 1 이상 120 이하여야 합니다.")
    @Max(value = 120, message = "나이는 1 이상 120 이하여야 합니다.")
    Integer age,

    @Schema(description = "위도 (-90 ~ 90, 소수점 6자리까지 저장)", example = "37.551234")
    @NotNull(message = "위치 정보가 필요합니다.")
    @DecimalMin(value = "-90", message = "위도는 -90 이상 90 이하여야 합니다.")
    @DecimalMax(value = "90", message = "위도는 -90 이상 90 이하여야 합니다.")
    BigDecimal latitude,

    @Schema(description = "경도 (-180 ~ 180, 소수점 6자리까지 저장)", example = "126.923456")
    @NotNull(message = "위치 정보가 필요합니다.")
    @DecimalMin(value = "-180", message = "경도는 -180 이상 180 이하여야 합니다.")
    @DecimalMax(value = "180", message = "경도는 -180 이상 180 이하여야 합니다.")
    BigDecimal longitude
) {

  private static final int COORDINATE_SCALE = 6;

  public OnboardingRequest {
    nickname = nickname == null ? null : nickname.strip();
  }

  /** {@code @Valid}를 거친 뒤에만 호출한다 (성별 문자열이 검증됐다는 전제). */
  public OnboardingProfile toProfile(String profileImageUrl) {
    return OnboardingProfile.builder()
        .nickname(nickname)
        .gender(Gender.valueOf(gender))
        .age(age)
        .profileImageUrl(profileImageUrl)
        .latitude(latitude.setScale(COORDINATE_SCALE, RoundingMode.HALF_UP))
        .longitude(longitude.setScale(COORDINATE_SCALE, RoundingMode.HALF_UP))
        .build();
  }
}
