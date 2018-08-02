package com.asemenkov.gromacs.frame.exceptions;

/**
 * @author asemenkov
 * @since Apr 9, 2018
 */
public class GmxFrameException extends RuntimeException {

    private static final long serialVersionUID = 5564796705217495469L;

    public GmxFrameException() {
    }

    public GmxFrameException(String message) {
        super(message);
    }

    public GmxFrameException(Throwable throwable) {
        super(throwable);
    }

    public GmxFrameException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
