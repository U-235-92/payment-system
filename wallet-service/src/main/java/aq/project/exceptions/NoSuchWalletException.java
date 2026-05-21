package aq.project.exceptions;

import java.util.UUID;

public class NoSuchWalletException extends Exception {

    public NoSuchWalletException(String walletId) {
        super("No such wallet with id: " + walletId);
    }
}
