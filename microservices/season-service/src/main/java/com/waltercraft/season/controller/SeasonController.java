package com.waltercraft.season.controller;

import com.waltercraft.season.dto.SeasonCreateRequest;
import com.waltercraft.season.dto.SeasonResponse;
import com.waltercraft.season.dto.SeasonUpdateRequest;
import com.waltercraft.season.service.SeasonService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1")
public class SeasonController {

    private final SeasonService seasonService;

    public SeasonController(SeasonService seasonService) {
        this.seasonService = seasonService;
    }

    @GetMapping("/seasons")
    public ResponseEntity<PagedModel<SeasonResponse>> listSeasons(
            @ParameterObject @PageableDefault(size = 50, sort = {"displayOrder", "id"}) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .body(new PagedModel<>(seasonService.listPublic(pageable)));
    }

    @GetMapping("/seasons/{id}")
    public SeasonResponse getSeason(@PathVariable Long id) {
        return seasonService.get(id);
    }

    @GetMapping("/admin/seasons")
    public PagedModel<SeasonResponse> listAllSeasons(
            @ParameterObject @PageableDefault(size = 50, sort = {"displayOrder", "id"}) Pageable pageable) {
        return new PagedModel<>(seasonService.listAll(pageable));
    }

    @PostMapping("/admin/seasons")
    @ResponseStatus(HttpStatus.CREATED)
    public SeasonResponse createSeason(@Valid @RequestBody SeasonCreateRequest request) {
        return seasonService.create(request);
    }

    @PutMapping("/admin/seasons/{id}")
    public SeasonResponse updateSeason(@PathVariable Long id, @Valid @RequestBody SeasonUpdateRequest request) {
        return seasonService.update(id, request);
    }

    @DeleteMapping("/admin/seasons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeason(@PathVariable Long id) {
        seasonService.delete(id);
    }
}
