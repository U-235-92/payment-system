package aq.project.initializers;

import aq.project.entities.Merchant;
import aq.project.repositories.MerchantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements ApplicationRunner {

    private final MerchantRepository merchantRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Merchant transactionServiceMerchant = getTransactionServiceMerchant();
        Merchant paymentServiceMerchant = getPaymentServiceMerchant();

        merchantRepository.save(transactionServiceMerchant);
        merchantRepository.save(paymentServiceMerchant);
    }

    private Merchant getTransactionServiceMerchant() {
        Merchant merchant = new Merchant();
        merchant.setMerchantId("transaction-service");
        merchant.setSecretKey(passwordEncoder.encode("secret"));
        merchant.setName("transaction-service");
        return merchant;
    }

    private Merchant getPaymentServiceMerchant() {
        Merchant merchant = new Merchant();
        merchant.setMerchantId("payment-service");
        merchant.setSecretKey(passwordEncoder.encode("secret"));
        merchant.setName("payment-service");
        return merchant;
    }
}