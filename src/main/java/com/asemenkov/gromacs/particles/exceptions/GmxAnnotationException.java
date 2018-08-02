package com.asemenkov.gromacs.particles.exceptions;

/**
 * @author asemenkov
 * @since Apr 19, 2018
 */
public class GmxAnnotationException extends RuntimeException {

    private static final long serialVersionUID = -4097786486682378891L;

    public GmxAnnotationException() {
    }

    public GmxAnnotationException(String message) {
        super(message);
    }

    public GmxAnnotationException(Throwable throwable) {
        super(throwable);
    }

    public GmxAnnotationException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
