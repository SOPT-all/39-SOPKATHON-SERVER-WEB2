package org.sopt.sopkathon.global.common.util;

import java.math.BigDecimal;

/** Haversine 공식으로 두 좌표 사이의 직선 거리를 계산한다. */
public final class DistanceCalculator {

  private static final double EARTH_RADIUS_METERS = 6_371_000;

  private DistanceCalculator() {}

  public static int distanceInMeters(
      BigDecimal latitude1, BigDecimal longitude1, BigDecimal latitude2, BigDecimal longitude2) {
    double lat1 = Math.toRadians(latitude1.doubleValue());
    double lat2 = Math.toRadians(latitude2.doubleValue());
    double deltaLat = lat2 - lat1;
    double deltaLon = Math.toRadians(longitude2.doubleValue() - longitude1.doubleValue());

    double a =
        Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
            + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return (int) Math.round(EARTH_RADIUS_METERS * c);
  }
}
