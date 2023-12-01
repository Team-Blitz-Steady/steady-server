package dev.steady.storage.fixture;

import dev.steady.user.dto.response.PutObjectUrlResponse;
import org.springframework.web.util.UriComponentsBuilder;

public class StorageFixture {

    public static PutObjectUrlResponse createPutObjectUrlResponse() {
        String presignedUrl = UriComponentsBuilder
                .fromUriString("bucket-name.s3.region.amazonaws.com/path/{fileName}")
                .queryParam("X-Amz-Algorithm", "{Algorithm}")
                .queryParam("X-Amz-Date", "{Date}")
                .queryParam("X-Amz-SignedHeaders", "{SignedHeaders}")
                .queryParam("X-Amz-Credential", "{Credential}")
                .queryParam("X-Amz-Expires", "{Expires}")
                .queryParam("X-Amz-Signature", "{Signature}")
                .build().toString();
        String objectUrl = "https:{bucket_name}.s3.{region}.com/{key}";
        return PutObjectUrlResponse.of(presignedUrl, objectUrl);
    }

}
