package com.waltercraft.skin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
@ActiveProfiles("test")
class SkinServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
