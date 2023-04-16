package com.deBijenkorf.imageresizeservice.service;

import com.deBijenkorf.imageresizeservice.dao.ImageDao;
import com.deBijenkorf.imageresizeservice.exception.NoResourceFoundException;
import com.deBijenkorf.imageresizeservice.util.ImageResizeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageResizeServiceTest {

    private ImageResizeService imageResizeService;

    @Mock
    private ImageDao imageDao;

    @Mock
    private ImageResizeUtil imageResizeUtil;

    @Mock
    private ConnectionService connectionService;

    @BeforeEach
    public void setup() {
        imageResizeService = new ImageResizeService(imageDao, imageResizeUtil, connectionService, "sourceUrl");
    }

    @Test
    public void should_return_resized_image() throws IOException {
        // Given
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        when(imageDao.findByName("thumbnail", "test.jpg")).thenReturn(image);

        // WHEN
        byte[] result = imageResizeService.findResizedImage("thumbnail", "test.jpg");

        // Then
        assertNotNull(result);
    }

    @Test
    public void should_resized_the_original_image_first() throws IOException {
        // Given
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage resized = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        when(imageDao.findByName("thumbnail", "test.jpg")).thenReturn(null);
        when(imageDao.findByName("original", "test.jpg")).thenReturn(image);
        when(imageResizeUtil.resize(image)).thenReturn(resized);
        // WHEN
        byte[] result = imageResizeService.findResizedImage("thumbnail", "test.jpg");

        // Then
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", output);
        assertArrayEquals(result, output.toByteArray());
    }

    @Test
    public void should_download_the_original_and_save_in_aws() throws IOException {
        // Given
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage resized = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        when(imageDao.findByName("thumbnail", "test.jpg")).thenReturn(null);
        when(imageDao.findByName("original", "test.jpg")).thenReturn(null);
        when(connectionService.downloadImage(anyString())).thenReturn(image);
        when(imageResizeUtil.resize(image)).thenReturn(resized);
        // WHEN
        byte[] result = imageResizeService.findResizedImage("thumbnail", "test.jpg");

        // Then
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(resized, "jpg", output);
        assertArrayEquals(result, output.toByteArray());
    }

    @Test
    public void should_throw_exception() throws IOException {
        // Execution and Assertion
        assertThrows(NoResourceFoundException.class, () -> imageResizeService.findResizedImage("test", "test.jpg"));

        when(imageDao.findByName("thumbnail", "test.jpg")).thenThrow(new IllegalArgumentException());

        assertThrows(NoResourceFoundException.class, () -> imageResizeService.findResizedImage("thumbnail", "test.jpg"));


    }

    @Test
    public void testFlush() {

        String predefinedTypeName = "thumbnail";
        String originalFilename = "test.jpg";

        imageResizeService.flush(predefinedTypeName, originalFilename);

        verify(imageDao).deleteObject(originalFilename, predefinedTypeName);
    }

    @Test
    public void testFlushException() {


        String predefinedTypeName = "thumbnail";
        String originalFilename = "test.jpg";

        doThrow(new IllegalArgumentException()).when(imageDao).deleteObject(originalFilename, predefinedTypeName);

        assertThrows(IllegalArgumentException.class, () -> imageResizeService.flush(predefinedTypeName, originalFilename));
    }

    @Test
    public void should_throw_exception_when_no_connection() throws IOException {
        // Given
        when(imageDao.findByName("thumbnail", "test.jpg")).thenReturn(null);
        when(imageDao.findByName("original", "test.jpg")).thenReturn(null);
        when(connectionService.downloadImage(anyString())).thenThrow(new NoResourceFoundException("test"));
        // Then
        assertThrows(NoResourceFoundException.class, () -> imageResizeService.findResizedImage("thumbnail", "test.jpg"));
    }

    @Test
    public void should_throw_exception_when_fail_save_image_in_s3() throws IOException {
        // Given
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        BufferedImage resized = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        when(imageDao.findByName("thumbnail", "test.jpg")).thenReturn(null);
        when(imageDao.findByName("original", "test.jpg")).thenReturn(image);
        when(imageResizeUtil.resize(image)).thenReturn(resized);
        doThrow(new IllegalArgumentException("test")).when(imageDao).save(resized, "test.jpg", "thumbnail");
        // Then
        assertThrows(NoResourceFoundException.class, () -> imageResizeService.findResizedImage("thumbnail", "test.jpg"));
    }
}
