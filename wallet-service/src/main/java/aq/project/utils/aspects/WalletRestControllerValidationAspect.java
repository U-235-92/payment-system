package aq.project.utils.aspects;

import aq.project.dto.CreateWalletRequestDto;
import jakarta.validation.Valid;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Aspect
@Component
@Validated
public class WalletRestControllerValidationAspect {

    @Before("execution(* aq.project.controllers.WalletRestController.createWallet(..)) && args(createWalletRequestDTO)")
    public void validateCreateWalletArguments(@Valid CreateWalletRequestDto createWalletRequestDTO) {}
}
