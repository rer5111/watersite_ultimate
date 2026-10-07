package org.example.p3.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Loyalty System Api",
                description = "API системы лояльности",
                version = "1.0.0",
                contact = @Contact(
                        name = "Zaharchev Nikita",
                        email = "NikitaZaharchev@gmail.com",
                        url = "https://rer5111.github.io/" // я когда-нибудь сделаю сайт и это не сейчас
                )
        )
)
public class OpenApiConfig {
    // Конфигурация для Swagger
}
