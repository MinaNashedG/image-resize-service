package com.deBijenkorf.imageresizeservice.dao;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.deBijenkorf.imageresizeservice.util.ImageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageDaoTest {

    private ImageDao imageDao;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private ImageUtil imageUtil;

    @BeforeEach
    public void setUp() throws Exception {

        imageDao = new ImageDao(amazonS3, imageUtil);
        // this.image = ImageIO.read(new File("src/test/resources/static/test-image.jpg"));

    }

    @Test
    public void should_load_image_from_s3() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        BufferedImage bufferedImage = mock(BufferedImage.class);
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(inputStream);

        when(amazonS3.getObject("/thumbnail/abcd/efgh/abcdefghij.jpg", "abcdefghij.jpg")).thenReturn(s3Object);
        when(imageUtil.bufferedImage(s3Object.getObjectContent())).thenReturn(bufferedImage);

        //WHEN
        BufferedImage result = imageDao.findByName("thumbnail", "abcdefghij.jpg");

        //THEN
        verify(amazonS3).getObject("/thumbnail/abcd/efgh/abcdefghij.jpg", "abcdefghij.jpg");
        assertNotNull(result);
    }

    @Test
    public void should_load_image_from_s3_with_4_char_name() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        BufferedImage bufferedImage = mock(BufferedImage.class);
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(inputStream);

        final String fileName = "abcd.jpg";
        final String bucketName = "/thumbnail/abcd.jpg";

        when(amazonS3.getObject(bucketName, fileName)).thenReturn(s3Object);
        when(imageUtil.bufferedImage(s3Object.getObjectContent())).thenReturn(bufferedImage);

        //WHEN
        BufferedImage result = imageDao.findByName("thumbnail", fileName);

        //THEN
        verify(amazonS3).getObject(bucketName, fileName);
        assertNotNull(result);
    }

    @Test
    public void should_load_image_from_s3_with_underscore_name() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        BufferedImage bufferedImage = mock(BufferedImage.class);
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(inputStream);

        final String fileName = "/somedir/anotherdir/abcdef.jpg";
        final String expectedFileName = "_somedir_anotherdir_abcdef.jpg";
        final String bucketName = "/thumbnail/_som/edir/_somedir_anotherdir_abcdef.jpg";

        when(amazonS3.getObject(bucketName, expectedFileName)).thenReturn(s3Object);
        when(imageUtil.bufferedImage(s3Object.getObjectContent())).thenReturn(bufferedImage);

        //WHEN
        BufferedImage result = imageDao.findByName("thumbnail", fileName);

        //THEN
        verify(amazonS3).getObject(bucketName, expectedFileName);
        assertNotNull(result);
    }

    @Test
    public void should_load_image_from_s3_with_6_char_name() throws IOException {
        InputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3});
        BufferedImage bufferedImage = mock(BufferedImage.class);
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(inputStream);

        final String fileName = "abcdef.jpg";
        final String bucketName = "/thumbnail/abcd/abcdef.jpg";

        when(amazonS3.getObject(bucketName, fileName)).thenReturn(s3Object);
        when(imageUtil.bufferedImage(s3Object.getObjectContent())).thenReturn(bufferedImage);

        //WHEN
        BufferedImage result = imageDao.findByName("thumbnail", fileName);

        //THEN
        verify(amazonS3).getObject(bucketName, fileName);
        assertNotNull(result);
    }

    @Test
    public void should_save_in_s3() throws IOException {

        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        File file = new File("test.jpg");

        when(imageUtil.writeImage(image, "jpg", file)).thenReturn(true);
        //WHEN
        imageDao.save(image, "test.jpg", "type");

        //THEN
        verify(amazonS3, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    public void should_get_file_extension() {
        String filename = "test-image.jpg";
        String expected = "jpg";

        String result = imageDao.getFileExtension(filename);

        assertEquals(expected, result);
    }

    @Test
    public void should_deleteOneFileInS3ThumbnailBucket() {
        String fileName = "test.jpg";

        imageDao.deleteObject(fileName, "thumbnail");
        verify(amazonS3, times(1)).deleteObject(eq("/thumbnail/test.jpg"), eq(fileName));
    }

    @Test
    public void should_deleteAllFileInS3Buckets() {
        String fileName = "test.jpg";
        when(amazonS3.listBuckets()).thenReturn(getBuckets());
        when(amazonS3.listObjectsV2("/original/test.jpg")).thenReturn(getOriginalSummaries());
        when(amazonS3.listObjectsV2("/thumbnail/test.jpg")).thenReturn(getThumbnailObjectSummaries());

        imageDao.deleteObject(fileName, "original");
        verify(amazonS3, times(1)).deleteObject(eq("/thumbnail/test.jpg"), eq(fileName));
        verify(amazonS3, times(1)).deleteObject(eq("/original/test.jpg"), eq(fileName));
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