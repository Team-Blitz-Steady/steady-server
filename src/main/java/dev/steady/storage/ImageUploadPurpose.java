package dev.steady.storage;

import dev.steady.global.exception.InvalidValueException;
import lombok.Getter;

import java.util.Arrays;

import static dev.steady.storage.exception.StorageErrorCode.NOT_SUPPORTED_PURPOSE;

@Getter
public enum ImageUploadPurpose {

    USER_PROFILE_IMAGE("profile", "profile/%s"),
    STEADY_CONTENT_IMAGE("steady", "steady/content/%s");

    private final String purpose;
    private final String keyPattern;

    ImageUploadPurpose(String purpose, String keyPattern) {
        this.purpose = purpose;
        this.keyPattern = keyPattern;
    }

    public static ImageUploadPurpose from(String purpose) {
        return Arrays.stream(ImageUploadPurpose.values())
                .filter(v -> v.getPurpose().equals(purpose))
                .findAny()
                .orElseThrow(() -> new InvalidValueException(NOT_SUPPORTED_PURPOSE));
    }

}
