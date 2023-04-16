package com.deBijenkorf.imageresizeservice.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Slf4j
public class AWSConfig {

    @Value("${aws-accesskey}")
    private String accessKeyId;

    @Value("${aws-secretkey}")
    private String accessKeySecret;

    @Value("${aws-region-name}")
    private String s3RegionName;

    @Bean
    public AmazonS3 getAmazonS3Client() {
        try {
            final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKeyId, accessKeySecret);
            return AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                    .withRegion(s3RegionName)
                    .build();
        } catch (Exception e) {
            log.error("Fail to connect to Amazon S3", e);
            return null;
        }
    }

}
