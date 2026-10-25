package com.futureme;
 
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
 
/** Smoke test: the whole Spring application context starts without errors. */
@SpringBootTest
@ActiveProfiles("test")
class FutureMeApplicationTests {
 
    @Test
    void contextLoads() {
    }
}
