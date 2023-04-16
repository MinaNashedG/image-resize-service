package com.deBijenkorf.imageresizeservice.service;

import com.deBijenkorf.imageresizeservice.dao.ImageDao;
import com.deBijenkorf.imageresizeservice.exception.NoResourceFoundException;
import com.deBijenkorf.imageresizeservice.model.PredefineTypeName;
import com.deBijenkorf.imageresizeservice.util.ImageResizeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
public class ImageResizeService {

    public static final String ORIGINAL = "original";
    public static final String JPG = "jpg";
    private final ImageDao imageDao;
    private final ImageResizeUtil imageResizeUtil;
    private final ConnectionService connectionService;
    private final String sourceUrl;

    public ImageResizeService(ImageDao imageDao, ImageResizeUtil imageResizeUtil, ConnectionService connectionService,
                              @Value("${source-root-url}") String sourceUrl) {
        this.imageDao = imageDao;
        this.imageResizeUtil = imageResizeUtil;
        this.connectionService = connectionService;
        this.sourceUrl = sourceUrl;
    }


    public byte[] findResizedImage(String predefinedTypeName, String originalFilename) {
        checkPredefineTypeExists(predefinedTypeName);
        BufferedImage resizedInputStream = findImageInAwsS3(predefinedTypeName, originalFilename);

        if (resizedInputStream == null) {
            resizedInputStream = findOriginalAwsS3(ORIGINAL, originalFilename);
        }

        ByteArrayOutputStream resizedOutputStream = new ByteArrayOutputStream();
        writeImage(resizedInputStream, resizedOutputStream);

        return resizedOutputStream.toByteArray();
    }

    private void checkPredefineTypeExists(String predefinedTypeName) {
        if (!PredefineTypeName.THUMBNAIL.getValue().equals(predefinedTypeName)
                && !PredefineTypeName.DETAIL_LARGE.getValue().equals(predefinedTypeName)) {

            log.info("Predefined Image type is not supported");
            throw new NoResourceFoundException("Predefined Image type is not supported");
        }
    }

    public void flush(String predefinedTypeName, String originalFilename) {
        imageDao.deleteObject(originalFilename, predefinedTypeName);
    }

    private void writeImage(BufferedImage resizedInputStream, ByteArrayOutputStream baos) {
        try {
            ImageIO.write(resizedInputStream, JPG, baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    BufferedImage findOriginalAwsS3(String predefinedTypeName, String originalFilename) {
        BufferedImage originalInputStream = findImageInAwsS3(ORIGINAL, originalFilename);

        if (originalInputStream == null) {
            originalInputStream = connectionService.downloadImage(sourceUrl + originalFilename);
            imageDao.save(originalInputStream, originalFilename, ORIGINAL);
        }

        return resizeAndSaveImageInAWS(predefinedTypeName, originalFilename, originalInputStream);
    }

    private BufferedImage resizeAndSaveImageInAWS(String predefinedTypeName, String originalFilename, BufferedImage originalInputStream) {
        try {
            BufferedImage resizedInputStream = imageResizeUtil.resize(originalInputStream);
            imageDao.save(resizedInputStream, originalFilename, predefinedTypeName);
            return resizedInputStream;
        } catch (Throwable e) {
            log.warn("Fail to save image in Amazon S3 {}", e);
            throw new NoResourceFoundException("Fail to save in Amazon S3");
        }
    }

    private BufferedImage findImageInAwsS3(String predefinedTypeName, String originalFilename) {
        try {
            return imageDao.findByName(predefinedTypeName, originalFilename);
        } catch (Throwable e) {
            log.error("fail to load resource", e);
            throw new NoResourceFoundException("fail to load resource");
        }
    }
}
