package org.sopt.sopkathon.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

  @Value("${cloud.aws.region}")
  private String region;

  @Value("${cloud.aws.credentials.access-key:}")
  private String accessKey;

  @Value("${cloud.aws.credentials.secret-key:}")
  private String secretKey;

  @Bean(destroyMethod = "close")
  public S3Client s3Client() {
    return S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(credentialsProvider())
        .build();
  }

  /**
   * 키가 설정되어 있으면(로컬) 해당 키를, 없으면(운영 EC2 IAM Role 등) 기본 자격 증명 체인을 사용한다.
   */
  private AwsCredentialsProvider credentialsProvider() {
    if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
      return StaticCredentialsProvider.create(
          AwsBasicCredentials.create(accessKey, secretKey)
      );
    }
    return DefaultCredentialsProvider.builder().build();
  }
}
