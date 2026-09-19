package aq.project.utils.initializers;

import aq.project.dto.UserRole;
import aq.project.entities.Merchant;
import aq.project.repositories.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDatabaseInitializer implements ApplicationRunner {

    private final MerchantRepository merchantRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Merchant merchant = getDevMerchant();
        merchantRepository.save(merchant);
    }

    private Merchant getDevMerchant() {
        Merchant merchant = new Merchant();
        merchant.setId("transaction-service");
        merchant.setSecretKey(passwordEncoder.encode("secret"));
        merchant.setName("transaction-service");
        merchant.setCreatedAt(OffsetDateTime.now());
        merchant.setUpdatedAt(OffsetDateTime.now());
        merchant.setRole(UserRole.USER);
        return merchant;
    }
}
