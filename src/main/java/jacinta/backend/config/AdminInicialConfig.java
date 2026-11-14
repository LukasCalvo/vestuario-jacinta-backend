package jacinta.backend.config;

import jacinta.backend.service.AdminInicialService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.admin-inicial.enabled", havingValue = "true", matchIfMissing = true)
public class AdminInicialConfig implements ApplicationRunner {
    private final AdminInicialService adminInicial;

    @Override
    public void run(ApplicationArguments args) {
        adminInicial.crearSiNoExiste();
    }
}
