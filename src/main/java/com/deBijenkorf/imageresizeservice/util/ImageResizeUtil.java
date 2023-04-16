package com.deBijenkorf.imageresizeservice.util;

import com.deBijenkorf.imageresizeservice.config.ImageConfig;
import com.deBijenkorf.imageresizeservice.model.ImageType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Component
@AllArgsConstructor
public class ImageResizeUtil {

    public static final float SKEW_RATIO = 0.2f;

    private final ImageConfig imageConfig;

    public BufferedImage resize(BufferedImage originalImage) throws IOException {

        BufferedImage resizedImage = new BufferedImage(imageConfig.getWidth(), imageConfig.getHeight()
                , BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        switch (imageConfig.getScaleType()) {
            case Crop -> crop(originalImage, graphics);
            case Fill -> fill(originalImage, graphics);
            default -> skew(originalImage, graphics);
        }

        graphics.dispose();
        return resizeQuality(resizedImage);
    }

    private BufferedImage resizeQuality(BufferedImage resizedImage) throws IOException {
        float quality = imageConfig.getQuality() / 100f;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageWriter writer = ImageIO.getImageWritersByFormatName(imageType()).next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.setOutput(new MemoryCacheImageOutputStream(baos));
            writer.write(null, new IIOImage(resizedImage, null, null), param);
            writer.dispose();

        } catch (IOException e) {
            log.error("fail to parse the image", e);
        }
        return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
    }

    private String imageType() {
        return ImageType.JPG == imageConfig.getType() ? "jpg" : "png";
    }

    private void skew(BufferedImage originalImage, Graphics2D graphics) {
        AffineTransform skewTransform = AffineTransform.getShearInstance(SKEW_RATIO, 0);
        graphics.setTransform(skewTransform);
        graphics.drawImage(originalImage, 0, 0, null);
    }

    private void fill(BufferedImage originalImage, Graphics2D graphics) {
        Color color = Color.decode(imageConfig.getFillColor());

        graphics.setColor(color);
        graphics.fillRect(0, 0, imageConfig.getWidth(), imageConfig.getHeight());
        graphics.drawImage(originalImage,
                (imageConfig.getWidth() - originalImage.getWidth()) / 2,
                (imageConfig.getHeight() - originalImage.getHeight()) / 2,
                originalImage.getWidth(),
                originalImage.getHeight(), null);

    }

    private void crop(BufferedImage originalImage, Graphics2D graphics) {
        graphics.drawImage(originalImage, 0, 0,
                imageConfig.getWidth(),
                imageConfig.getHeight(),
                originalImage.getWidth() / 4,
                originalImage.getHeight() / 4, originalImage.getWidth() * 3 / 4,
                originalImage.getHeight() * 3 / 4, null);
    }
}
