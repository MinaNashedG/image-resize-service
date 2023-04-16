package com.deBijenkorf.imageresizeservice.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.deBijenkorf.imageresizeservice.service.ConnectionService;
import com.deBijenkorf.imageresizeservice.service.ImageResizeService;
import com.deBijenkorf.imageresizeservice.util.ImageResizeUtil;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableWebMvc
@ActiveProfiles("test")
class ImageResizeControllerIntegrationTest {

    public static final String SOURCE_URL = "http://www.debijenkorf.nl/INTERSHOP/static/WFS/dbk-shop-Site/-/dbk-shop/nl_NL/product-images/";
    private MockMvc mockMvc;

    @Autowired
    private ImageResizeService imageResizeService;

    @Autowired
    private ImageResizeUtil imageResizeUtil;

    @MockBean
    private AmazonS3 amazonS3;

    @MockBean
    private ConnectionService connectionService;

    @Autowired
    private ImageResizeController imageResizeController;

    @BeforeEach
    public void setup() {
        PutObjectResult putObjectResult = new PutObjectResult();
        mockMvc = MockMvcBuilders.standaloneSetup(imageResizeController).build();
        when(amazonS3.putObject(any(PutObjectRequest.class))).thenReturn(putObjectResult);
    }

    @Test
    public void should_return_resized_from_s3() throws Exception {
        InputStream inputStream = new FileInputStream("src/test/resources/static/test.jpg");
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new ByteArrayInputStream(inputStream.readAllBytes()));
        String predefinedTypeName = "thumbnail";
        String originalFilename = "test.jpg";

        when(amazonS3.getObject("/thumbnail/test.jpg", originalFilename)).thenReturn(s3Object);

        mockMvc.perform(MockMvcRequestBuilders.get("/image/show/{predefined-type-name}/{dummy-seo-name}",
                                predefinedTypeName, null)
                        .queryParam("reference", originalFilename))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                .andExpect(content().bytes(inputStream.readAllBytes()));

    }

    @Test
    public void should_save_and_return_resized_from_s3() throws Exception {
        InputStream inputStream = new FileInputStream("src/test/resources/static/test.jpg");

        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new ByteArrayInputStream(inputStream.readAllBytes()));
        String predefinedTypeName = "thumbnail";
        String originalFilename = "test.jpg";

        when(amazonS3.getObject("/thumbnail/test.jpg", originalFilename)).thenReturn(null);
        when(amazonS3.getObject("/original/test.jpg", originalFilename)).thenReturn(s3Object);

        mockMvc.perform(MockMvcRequestBuilders.get("/image/show/{predefined-type-name}/{dummy-seo-name}",
                                predefinedTypeName, null)
                        .queryParam("reference", originalFilename))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                .andExpect(content().string(Matchers.notNullValue()));

        verify(amazonS3, times(1)).putObject(any(PutObjectRequest.class));

    }

    @Test
    public void should_save_original_and_resized_and_return_resized_from_s3() throws Exception {

        String predefinedTypeName = "thumbnail";
        String originalFilename = "test.jpg";

        when(amazonS3.getObject("/thumbnail/test.jpg", originalFilename)).thenReturn(null);
        when(amazonS3.getObject("/original/test.jpg", originalFilename)).thenReturn(null);
        when(connectionService.downloadImage(SOURCE_URL + originalFilename))
                .thenReturn(ImageIO.read(new File("src/test/resources/static/test-image.jpg")));

        mockMvc.perform(MockMvcRequestBuilders.get("/image/show/{predefined-type-name}/{dummy-seo-name}",
                                predefinedTypeName, null)
                        .queryParam("reference", originalFilename))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                .andExpect(content().string(Matchers.notNullValue()));

        verify(amazonS3, times(2)).putObject(any(PutObjectRequest.class));

    }

    @Test
    public void should_testRemoveImage() throws Exception {
        InputStream inputStream = new FileInputStream("src/test/resources/static/test.jpg");

        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new ByteArrayInputStream(inputStream.readAllBytes()));
        String predefinedTypeName = "thumbnail";
        String originalFilename = "test.jpg";

        mockMvc.perform(MockMvcRequestBuilders.delete("/image/flush/{predefined-type-name}/",
                                predefinedTypeName)
                        .queryParam("reference", originalFilename))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        verify(amazonS3, times(1)).deleteObject("/thumbnail/test.jpg", "test.jpg");
    }

    @Test
    public void should_testRemoveOriginalImage() throws Exception {
        InputStream inputStream = new FileInputStream("src/test/resources/static/test.jpg");

        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(new ByteArrayInputStream(inputStream.readAllBytes()));
        String predefinedTypeName = "original";
        String originalFilename = "test.jpg";

        when(amazonS3.listBuckets()).thenReturn(getBuckets());
        lenient().when(amazonS3.listObjectsV2("/thumbnail/test.jpg")).thenReturn(getThumbnailObjectSummaries());
        lenient().when(amazonS3.listObjectsV2("/original/test.jpg")).thenReturn(getOriginalSummaries());


        mockMvc.perform(MockMvcRequestBuilders.delete("/image/flush/{predefined-type-name}/",
                                predefinedTypeName)
                        .queryParam("reference", originalFilename))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        verify(amazonS3, times(1)).deleteObject("/thumbnail/test.jpg", "test.jpg");
        verify(amazonS3, times(1)).deleteObject("/original/test.jpg", "test.jpg");
    }

    private List<Bucket> getBuckets() {
        List<Bucket> buckets = new ArrayList<>();
        buckets.add(new Bucket("/original/test.jpg"));
        buckets.add(new Bucket("/thumbnail/test.jpg"));
        return buckets;
    }

    private ListObjectsV2Result getThumbnailObjectSummaries() {
        ListObjectsV2Result result = new ListObjectsV2Result();
        List<S3ObjectSummary> objects = new ArrayList<>();
        objects.add(new S3ObjectSummary());
        objects.get(0).setBucketName("/thumbnail/test.jpg");
        objects.get(0).setKey("test.jpg");

        result.getObjectSummaries().addAll(objects);
        return result;
    }

    private ListObjectsV2Result getOriginalSummaries() {
        ListObjectsV2Result result = new ListObjectsV2Result();
        List<S3ObjectSummary> objects = new ArrayList<>();
        objects.add(new S3ObjectSummary());
        objects.get(0).setBucketName("/original/test.jpg");
        objects.get(0).setKey("test.jpg");

        result.getObjectSummaries().addAll(objects);
        return result;
    }

}