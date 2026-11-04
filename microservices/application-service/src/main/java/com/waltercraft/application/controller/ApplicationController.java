package com.waltercraft.application.controller;

import com.waltercraft.application.dto.ApplicationCreateRequest;
import com.waltercraft.application.dto.ApplicationResponse;
import com.waltercraft.application.dto.ApplicationUpdateRequest;
import com.waltercraft.application.service.ApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Validated
@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public PagedModel<ApplicationResponse> listApplications(
            @PageableDefault(size = 25, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication
    ) {
        return new PagedModel<>(applicationService.list(pageable, authentication));
    }

    @GetMapping("/{id}")
    public ApplicationResponse getApplication(@PathVariable @Positive long id, Authentication authentication) {
        return applicationService.get(id, authentication);
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody ApplicationCreateRequest request,
            Authentication authentication
    ) {
        ApplicationResponse created = applicationService.create(request, authentication);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ApplicationResponse updateApplication(
            @PathVariable @Positive long id,
            @Valid @RequestBody ApplicationUpdateRequest request
    ) {
        return applicationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplication(@PathVariable @Positive long id, Authentication authentication) {
        applicationService.delete(id, authentication);
    }
}
