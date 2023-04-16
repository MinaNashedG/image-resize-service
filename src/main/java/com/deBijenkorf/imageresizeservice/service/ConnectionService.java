package com.deBijenkorf.imageresizeservice.service;

import com.deBijenkorf.imageresizeservice.exception.NoResourceFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;
import java.net.URL;


@Service
@Slf4j
public class ConnectionService {

    public BufferedImage downloadImage(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            BufferedImage image = ImageIO.read(connection.getInputStream());
            if (image == null) {
                log.info("Source Image not found");
                throw new NoResourceFoundException("Source Image not found");
            }
            connection.disconnect();
            return image;
        } catch (Exception e) {
            log.info("Source Image not found");
            throw new NoResourceFoundException("Source Image not found");
        }
    }
}
