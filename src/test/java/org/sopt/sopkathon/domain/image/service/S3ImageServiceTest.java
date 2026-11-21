package org.sopt.sopkathon.domain.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sopt.sopkathon.domain.image.ImageDirectory;
import org.sopt.sopkathon.domain.image.code.ImageErrorCode;
import org.sopt.sopkathon.global.exception.BaseException;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

class S3ImageServiceTest {

  private static final String BUCKET = "test-bucket";
  private static final String URL_PREFIX = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/";

  private S3Client s3Client;
  private S3ImageService s3ImageService;

  @BeforeEach
  void setUp() {
    s3Client = mock(S3Client.class);
    given(s3Client.utilities()).willReturn(
        S3Utilities.builder().region(Region.AP_NORTHEAST_2).build()
    );
    s3ImageService = new S3ImageService(s3Client, BUCKET);
  }

  @Test
  void 이미지_업로드에_성공하면_URL을_반환한다() {
    MockMultipartFile file = image("profile.PNG", "image/png");

    String url = s3ImageService.upload(file, ImageDirectory.PROFILE);

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
    PutObjectRequest request = captor.getValue();

    assertThat(request.bucket()).isEqualTo(BUCKET);
    assertThat(request.key()).startsWith("profile/").endsWith(".png");
    assertThat(request.contentType()).isEqualTo("image/png");
    assertThat(url).isEqualTo(URL_PREFIX + request.key());
  }

  @Test
  void 빈_파일은_업로드할_수_없다() {
    MockMultipartFile file = new MockMultipartFile("file", "profile.png", "image/png", new byte[0]);

    assertErrorCode(() -> s3ImageService.upload(file, ImageDirectory.PROFILE),
        ImageErrorCode.EMPTY_IMAGE);
    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void 허용하지_않은_확장자는_업로드할_수_없다() {
    MockMultipartFile file = image("profile.gif", "image/gif");

    assertErrorCode(() -> s3ImageService.upload(file, ImageDirectory.PROFILE),
        ImageErrorCode.INVALID_IMAGE_TYPE);
  }

  @Test
  void 확장자와_ContentType이_다르면_업로드할_수_없다() {
    MockMultipartFile file = image("profile.png", "text/html");

    assertErrorCode(() -> s3ImageService.upload(file, ImageDirectory.PROFILE),
        ImageErrorCode.INVALID_IMAGE_TYPE);
  }

  @Test
  void S3_업로드_중_예외가_발생하면_업로드_실패_에러를_던진다() {
    given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .willThrow(S3Exception.builder().message("fail").build());

    assertErrorCode(() -> s3ImageService.upload(image("profile.jpg", "image/jpeg"),
        ImageDirectory.PROFILE), ImageErrorCode.IMAGE_UPLOAD_FAILED);
  }

  private MockMultipartFile image(String filename, String contentType) {
    return new MockMultipartFile("file", filename, contentType, new byte[]{1, 2, 3});
  }

  private void assertErrorCode(Runnable runnable, ImageErrorCode errorCode) {
    assertThatThrownBy(runnable::run)
        .isInstanceOf(BaseException.class)
        .extracting("errorCode")
        .isEqualTo(errorCode);
  }
}
