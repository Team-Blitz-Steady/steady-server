package dev.steady.storage.controller;

import dev.steady.global.auth.Auth;
import dev.steady.global.auth.UserInfo;
import dev.steady.storage.ImageUploadPurpose;
import dev.steady.storage.service.StorageService;
import dev.steady.user.dto.response.PutObjectUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storage/image")
public class StorageImageController {

    private final StorageService storageService;

    @GetMapping("/{purpose}")
    public ResponseEntity<PutObjectUrlResponse> getImageUploadUrl(@PathVariable String purpose,
                                                                  @RequestParam String fileName,
                                                                  @Auth UserInfo userInfo) {
        String keyPattern = ImageUploadPurpose.from(purpose).getKeyPattern();
        PutObjectUrlResponse response = storageService.generatePutObjectUrl(fileName, keyPattern);
        return ResponseEntity.ok(response);
    }

}
