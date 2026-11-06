package com.waltercraft.skin.controller;

import com.waltercraft.skin.service.SkinService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.Pattern;

import java.time.Duration;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/api/skin")
public class SkinController {

    private static final String SKIN_SUBJECT_PATTERN = "^(?:[A-Za-z0-9_]{1,16}|X-[A-Za-z0-9_]{1,14})$";

    private final SkinService skinService;

    public SkinController(SkinService skinService) {
        this.skinService = skinService;
    }

    @GetMapping(value = "/{uuid}/full", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getFullByUuid(@PathVariable UUID uuid) {
        return skinService.getFullByUuid(uuid)
                .map(this::pngResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/subject/{subject}/full", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getFullBySubject(
            @PathVariable @Pattern(regexp = SKIN_SUBJECT_PATTERN) String subject) {
        return skinService.getFullBySubject(subject)
                .map(this::pngResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/subject/{subject}/bust", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getBustBySubject(
            @PathVariable @Pattern(regexp = SKIN_SUBJECT_PATTERN) String subject) {
        return skinService.getBustBySubject(subject)
                .map(this::pngResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/subject/{subject}/face", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getFaceBySubject(
            @PathVariable @Pattern(regexp = SKIN_SUBJECT_PATTERN) String subject) {
        return skinService.getFaceBySubject(subject)
                .map(this::pngResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{uuid}/bust", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getBustByUuid(@PathVariable UUID uuid) {
        return skinService.getBustByUuid(uuid)
                .map(this::pngResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{uuid}/face", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getFaceByUuid(@PathVariable UUID uuid) {
        return skinService.getFaceByUuid(uuid)
                .map(this::pngResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/username/{username}/face", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getFaceByUsername(
            @PathVariable @Pattern(regexp = SKIN_SUBJECT_PATTERN) String username) {
        return skinService.getFaceByUsername(username)
                .map(this::pngResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<byte[]> pngResponse(byte[] bytes) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePublic())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length))
                .body(bytes);
    }
}
