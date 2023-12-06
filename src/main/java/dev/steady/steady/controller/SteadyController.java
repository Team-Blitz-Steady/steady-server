package dev.steady.steady.controller;

import dev.steady.application.dto.response.SliceResponse;
import dev.steady.global.auth.Auth;
import dev.steady.global.auth.UserInfo;
import dev.steady.steady.domain.SteadyStatus;
import dev.steady.steady.dto.SearchConditionDto;
import dev.steady.steady.dto.request.SteadyCreateRequest;
import dev.steady.steady.dto.request.SteadyPageRequest;
import dev.steady.steady.dto.request.SteadyQuestionUpdateRequest;
import dev.steady.steady.dto.request.SteadySearchRequest;
import dev.steady.steady.dto.request.SteadyUpdateRequest;
import dev.steady.steady.dto.response.MySteadyResponse;
import dev.steady.steady.dto.response.PageResponse;
import dev.steady.steady.dto.response.ParticipantsResponse;
import dev.steady.steady.dto.response.SteadyDetailResponse;
import dev.steady.steady.dto.response.SteadyQuestionsResponse;
import dev.steady.steady.dto.response.SteadySearchResponse;
import dev.steady.steady.service.SteadyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/steadies")
public class SteadyController {

    private final SteadyService steadyService;

    @PostMapping
    public ResponseEntity<Void> createSteady(@RequestBody @Valid SteadyCreateRequest request,
                                             @Auth UserInfo userInfo) {
        Long steadyId = steadyService.create(request, userInfo);
        return ResponseEntity.created(URI.create(String.format("/api/v1/steadies/%d", steadyId))).build();
    }

    @GetMapping("/my")
    public ResponseEntity<SliceResponse<MySteadyResponse>> findMySteadies(@RequestParam(required = false) SteadyStatus status,
                                                                          @Auth UserInfo userInfo,
                                                                          SteadyPageRequest request
    ) {
        Pageable pageable = request.toPageable();

        SliceResponse<MySteadyResponse> response = steadyService.findMySteadies(status, userInfo, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<SteadySearchResponse>> getSteadies(@Auth(required = false) UserInfo userInfo,
                                                                          SteadySearchRequest request) {
        SearchConditionDto condition = SearchConditionDto.from(request);
        Pageable pageable = request.toPageable();
        PageResponse<SteadySearchResponse> response = steadyService.getSteadies(userInfo, condition, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<PageResponse<SteadySearchResponse>> test(@Auth(required = false) UserInfo userInfo,
                                                SteadySearchRequest request) {
        SearchConditionDto condition = SearchConditionDto.from(request);
        Pageable pageable = request.toPageable();
        PageResponse<SteadySearchResponse> test = steadyService.test(userInfo, condition, pageable);
        return ResponseEntity.ok(test);
    }

    @GetMapping("/{steadyId}")
    public ResponseEntity<SteadyDetailResponse> getDetailSteady(@PathVariable Long steadyId,
                                                                @Auth(required = false) UserInfo userInfo) {
        SteadyDetailResponse response = steadyService.getDetailSteady(steadyId, userInfo);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{steadyId}/participants")
    public ResponseEntity<ParticipantsResponse> getSteadyParticipants(@PathVariable Long steadyId) {
        ParticipantsResponse response = steadyService.getSteadyParticipants(steadyId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{steadyId}")
    public ResponseEntity<Void> updateSteady(@PathVariable Long steadyId,
                                             @RequestBody @Valid SteadyUpdateRequest request,
                                             @Auth UserInfo userInfo) {
        steadyService.updateSteady(steadyId, request, userInfo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{steadyId}/questions")
    public ResponseEntity<SteadyQuestionsResponse> getSteadyQuestions(@PathVariable Long steadyId) {
        SteadyQuestionsResponse response = steadyService.getSteadyQuestions(steadyId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{steadyId}/questions")
    public ResponseEntity<Void> updateSteadyQuestions(@PathVariable Long steadyId,
                                                      @RequestBody @Valid SteadyQuestionUpdateRequest request,
                                                      @Auth UserInfo userInfo) {
        steadyService.updateSteadyQuestions(steadyId, request, userInfo);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{steadyId}/{memberId}")
    public ResponseEntity<Void> expelParticipant(@PathVariable Long steadyId,
                                                 @PathVariable Long memberId,
                                                 @Auth UserInfo userInfo) {
        steadyService.expelParticipant(steadyId, memberId, userInfo);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{steadyId}/promote")
    public ResponseEntity<Void> promoteSteady(@PathVariable Long steadyId, @Auth UserInfo userInfo) {
        steadyService.promoteSteady(steadyId, userInfo);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{steadyId}/finish")
    public ResponseEntity<Void> finishSteady(@PathVariable Long steadyId, @Auth UserInfo userInfo) {
        steadyService.finishSteady(steadyId, userInfo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{steadyId}")
    public ResponseEntity<Void> deleteSteady(@PathVariable Long steadyId, @Auth UserInfo userInfo) {
        steadyService.deleteSteady(steadyId, userInfo);
        return ResponseEntity.noContent().build();
    }

}
