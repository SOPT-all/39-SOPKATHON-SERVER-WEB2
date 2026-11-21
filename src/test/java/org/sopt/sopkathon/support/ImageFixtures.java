package org.sopt.sopkathon.support;

/** 파일 시그니처 검사를 통과하는 최소한의 이미지 바이트. */
public final class ImageFixtures {

  public static final byte[] JPEG =
      {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F', 0, 1};
  public static final byte[] PNG =
      {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0x0D};
  public static final byte[] WEBP =
      {'R', 'I', 'F', 'F', 0x24, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8', ' '};

  private ImageFixtures() {}

  /** 확장자(jpg, jpeg, png, webp)에 맞는 이미지 바이트 */
  public static byte[] bytesOf(String filename) {
    String lower = filename.toLowerCase();
    if (lower.endsWith(".png")) {
      return PNG;
    }
    if (lower.endsWith(".webp")) {
      return WEBP;
    }
    return JPEG;
  }
}
