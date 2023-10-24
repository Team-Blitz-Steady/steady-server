package dev.steady.user.api;

import dev.steady.steady.application.SteadyService;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/steadies")
public class SteadyController {

    private final SteadyService steadyService;

    @PostMapping
    public ResponseEntity<Void> createSteady(@RequestBody SteadyCreateRequest request) {
        Long steadyId = steadyService.create(request);
        return ResponseEntity.created(URI.create(String.format("/api/v1/steadies/%d/detail", steadyId))).build();
    }

}
