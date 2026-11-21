package org.sopt.sopkathon.domain.place.service;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.place.dto.PlaceListResponse;
import org.sopt.sopkathon.domain.place.dto.PlaceResponse;
import org.sopt.sopkathon.domain.place.entity.Place;
import org.sopt.sopkathon.domain.place.repository.PlaceRepository;
import org.sopt.sopkathon.domain.user.entity.User;
import org.sopt.sopkathon.domain.user.service.UserService;
import org.sopt.sopkathon.global.common.util.DistanceCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

  private final UserService userService;
  private final PlaceRepository placeRepository;

  /** 장소 수가 적어 활성 장소 전체를 조회해 서버에서 거리 계산·정렬한 뒤 해당 페이지만 자른다. */
  public PlaceListResponse getNearbyPlaces(Long userId, int page, int size) {
    User me = userService.getOnboardedUser(userId);

    List<PlaceResponse> sorted =
        placeRepository.findAllByActiveTrue().stream()
            .map(place -> PlaceResponse.from(place, distanceBetween(me, place)))
            .sorted(
                Comparator.comparingInt(PlaceResponse::distance)
                    .thenComparing(PlaceResponse::placeId))
            .toList();

    // page가 아주 크면 page * size가 int 범위를 넘을 수 있어 long으로 계산
    long from = (long) page * size;
    if (from >= sorted.size()) {
      return PlaceListResponse.of(List.of(), page, size, false);
    }
    int to = (int) Math.min(from + size, sorted.size());
    return PlaceListResponse.of(sorted.subList((int) from, to), page, size, to < sorted.size());
  }

  private int distanceBetween(User me, Place place) {
    return DistanceCalculator.distanceInMeters(
        me.getLatitude(), me.getLongitude(), place.getLatitude(), place.getLongitude());
  }
}
