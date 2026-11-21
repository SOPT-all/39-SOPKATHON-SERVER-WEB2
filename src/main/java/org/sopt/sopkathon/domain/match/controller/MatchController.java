package org.sopt.sopkathon.domain.match.controller;

import lombok.RequiredArgsConstructor;
import org.sopt.sopkathon.domain.match.dto.MatchListResponse;
import org.sopt.sopkathon.domain.match.service.MatchService;
import org.sopt.sopkathon.global.common.code.GlobalSuccessCode;
import org.sopt.sopkathon.global.response.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
public class MatchController implements MatchApi {

  private final MatchService matchService;

  /** limit 범위 검증(@Min, @Max)은 {@link MatchApi}에 선언되어 있다. */
  @Override
  @GetMapping
  public BaseResponse<MatchListResponse> getMatches(
      @RequestHeader("X-User-Id") Long userId,
      @RequestParam(defaultValue = "10") int limit) {
    return BaseResponse.success(GlobalSuccessCode.OK, matchService.getMatches(userId, limit));
  }

  @Override
  @PostMapping("/{targetId}/skip")
  public BaseResponse<Void> skip(
      @RequestHeader("X-User-Id") Long userId,
      @PathVariable Long targetId) {
    matchService.skip(userId, targetId);
    return BaseResponse.success(GlobalSuccessCode.OK);
  }
}
