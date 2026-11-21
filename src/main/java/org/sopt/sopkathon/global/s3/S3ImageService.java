package org.sopt.sopkathon.global.s3;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.sopt.sopkathon.global.common.code.ImageErrorCode;
import org.sopt.sopkathon.global.exception.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * S3 이미지 업로드/삭제 공통 서비스.
 * 반환하는 URL을 그대로 DB에 저장하고, 삭제 시 같은 URL을 넘기면 된다.
 */
@Slf4j
@Service
public class S3ImageService {

  // 확장자 → Content-Type (확장자와 Content-Type이 모두 일치해야 허용)
  private static final Map<String, String> ALLOWED_TYPES = Map.of(
      "jpg", "image/jpeg",
      "jpeg", "image/jpeg",
      "png", "image/png",
      "webp", "image/webp"
  );

  private final S3Client s3Client;
  private final String bucket;
  private final String urlPrefix;

  public S3ImageService(
      S3Client s3Client,
      @Value("${cloud.aws.s3.bucket}") String bucket
  ) {
    this.s3Client = s3Client;
    this.bucket = bucket;
    this.urlPrefix = resolveUrlPrefix(s3Client, bucket);
  }

  // 빈 key는 허용되지 않으므로 임시 key로 URL을 만든 뒤 key 부분을 잘라낸다
  private static String resolveUrlPrefix(S3Client s3Client, String bucket) {
    String placeholder = "placeholder";
    String url = s3Client.utilities()
        .getUrl(GetUrlRequest.builder().bucket(bucket).key(placeholder).build())
        .toString();
    return url.substring(0, url.length() - placeholder.length());
  }

  /**
   * @param file      업로드할 이미지
   * @param directory 저장할 디렉터리 (예: {@link ImageDirectory#PROFILE})
   * @return 업로드된 이미지 URL
   */
  public String upload(MultipartFile file, ImageDirectory directory) {
    if (file == null || file.isEmpty()) {
      throw new BaseException(ImageErrorCode.EMPTY_IMAGE);
    }

    String extension = extractExtension(file.getOriginalFilename());
    String contentType = ALLOWED_TYPES.get(extension);
    if (contentType == null || !contentType.equalsIgnoreCase(file.getContentType())) {
      throw new BaseException(ImageErrorCode.INVALID_IMAGE_TYPE);
    }

    String key = directory.getPath() + "/" + UUID.randomUUID() + "." + extension;

    PutObjectRequest request = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(contentType)
        .build();

    try (InputStream inputStream = file.getInputStream()) {
      s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
    } catch (IOException | SdkException e) {
      log.error("[S3] 이미지 업로드 실패 key={}", key, e);
      throw new BaseException(ImageErrorCode.IMAGE_UPLOAD_FAILED, e);
    }

    return urlPrefix + key;
  }

  /**
   * @param imageUrl {@link #upload}가 반환한 이미지 URL
   */
  public void delete(String imageUrl) {
    if (!StringUtils.hasText(imageUrl) || !imageUrl.startsWith(urlPrefix)
        || imageUrl.length() == urlPrefix.length()) {
      throw new BaseException(ImageErrorCode.INVALID_IMAGE_URL);
    }

    String key = imageUrl.substring(urlPrefix.length());

    try {
      s3Client.deleteObject(DeleteObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build());
    } catch (SdkException e) {
      log.error("[S3] 이미지 삭제 실패 key={}", key, e);
      throw new BaseException(ImageErrorCode.IMAGE_DELETE_FAILED, e);
    }
  }

  private String extractExtension(String filename) {
    if (!StringUtils.hasText(filename)) {
      throw new BaseException(ImageErrorCode.INVALID_IMAGE_TYPE);
    }
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == filename.length() - 1) {
      throw new BaseException(ImageErrorCode.INVALID_IMAGE_TYPE);
    }
    return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
  }
}
