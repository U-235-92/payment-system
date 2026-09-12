package aq.project.register_merchant.integration;

import aq.project.dto.MerchantRegistrationRequestDto;
import aq.project.entities.Merchant;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.repositories.MerchantRepository;
import aq.project.services.MerchantAdminService;
import aq.project._utils.Containers;
import aq.project._utils.ContainerPropertiesConfigurer;
import aq.project.utils.mappers.MerchantMapper;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static aq.project._utils.Entities.getInvalidUserMerchantRegistrationRequestDto;
import static aq.project._utils.Entities.getValidUserMerchantRegistrationRequestDto;

@DirtiesContext
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegisterMerchantIntegrationTest {

    private final MerchantMapper merchantMapper = MerchantMapper.INSTANCE;

    @Container
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER = Containers.POSTGRESQL;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private MerchantAdminService merchantAdminService;

    @DynamicPropertySource
    public static void dynamicConfigureProperties(DynamicPropertyRegistry registry) {
        ContainerPropertiesConfigurer.registerApplicationContextPostgresqlContainerProperties(registry, POSTGRESQL_CONTAINER);
    }

    @AfterEach
    public void tearDownRepositories() {
        merchantRepository.deleteAll();
    }

    @Test
    public void successRegisterMerchantIntegrationTest() {
//        Arrange
        MerchantRegistrationRequestDto dto = getValidUserMerchantRegistrationRequestDto();

//        Act & Assert
        Assertions.assertDoesNotThrow(() -> merchantAdminService.registerMerchant(dto));
        Merchant merchant = merchantRepository.findById(dto.getMerchantId()).orElseThrow();
        Assertions.assertEquals(merchant.getId(), dto.getMerchantId());
        Assertions.assertNotNull(merchant.getCreatedAt());
        Assertions.assertNotNull(merchant.getUpdatedAt());
    }

    @Test
    public void failRegisterMerchantOnInvalidMerchantRegistrationRequestDtoIntegrationTest() {
//        Arrange
        MerchantRegistrationRequestDto dto = getInvalidUserMerchantRegistrationRequestDto();

//        Act & Assert
        Assertions.assertThrows(ConstraintViolationException.class,
                () -> merchantAdminService.registerMerchant(dto));
    }

    @Test
    public void failRegisterMerchantOnDuplicateMerchantRegistrationRequestDtoIntegrationTest() {
//        Arrange
        MerchantRegistrationRequestDto dto = getValidUserMerchantRegistrationRequestDto();
        Merchant merchant = merchantMapper.toMerchant(dto);
        merchantRepository.save(merchant);

//        Act & Assert
        Assertions.assertThrows(EntityAlreadyExistsException.class,
                () -> merchantAdminService.registerMerchant(dto));
    }
}
