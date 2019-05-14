package com.asemenkov.command.exceptions;

/**
 * @author asemenkov
 * @since May 04, 2019
 */
public class CmdIoException extends RuntimeException {

    private static final long serialVersionUID = 4827075795517618780L;

    public CmdIoException() {
    }

    public CmdIoException(String message) {
        super(message);
    }

    public CmdIoException(Throwable throwable) {
        super(throwable);
    }

    public CmdIoException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
