package dev.steady.storage.service;

import dev.steady.global.exception.InvalidValueException;
import dev.steady.user.dto.response.PutObjectUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import static dev.steady.storage.exception.StorageErrorCode.NOT_SUPPORTED_FILE_TYPE;

@Component
public class StorageService {

    private static final String CONTENT_TYPE_PATTERN = "image/%s";
    private static final String OBJECT_URL_PATTERN = "https://%s.s3.%s.amazonaws.com/%s";
    private static final String PERIOD = ".";
    private final String bucketName;
    private final String region;
    private final S3Presigner s3Presigner;

    public StorageService(@Value("${cloud.s3.bucket}") String bucketName,
                          @Value("${cloud.aws.region}") String region,
                          S3Presigner s3Presigner) {
        this.bucketName = bucketName;
        this.region = region;
        this.s3Presigner = s3Presigner;
    }

    public PutObjectUrlResponse generatePutObjectUrl(String fileName, String keyPattern) {

        String extension = fileName.substring(fileName.lastIndexOf(PERIOD) + 1);
        validateImageFile(extension);

        String key = String.format(keyPattern, UUID.randomUUID());
        String contentType = getContentType(extension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(3))
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        String presignedUrl = presignedRequest.url().toString();
        String objectUrl = String.format(OBJECT_URL_PATTERN, bucketName, region, key);

        return PutObjectUrlResponse.of(presignedUrl, objectUrl);
    }

    private void validateImageFile(String extension) {
        String imageFileRegex = "^(jpeg|jpg|png)$";
        if (!Pattern.matches(imageFileRegex, extension)) {
            throw new InvalidValueException(NOT_SUPPORTED_FILE_TYPE);
        }
    }

    private String getContentType(String extension) {
        if (Objects.equals(extension, "jpg")) {
            return String.format(CONTENT_TYPE_PATTERN, "jpeg");
        }
        return String.format(CONTENT_TYPE_PATTERN, extension.toLowerCase());
    }

}
