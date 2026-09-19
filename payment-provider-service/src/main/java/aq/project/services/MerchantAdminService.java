package aq.project.services;

import aq.project.dto.MerchantRegistrationRequestDto;
import aq.project.dto.MerchantRegistrationResponseDto;
import aq.project.entities.Merchant;
import aq.project.exceptions.EntityAlreadyExistsException;
import aq.project.repositories.MerchantRepository;
import aq.project.utils.mappers.MerchantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MerchantAdminService {

    private final MerchantRepository merchantRepository;

    private final PasswordEncoder passwordEncoder;

    private final MerchantMapper merchantMapper = MerchantMapper.INSTANCE;

    @Transactional
    public MerchantRegistrationResponseDto registerMerchant(MerchantRegistrationRequestDto requestDto) {
        if(merchantRepository.existsById(requestDto.getMerchantId()))
            throw new EntityAlreadyExistsException(String.format("Merchant with id: [%s] already exists",
                    requestDto.getMerchantId()));

        Merchant merchant = merchantMapper.toMerchant(requestDto);
        merchant.setSecretKey(passwordEncoder.encode(requestDto.getSecretKey()));

        Merchant saved = merchantRepository.save(merchant);

        return merchantMapper.toResponseDto(saved);
    }
}
