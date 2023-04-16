package com.deBijenkorf.imageresizeservice.dao;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.deBijenkorf.imageresizeservice.exception.NoResourceFoundException;
import com.deBijenkorf.imageresizeservice.util.ImageUtil;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static java.io.File.separator;

@Slf4j
@Repository
@AllArgsConstructor
public class ImageDao {

    public static final String ORIGINAL = "original";
    private final AmazonS3 amazonS3;
    private final ImageUtil imageUtil;

    public BufferedImage findByName(String typeName, String fileName) throws IOException {
        if (amazonS3 == null) {
            log.error("Fail to connect to Amazon S3");
            throw new NoResourceFoundException("Fail to connect to Amazon S3");
        }
        fileName = fileName.replaceAll(separator, "_");
        log.info("Downloading file with name {}", fileName);

        return Optional.ofNullable(amazonS3.getObject(getS3BucketName(fileName, typeName), fileName))
                .map(S3Object::getObjectContent)
                .map(this::getBufferedImage)
                .orElse(null);
    }

    private BufferedImage getBufferedImage(S3ObjectInputStream s3ObjectInputStream) {
        try {
            return imageUtil.bufferedImage(s3ObjectInputStream);
        } catch (IOException ex) {
            log.error("fail to load image from S3", ex);
            throw new NoResourceFoundException("fail to load image from S3");
        }
    }

    public void save(BufferedImage bufferedImage, String fileName, String typeName) {
        try {
            File file = new File(fileName);
            imageUtil.writeImage(bufferedImage, getFileExtension(fileName), file);
            PutObjectRequest putObjectRequest = new PutObjectRequest(getS3BucketName(fileName, typeName), fileName, file);
            amazonS3.putObject(putObjectRequest);
            Files.delete(file.toPath()); // Remove the file locally created in the project folder
        } catch (AmazonServiceException e) {
            log.error("Error {} occurred while uploading file", e.getLocalizedMessage());
        } catch (IOException ex) {
            log.error("Error {} occurred while deleting temporary file", ex.getLocalizedMessage());
        }
    }

    public void deleteObject(String fileName, String typeName) {
        final String s3BucketName = getS3BucketName(fileName, typeName);
        if (StringUtils.isNotBlank(s3BucketName) && !s3BucketName.contains(ORIGINAL)) {
            amazonS3.deleteObject(s3BucketName, fileName);
        } else {
            deleteAllFilesInS3Buckets(fileName);
        }
    }

    private void deleteAllFilesInS3Buckets(String fileName) {
        List<Bucket> buckets = amazonS3.listBuckets();
        for (Bucket bucket : buckets) {
            String bucketName = bucket.getName();
            ListObjectsV2Result result = amazonS3.listObjectsV2(bucketName);
            List<S3ObjectSummary> objects = result.getObjectSummaries();
            for (S3ObjectSummary object : objects) {
                if (object.getKey().equals(fileName)) {
                    amazonS3.deleteObject(bucketName, fileName);
                }
            }
        }
    }

    private String getS3BucketName(String fileName, String typeName) {
        StringBuilder builder = new StringBuilder(separator);
        builder.append(typeName == null ? ORIGINAL : typeName).append(separator);
        String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));

        if (fileNameWithoutExt.length() > 4)
            builder.append(fileNameWithoutExt, 0, 4).append(separator);

        if (fileNameWithoutExt.length() > 8)
            builder.append(fileNameWithoutExt, 4, 8).append(separator);

        builder.append(fileName);
        return builder.toString();
    }

    public String getFileExtension(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1))
                .orElseThrow(() -> new IllegalArgumentException("invalid file extension"));
    }
}
