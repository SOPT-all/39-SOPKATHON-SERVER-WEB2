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

  @org.junit.jupiter.params.ParameterizedTest
  @org.junit.jupiter.params.provider.CsvSource({"0, 0", "1, 100", "100, 100", "101, 200", "1112, 1200"})
  void 응답용_거리는_100m_단위로_올림한다(int meters, int expected) {
    assertThat(DistanceCalculator.toDisplayDistance(meters)).isEqualTo(expected);
  }
}
