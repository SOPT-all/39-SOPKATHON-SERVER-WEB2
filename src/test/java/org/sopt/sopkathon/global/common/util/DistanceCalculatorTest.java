package org.sopt.sopkathon.global.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DistanceCalculatorTest {

  @Test
  void 같은_좌표면_0미터다() {
    BigDecimal lat = new BigDecimal("37.550000");
    BigDecimal lon = new BigDecimal("126.920000");

    assertThat(DistanceCalculator.distanceInMeters(lat, lon, lat, lon)).isZero();
  }

  @Test
  void 위도_0_01도_차이는_약_1112미터다() {
    int distance =
        DistanceCalculator.distanceInMeters(
            new BigDecimal("37.550000"),
            new BigDecimal("126.920000"),
            new BigDecimal("37.560000"),
            new BigDecimal("126.920000"));

    assertThat(distance).isEqualTo(1112);
  }
}
