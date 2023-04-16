package com.deBijenkorf.imageresizeservice.util;

import com.deBijenkorf.imageresizeservice.config.ImageConfig;
import com.deBijenkorf.imageresizeservice.model.ImageType;
import com.deBijenkorf.imageresizeservice.model.ScaleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageResizeUtilTest {

    private ImageResizeUtil imageResizeUtil;
    private BufferedImage testImage;
    private ImageConfig imageConfig;

    @BeforeEach
    public void setup() throws IOException {
        imageConfig = new ImageConfig(200, 200, 80, ScaleType.Crop, "#3260a8", ImageType.PNG);
        imageResizeUtil = new ImageResizeUtil(imageConfig);
        testImage = ImageIO.read(new File("src/test/resources/static/test-image.jpg"));
    }

    @Test
    public void testResizeQuality() throws IOException {
        BufferedImage resizedImage = imageResizeUtil.resize(testImage);
        assertTrue(resizedImage.getWidth() <= imageConfig.getWidth());
        assertTrue(resizedImage.getHeight() <= imageConfig.getHeight());
    }

    @Test
    public void testSkew() throws IOException {
        imageConfig.setScaleType(ScaleType.Skew);
        final String pathname = "src/test/resources/static/test_output_skew.jpg";
        final BufferedImage resize = imageResizeUtil.resize(testImage);
        ImageIO.write(resize, "jpg", new File(pathname));
        assertTrue(new File(pathname).exists());
    }

    @Test
    public void testFill() throws IOException {
        imageConfig.setScaleType(ScaleType.Fill);
        final String pathname = "src/test/resources/static/test_output_fill.jpg";
        ImageIO.write(imageResizeUtil.resize(testImage), "jpg", new File(pathname));
        assertTrue(new File(pathname).exists());
    }

    @Test
    public void testCrop() throws IOException {
        imageConfig.setScaleType(ScaleType.Crop);
        final String pathname = "src/test/resources/static/test_output_crop.jpg";
        ImageIO.write(imageResizeUtil.resize(testImage), "jpg", new File(pathname));
        assertTrue(new File(pathname).exists());
    }
}