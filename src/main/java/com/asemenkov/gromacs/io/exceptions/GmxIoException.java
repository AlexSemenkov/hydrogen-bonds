package com.asemenkov.gromacs.io.exceptions;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */
public class GmxIoException extends RuntimeException {

    private static final long serialVersionUID = -5226032301928962856L;

    public GmxIoException() {
    }

    public GmxIoException(String message) {
        super(message);
    }

    public GmxIoException(Throwable throwable) {
        super(throwable);
    }

    public GmxIoException(String message, Throwable throwable) {
        super(message, throwable);
    }

}