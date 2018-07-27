package com.asemenkov.gromacs.exceptions;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */
public class GmxAtomTypeException extends RuntimeException {

    private static final long serialVersionUID = -2687450129477182659L;

    public GmxAtomTypeException() {
    }

    public GmxAtomTypeException(String message) {
        super(message);
    }

    public GmxAtomTypeException(Throwable throwable) {
        super(throwable);
    }

    public GmxAtomTypeException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
