package aq.project.controllers;

import aq.project.controller.AdminRestControllerApi;
import aq.project.dto.MerchantRegistrationRequestDto;
import aq.project.dto.MerchantRegistrationResponseDto;
import aq.project.services.MerchantAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminRestController implements AdminRestControllerApi {

    private final MerchantAdminService merchantAdminService;

    @Override
    public ResponseEntity<MerchantRegistrationResponseDto> registerMerchant(
            MerchantRegistrationRequestDto merchantRegistrationRequestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(merchantAdminService.registerMerchant(merchantRegistrationRequestDto));
    }
}
