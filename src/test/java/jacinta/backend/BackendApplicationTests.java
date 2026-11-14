package jacinta.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:contexto;DB_CLOSE_ON_EXIT=FALSE",
                    "app.jwt.secret=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="})
class BackendApplicationTests {
    @Test
    void contextLoads() {
    }
}
