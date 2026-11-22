package ngoctamhotel.ngoctamhotel.config;

import ngoctamhotel.ngoctamhotel.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrap {
    @Bean
    ApplicationRunner createInitialAdmin(AdminRepository repository, PasswordEncoder encoder,
            @Value("${app.admin.bootstrap-name:admin}") String name,
            @Value("${app.admin.bootstrap-password:}") String password) {
        return args -> {
            if (!password.isBlank() && repository.findByName(name).isEmpty()) {
                repository.create(name, encoder.encode(password));
            }
        };
    }
}
