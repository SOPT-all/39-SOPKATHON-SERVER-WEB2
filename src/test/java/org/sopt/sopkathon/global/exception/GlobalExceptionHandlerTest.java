package org.sopt.sopkathon.global.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sopt.sopkathon.global.common.code.GlobalErrorCode;
import org.sopt.sopkathon.global.response.BaseResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void DB_무결성_위반은_500이_아니라_409로_응답한다() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/requests");

    ResponseEntity<BaseResponse<Void>> response =
        handler.handleDataIntegrityViolation(
            new DataIntegrityViolationException("duplicate"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody().code()).isEqualTo(GlobalErrorCode.DUPLICATE_RESOURCE.getCode());
  }
}
