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
import lombok.experimental.SuperBuilder;
import org.sopt.sopkathon.global.common.entity.BaseEntity;

/** 운영자가 미리 등록한 제휴 장소. 사용자 등록 기능은 없다. */
@Getter
@SuperBuilder
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

  /** 제휴 혜택 문구. 혜택이 없으면 null */
  @Column(name = "benefit_text")
  private String benefitText;

  @Column(name = "naver_map_url", length = 500)
  private String naverMapUrl;

  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private boolean active = true;
}
