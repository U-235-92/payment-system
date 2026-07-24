package aq.project.utils.initializers;

import aq.project.entities.AdjustmentFactor;
import aq.project.entities.RateProvider;
import aq.project.utils.mappers.rate.FrankfurterRateProviderMapper;
import aq.project.repositories.AdjustmentFactorRepository;
import aq.project.repositories.RateProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDatabaseInitializer implements ApplicationRunner {

    @Value("${application.rates.provider.default.adjustment-factor}")
    private String defaultAdjustmentFactor;

    private final AdjustmentFactorRepository adjustmentFactorRepository;
    private final RateProviderRepository rateProviderRepository;

    private final FrankfurterRateProviderMapper frankfurterRateProviderMapper;

    @Override
    public void run(ApplicationArguments args) {
        try(DataInputStream rateProviderDis = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("currency-rate-service/src/main/resources/dev_rate_providers.json")))
        ) {
            log.info("Beginning to save AdjustmentFactors");
            String rateProvidersJsonString = new String(rateProviderDis.readAllBytes());
            Map<String, RateProvider> rateProviderMap = frankfurterRateProviderMapper.getRateProvidersMap(rateProvidersJsonString);
            rateProviderRepository.saveAll(rateProviderMap.values());
            rateProviderMap.forEach((k, v) -> {
                log.debug("Loading adjustment factor for rate provider: {}", k);
                AdjustmentFactor adjustmentFactor = new AdjustmentFactor();
                adjustmentFactor.setRateProvider(v);
                adjustmentFactor.setFactor(BigDecimal.valueOf(Double.valueOf(defaultAdjustmentFactor)));
                adjustmentFactor.setCreatedAt(OffsetDateTime.now());
                adjustmentFactor.setModifiedAt(OffsetDateTime.now());
                AdjustmentFactor saved = adjustmentFactorRepository.save(adjustmentFactor);
                log.debug("Saved adjustment factor for rate provider with id: {}", saved.getId());
            });
            log.info("AdjustmentFactors saved successfully");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
