package com.deBijenkorf.imageresizeservice.controller;

import com.deBijenkorf.imageresizeservice.service.ImageResizeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import static java.io.File.separator;

@Slf4j
@RestController
@AllArgsConstructor
public class ImageResizeController {

    private final ImageResizeService imageResizeService;

    @RequestMapping(method = RequestMethod.GET, value = "/image/show/{predefined-type-name}/{dummy-seo-name}/", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] showImage(@PathVariable("predefined-type-name") String predefinedTypeName,
                            @PathVariable(value = "dummy-seo-name") String seoName,
                            @RequestParam(value = "reference") String originalFilename) {

        return imageResizeService.findResizedImage(predefinedTypeName, originalFilename.replaceAll(separator, "_"));

    }

    @RequestMapping(method = RequestMethod.GET, value = "/image/show/{predefined-type-name}/", produces = MediaType.IMAGE_JPEG_VALUE)
    @ResponseBody
    public byte[] showImage(@PathVariable("predefined-type-name") String predefinedTypeName,
                            @RequestParam(value = "reference") String originalFilename) {

        return imageResizeService.findResizedImage(predefinedTypeName, originalFilename.replaceAll(separator, "_"));

    }


    @DeleteMapping("/image/flush/{predefined-image-type}/")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeImage(@PathVariable("predefined-image-type") String predefinedTypeName,
                            @RequestParam(value = "reference") String originalFilename) {
        imageResizeService.flush(predefinedTypeName, originalFilename.replaceAll(separator, "_"));
    }

}
