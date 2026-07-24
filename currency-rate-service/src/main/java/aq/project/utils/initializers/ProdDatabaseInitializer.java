package aq.project.utils.initializers;

import aq.project.clients.FrankfurterRateProviderClient;
import aq.project.entities.AdjustmentFactor;
import aq.project.entities.RateProvider;
import aq.project.repositories.AdjustmentFactorRepository;
import aq.project.repositories.RateProviderRepository;
import aq.project.utils.mappers.rate.AbstractRateProviderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("prod")
public class ProdDatabaseInitializer implements ApplicationRunner {

    @Value("${application.rates.provider.default.adjustment-factor}")
    private String defaultAdjustmentFactor;

    @Autowired
    @Qualifier("frankfurter")
    private AbstractRateProviderMapper rateProviderMapper;

    @Autowired
    private FrankfurterRateProviderClient frankfurterRateProviderClient;

    @Autowired
    private RateProviderRepository rateProviderRepository;
    @Autowired
    private AdjustmentFactorRepository adjustmentFactorRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        List<RateProvider> rateProviders = new ArrayList<>(rateProviderMapper
                .getRateProvidersMap(frankfurterRateProviderClient.getProviders())
                .values());
        rateProviderRepository.saveAll(rateProviders);
        for(RateProvider rateProvider : rateProviders) {
            AdjustmentFactor adjustmentFactor = new AdjustmentFactor();
            adjustmentFactor.setRateProvider(rateProvider);
            adjustmentFactor.setFactor(BigDecimal.valueOf(Double.valueOf(defaultAdjustmentFactor)));
            adjustmentFactor.setCreatedAt(OffsetDateTime.now());
            adjustmentFactor.setModifiedAt(OffsetDateTime.now());
            adjustmentFactorRepository.save(adjustmentFactor);
        }
    }
}
