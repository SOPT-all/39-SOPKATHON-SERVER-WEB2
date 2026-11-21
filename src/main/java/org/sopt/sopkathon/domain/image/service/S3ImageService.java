package org.sopt.sopkathon.domain.image.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.sopt.sopkathon.domain.image.ImageDirectory;
import org.sopt.sopkathon.domain.image.code.ImageErrorCode;
import org.sopt.sopkathon.global.exception.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class S3ImageService {

  // 확장자 → Content-Type. 둘 다 일치해야 허용
  private static final Map<String, String> ALLOWED_TYPES = Map.of(
      "jpg", "image/jpeg",
      "jpeg", "image/jpeg",
      "png", "image/png",
      "webp", "image/webp"
  );

  // 확장자·Content-Type은 클라이언트가 정하는 값이라 실제 파일 시그니처도 확인
  private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
  private static final byte[] PNG_SIGNATURE =
      {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
  private static final byte[] RIFF_SIGNATURE = {'R', 'I', 'F', 'F'};
  private static final byte[] WEBP_SIGNATURE = {'W', 'E', 'B', 'P'};
  private static final int SIGNATURE_LENGTH = 12;

  private final S3Client s3Client;
  private final String bucket;

  public S3ImageService(
      S3Client s3Client,
      @Value("${cloud.aws.s3.bucket}") String bucket
  ) {
    this.s3Client = s3Client;
    this.bucket = bucket;
  }

  public String upload(MultipartFile file, ImageDirectory directory) {
    if (file == null || file.isEmpty()) {
      throw new BaseException(ImageErrorCode.EMPTY_IMAGE);
    }

    String extension = extractExtension(file.getOriginalFilename());
    String contentType = ALLOWED_TYPES.get(extension);
    if (contentType == null || !contentType.equalsIgnoreCase(file.getContentType())) {
      throw new BaseException(ImageErrorCode.INVALID_IMAGE_TYPE);
    }
    if (!hasSignatureOf(file, contentType)) {
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

    return s3Client.utilities()
        .getUrl(GetUrlRequest.builder().bucket(bucket).key(key).build())
        .toString();
  }

  private boolean hasSignatureOf(MultipartFile file, String contentType) {
    byte[] header;
    try (InputStream inputStream = file.getInputStream()) {
      header = inputStream.readNBytes(SIGNATURE_LENGTH);
    } catch (IOException e) {
      log.error("[S3] 이미지 파일 읽기 실패", e);
      throw new BaseException(ImageErrorCode.IMAGE_UPLOAD_FAILED, e);
    }

    return switch (contentType) {
      case "image/jpeg" -> startsWith(header, 0, JPEG_SIGNATURE);
      case "image/png" -> startsWith(header, 0, PNG_SIGNATURE);
      // WebP: "RIFF" + 파일 크기(4바이트) + "WEBP"
      case "image/webp" -> startsWith(header, 0, RIFF_SIGNATURE) && startsWith(header, 8, WEBP_SIGNATURE);
      default -> false;
    };
  }

  private static boolean startsWith(byte[] data, int offset, byte[] signature) {
    return data.length >= offset + signature.length
        && Arrays.equals(data, offset, offset + signature.length, signature, 0, signature.length);
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
