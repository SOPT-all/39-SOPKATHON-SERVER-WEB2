package org.sopt.sopkathon.domain.place.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.place.dto.PlaceListResponse;
import org.sopt.sopkathon.domain.place.service.PlaceService;
import org.sopt.sopkathon.global.common.code.GlobalSuccessCode;
import org.sopt.sopkathon.global.response.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/places")
public class PlaceController implements PlaceApi {

  private final PlaceService placeService;

  /** page·size 범위 검증(@Min, @Max)은 {@link PlaceApi}에 선언되어 있다. */
  @Override
  @GetMapping
  public BaseResponse<PlaceListResponse> getNearbyPlaces(
      @RequestHeader("X-User-Id") Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return BaseResponse.success(
        GlobalSuccessCode.OK, placeService.getNearbyPlaces(userId, page, size));
  }
}
