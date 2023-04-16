package com.deBijenkorf.imageresizeservice.config;

import com.deBijenkorf.imageresizeservice.model.ImageType;
import com.deBijenkorf.imageresizeservice.model.ScaleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
@Data
@ConfigurationProperties(value = "predefined.image.types")
public class ImageConfig {

    private Integer height;

    private Integer width;

    private Integer quality;

    private ScaleType scaleType;

    private String fillColor;

    private ImageType type;

}
