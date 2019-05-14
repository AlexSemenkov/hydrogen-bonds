package com.asemenkov.command.exceptions;

/**
 * @author asemenkov
 * @since May 04, 2019
 */
public class CmdArgumentException extends RuntimeException {

    private static final long serialVersionUID = 1601004121125642315L;

    public CmdArgumentException() {
    }

    public CmdArgumentException(String message) {
        super(message);
    }

    public CmdArgumentException(Throwable throwable) {
        super(throwable);
    }

    public CmdArgumentException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
