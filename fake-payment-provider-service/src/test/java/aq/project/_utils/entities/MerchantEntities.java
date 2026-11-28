package aq.project._utils.entities;

import aq.project.entities.Merchant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class MerchantEntities {

    public static Merchant getMerchant() {
        Merchant merchant = new Merchant();

        merchant.setId(null);
        merchant.setMerchantId(UUID.randomUUID().toString());
        merchant.setSecretKey(UUID.randomUUID().toString());
        merchant.setName(UUID.randomUUID().toString());

        return merchant;
    }
}
