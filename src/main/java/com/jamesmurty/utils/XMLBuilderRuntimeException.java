package com.jamesmurty.utils;

/**
 * A runtime exception class used in {@link XMLBuilder2} to wrap any exceptions
 * that would otherwise lead to checked exceptions in the interface.
 *
 * @author jmurty
 *
 */
public class XMLBuilderRuntimeException extends RuntimeException {

    private static final long serialVersionUID = -635323496745601589L;

    /**
     * @param exception
     * cause exception to be wrapped
     */
    public XMLBuilderRuntimeException(Exception exception) {
        super(exception);
    }

}
