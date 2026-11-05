package com.waltercraft.notification.controller;

import com.waltercraft.notification.dto.BroadcastRequest;
import com.waltercraft.notification.dto.BroadcastResponse;
import com.waltercraft.notification.service.BroadcastService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/broadcasts")
public class BroadcastController {

    private final BroadcastService broadcastService;

    public BroadcastController(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @GetMapping
    public List<BroadcastResponse> listHistory() {
        return broadcastService.listHistory();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BroadcastResponse dispatch(@Valid @RequestBody BroadcastRequest request) {
        return broadcastService.dispatch(request);
    }
}
