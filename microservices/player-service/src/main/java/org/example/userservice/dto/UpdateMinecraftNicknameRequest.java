package org.example.userservice.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public record UpdateMinecraftNicknameRequest(
        @NotBlank(message = "Никнейм не может быть пустым")
        @Size(min = 3, max = 16, message = "Никнейм должен содержать от 3 до 16 символов")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Никнейм может содержать только латинские буквы, цифры и знак подчеркивания")
        String minecraftNickname
) {
}
