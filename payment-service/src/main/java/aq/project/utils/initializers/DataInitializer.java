package aq.project.utils.initializers;

import aq.project.dto.PaymentType;
import aq.project.entities.PaymentMethod;
import aq.project.entities.PaymentMethodDefinition;
import aq.project.entities.PaymentMethodRequiredField;
import aq.project.repositories.PaymentMethodDefinitionRepository;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.repositories.PaymentMethodRequiredFieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodDefinitionRepository paymentMethodDefinitionRepository;
    private final PaymentMethodRequiredFieldRepository paymentMethodRequiredFieldRepository;

    @Override
    public void run(
            ApplicationArguments args
    ) {
        log.info("Start to initialize [default payment method]");

        PaymentMethodDefinition savedPaymentMethodDefinition = save(getDefaultPaymentMethodDefinition());
        log.info("Initialized [default payment method definition] with id: [{}]", savedPaymentMethodDefinition.getId());

        PaymentMethodRequiredField savedPaymentMethodRequiredField = save(getDefaultPaymentMethodRequiredField());
        log.info("Initialized [default payment method required fields] with id: [{}]", savedPaymentMethodRequiredField.getId());

        PaymentMethod savedPaymentMethod = save(savedPaymentMethodDefinition, savedPaymentMethodRequiredField);
        log.info("Finish to initialize [default payment method] with id: [{}]", savedPaymentMethod.getId());
    }

    private PaymentMethodDefinition save(
            PaymentMethodDefinition paymentMethodDefinition
    ) {
        return paymentMethodDefinitionRepository.save(paymentMethodDefinition);
    }

    private PaymentMethodRequiredField save(
            PaymentMethodRequiredField paymentMethodRequiredField
    ) {
        return paymentMethodRequiredFieldRepository.save(paymentMethodRequiredField);
    }

    private PaymentMethod save(
            PaymentMethodDefinition paymentMethodDefinition,
            PaymentMethodRequiredField paymentMethodRequiredField
    ) {
        return paymentMethodRepository.save(
                getDefaultPaymentMethod(paymentMethodDefinition, paymentMethodRequiredField));
    }

    private PaymentMethod getDefaultPaymentMethod(
            PaymentMethodDefinition paymentMethodDefinition,
            PaymentMethodRequiredField paymentMethodRequiredField
    ) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(null);
        paymentMethod.setDefinition(paymentMethodDefinition);
        paymentMethod.setRequiredFields(paymentMethodRequiredField);
        paymentMethod.setCreatedAt(OffsetDateTime.now());
        paymentMethod.setUpdatedAt(OffsetDateTime.now());
        paymentMethod.setType(PaymentType.BANK_TRANSFER);
        paymentMethod.setLogo("AQ");
        return paymentMethod;
    }

    private PaymentMethodDefinition getDefaultPaymentMethodDefinition() {
        PaymentMethodDefinition paymentMethodDefinition = new PaymentMethodDefinition();
        paymentMethodDefinition.setId(null);
        return paymentMethodDefinition;
    }

    private PaymentMethodRequiredField getDefaultPaymentMethodRequiredField() {
        PaymentMethodRequiredField paymentMethodRequiredField = new PaymentMethodRequiredField();
        paymentMethodRequiredField.setId(null);
        paymentMethodRequiredField.setCreatedAt(OffsetDateTime.now());
        paymentMethodRequiredField.setUpdatedAt(OffsetDateTime.now());
        paymentMethodRequiredField.setCountryAlpha3Code("ATA");
        paymentMethodRequiredField.setCurrencyCode("USD");
        paymentMethodRequiredField.setName("Antarctica Inc.");
        paymentMethodRequiredField.setDataType("DataType");
        paymentMethodRequiredField.setValidationType("ValidationType");
        paymentMethodRequiredField.setValidationRule("ValidationRule");
        paymentMethodRequiredField.setDefaultValue("DefaultValue");
        paymentMethodRequiredField.setValuesOptions("ValuesOptions");
        paymentMethodRequiredField.setDescription("Description");
        paymentMethodRequiredField.setPlaceholder("Placeholder");
        paymentMethodRequiredField.setRepresentationName("RepresentationName");
        paymentMethodRequiredField.setLanguage("en");
        paymentMethodRequiredField.setImageUrl("https://image.aq/image");
        return paymentMethodRequiredField;
    }
}
