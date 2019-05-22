package com.asemenkov.command.exceptions;

/**
 * @author asemenkov
 * @since May 20, 2019
 */
public class CmdInheritanceException extends RuntimeException {

    private static final long serialVersionUID = -8007976126256338787L;

    public CmdInheritanceException() {
    }

    public CmdInheritanceException(String message) {
        super(message);
    }

    public CmdInheritanceException(Throwable throwable) {
        super(throwable);
    }

    public CmdInheritanceException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
