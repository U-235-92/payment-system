package aq.project.security;

import aq.project.entities.Merchant;
import aq.project.repositories.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class MerchantDetailsService implements UserDetailsService {

    private final MerchantRepository merchantRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        Merchant merchant = merchantRepository.findByMerchantId(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return User.withUsername(merchant.getMerchantId())
                .password(merchant.getSecretKey())
                .build();
    }
}
