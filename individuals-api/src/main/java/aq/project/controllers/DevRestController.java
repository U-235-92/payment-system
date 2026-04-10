package aq.project.controllers;

import aq.project.dto.AddressDTO;
import aq.project.dto.CountryDTO;
import aq.project.dto.UpdateIndividualDataDTO;
import aq.project.dto.UpdateUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Profile("dev")
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevRestController {

    private static final String ACTUAL_USER_KEYCLOAK_ID = "c0391ed2-80b5-400c-8fd2-4d374acad407"; // Copied from Keycloak Admin-CLI (User Alice)

    @PostMapping("/try-update-user")
    public Mono<Void> tryUpdateUser() {
        CountryDTO countryDTO = getValidCountryDTO();
        AddressDTO addressDTO = getValidAddressDTO(countryDTO);
        UpdateIndividualDataDTO updateIndividualDataDTO = getValidUpdateIndividualDataDTO(addressDTO);
        UpdateUserDTO updateUserDTO = getValidUpdateUserDTO(updateIndividualDataDTO);

        String uri = String.format("http://localhost:%s", 8081);
        WebClient webClient = WebClient.builder().baseUrl(uri).build();
        return webClient.patch()
                .uri("/gateway/api/user/update-user")
                .bodyValue(updateUserDTO)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(System.out::println)
                .doOnSuccess(System.out::println);
    }

    private CountryDTO getValidCountryDTO() {
        CountryDTO countryDTO = new CountryDTO();
        countryDTO.setName("Russia");
        countryDTO.setCode("RU");
        return countryDTO;
    }

    private AddressDTO getValidAddressDTO(CountryDTO countryDTO) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCountry(countryDTO);
        addressDTO.setState("updated_state");
        addressDTO.setCity("updated_city");
        addressDTO.setAddress("updated_address");
        addressDTO.setZipCode("updated_zipcode");
        return addressDTO;
    }

    private UpdateIndividualDataDTO getValidUpdateIndividualDataDTO(AddressDTO addressDTO) {
        UpdateIndividualDataDTO updateIndividualDataDTO = new UpdateIndividualDataDTO();
        updateIndividualDataDTO.setKeycloakUserId(ACTUAL_USER_KEYCLOAK_ID);
        updateIndividualDataDTO.setFirstName("updatedFirstName");
        updateIndividualDataDTO.setLastName("updatedLastName");
        updateIndividualDataDTO.phoneNumber("9876543210");
        updateIndividualDataDTO.setPassportNumber("9876543210");
        updateIndividualDataDTO.setAddress(addressDTO);
        return updateIndividualDataDTO;
    }

    private UpdateUserDTO getValidUpdateUserDTO(UpdateIndividualDataDTO updateIndividualDataDTO) {
        return new UpdateUserDTO()
                .keycloakUserId(ACTUAL_USER_KEYCLOAK_ID)
                .password("password")
                .confirmPassword("password")
                .individualData(updateIndividualDataDTO);
    }
}
