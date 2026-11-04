package com.waltercraft.auth.exception;

public class PlayerServiceUnavailableException extends RuntimeException {
    public PlayerServiceUnavailableException(Throwable cause) {
        super(cause);
    }

    public PlayerServiceUnavailableException() {
        super();
    }
}
