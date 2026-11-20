package org.sopt.sopkathon.global.health;

import org.sopt.sopkathon.global.common.code.GlobalSuccessCode;
import org.sopt.sopkathon.global.response.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping("/health")
  public BaseResponse<Void> health() {
    return BaseResponse.success(GlobalSuccessCode.OK);
  }
}
