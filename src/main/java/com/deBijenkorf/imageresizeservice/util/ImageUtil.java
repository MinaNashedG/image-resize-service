package com.deBijenkorf.imageresizeservice.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
@AllArgsConstructor
public class ImageUtil {

    public BufferedImage bufferedImage(InputStream inputStream) throws IOException {
        return ImageIO.read(inputStream);
    }

    public boolean writeImage(BufferedImage image, String extension, File file) throws IOException {
        return ImageIO.write(image, extension, file);
    }
}
