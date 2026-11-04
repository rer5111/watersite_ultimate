package com.waltercraft.skin.controller;

import com.waltercraft.skin.service.SkinService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SkinController.class)
class SkinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SkinService skinService;

    @Test
    void getFullByUuid_ReturnsPngWhenFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        byte[] fakeImage = new byte[]{10, 20, 30};
        when(skinService.getFullByUuid(uuid)).thenReturn(Optional.of(fakeImage));

        mockMvc.perform(get("/api/skin/{uuid}/full", uuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("Cache-Control", "max-age=86400, public"))
                .andExpect(content().bytes(fakeImage));
    }

    @Test
    void getFullBySubject_ReturnsPngWhenFound() throws Exception {
        byte[] fakeImage = new byte[]{11, 22, 33};
        when(skinService.getFullBySubject("X-Ghost")).thenReturn(Optional.of(fakeImage));

        mockMvc.perform(get("/api/skin/subject/{subject}/full", "X-Ghost"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(fakeImage));
    }

    @Test
    void getFaceByUuid_ReturnsPngWhenFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        byte[] fakeImage = new byte[]{1, 2, 3};
        when(skinService.getFaceByUuid(uuid)).thenReturn(Optional.of(fakeImage));

        mockMvc.perform(get("/api/skin/{uuid}/face", uuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("Cache-Control", "max-age=86400, public"))
                .andExpect(content().bytes(fakeImage));
    }

    @Test
    void getFaceByUuid_Returns404WhenNotFound() throws Exception {
        when(skinService.getFaceByUuid(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/skin/{uuid}/face", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFaceByUuid_Returns400OnInvalidUuid() throws Exception {
        mockMvc.perform(get("/api/skin/{uuid}/face", "invalid-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBustByUuid_ReturnsPngWhenFound() throws Exception {
        UUID uuid = UUID.randomUUID();
        byte[] fakeImage = new byte[]{4, 5, 6};
        when(skinService.getBustByUuid(uuid)).thenReturn(Optional.of(fakeImage));

        mockMvc.perform(get("/api/skin/{uuid}/bust", uuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(fakeImage));
    }

    @Test
    void getFaceByUsername_ReturnsPngWhenFound() throws Exception {
        byte[] fakeImage = new byte[]{7, 8, 9};
        when(skinService.getFaceByUsername("kotyarendj")).thenReturn(Optional.of(fakeImage));

        mockMvc.perform(get("/api/skin/username/{username}/face", "kotyarendj"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(fakeImage));
    }
}
