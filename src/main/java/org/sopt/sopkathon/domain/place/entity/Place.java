package org.sopt.sopkathon.domain.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.sopkathon.global.common.entity.BaseEntity;

/** 운영자가 미리 등록한 제휴 장소. 사용자 등록 기능은 없다. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "places",
    indexes = @Index(name = "idx_places_location", columnList = "latitude, longitude"))
public class Place extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(name = "image_url", length = 500)
  private String imageUrl;

  private String address;

  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal latitude;

  @Column(nullable = false, precision = 9, scale = 6)
  private BigDecimal longitude;

  @Column(name = "benefit_text")
  private String benefitText;

  @Column(name = "naver_map_url", length = 500)
  private String naverMapUrl;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Builder
  private Place(
      String name,
      String imageUrl,
      String address,
      BigDecimal latitude,
      BigDecimal longitude,
      String benefitText,
      String naverMapUrl) {
    this.name = name;
    this.imageUrl = imageUrl;
    this.address = address;
    this.latitude = latitude;
    this.longitude = longitude;
    this.benefitText = benefitText;
    this.naverMapUrl = naverMapUrl;
  }
}
