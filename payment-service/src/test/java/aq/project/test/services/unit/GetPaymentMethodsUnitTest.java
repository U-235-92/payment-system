package aq.project.test.services.unit;

import aq.project.entities.PaymentMethod;
import aq.project.repositories.PaymentMethodRepository;
import aq.project.services.PaymentService;
import aq.project.utils.mappers.PaymentMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static aq.project._utils.entities.PaymentServiceEntities.getValidPaymentMethod;

@ExtendWith(MockitoExtension.class)
public class GetPaymentMethodsUnitTest {

    @Spy
    private PaymentMapper paymentMapper = PaymentMapper.INSTANCE;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    public void successGetPaymentMethods() {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";

        PaymentMethod paymentMethod = getValidPaymentMethod();

        Mockito.when(paymentMethodRepository.findAllByCurrencyCodeAndCountryCode(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(List.of(paymentMethod));

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentService.getPaymentMethods(currencyCode, countryCode));
    }

    @Test
    public void successGetPaymentMethodsOnEmptyPaymentMethodList() {
//        Arrange
        String currencyCode = "USD";
        String countryCode = "ATA";

        Mockito.when(paymentMethodRepository.findAllByCurrencyCodeAndCountryCode(Mockito.any(String.class), Mockito.any(String.class)))
                .thenReturn(List.of());

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> paymentService.getPaymentMethods(currencyCode, countryCode));
    }
}
