package aq.project.admin.unit;

import aq.project.dto.MerchantRegistrationRequestDto;
import aq.project.entities.Merchant;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.repositories.MerchantRepository;
import aq.project.services.MerchantAdminService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static aq.project._utils.Entities.getValidUserMerchantRegistrationRequestDto;

@ExtendWith(MockitoExtension.class)
public class RegisterMerchantUnitTest {

    @Spy
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private MerchantRepository merchantRepository;

    @InjectMocks
    private MerchantAdminService merchantAdminService;

    @Test
    public void successRegisterMerchantUnitTest() {
//        Arrange
        MerchantRegistrationRequestDto dto = getValidUserMerchantRegistrationRequestDto();

        Mockito.when(merchantRepository.existsById(Mockito.anyString())).thenReturn(false);

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> merchantAdminService.registerMerchant(dto));
        Mockito.verify(merchantRepository, Mockito.times(1)).save(Mockito.any(Merchant.class));
    }

    @Test
    public void failRegisterMerchantOnMerchantExistsUnitTest() {
//        Arrange
        MerchantRegistrationRequestDto dto = getValidUserMerchantRegistrationRequestDto();

        Mockito.when(merchantRepository.existsById(Mockito.anyString())).thenReturn(true);

//        Act & Assert
        Assertions.assertThrows(EntityAlreadyExistsException.class,
                () -> merchantAdminService.registerMerchant(dto));
        Mockito.verify(merchantRepository, Mockito.never()).save(Mockito.any(Merchant.class));
    }
}
