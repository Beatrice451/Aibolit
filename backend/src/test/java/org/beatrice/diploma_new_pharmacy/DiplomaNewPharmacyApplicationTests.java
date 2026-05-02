package org.beatrice.diploma_new_pharmacy;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DiplomaNewPharmacyApplicationTests {
    // Это тут не нужно. contextLoads нужен для определения того, что контекст спринга поднимается, но!
    // Он сейчас невалиден концептуально - он не воспроизводит реальное тестовое окружение.
//    @Test
//    void contextLoads() {
//    }

}
