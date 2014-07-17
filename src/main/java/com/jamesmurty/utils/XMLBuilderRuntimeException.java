package com.jamesmurty.utils;

/**
 * A runtime exception class used in {@link XMLBuilder2} to wrap any exceptions
 * that would otherwise lead to checked exceptions in the interface.
 *
 * @author jmurty
 *
 */
public class XMLBuilderRuntimeException extends RuntimeException {

    public XMLBuilderRuntimeException(Exception exception) {
        super(exception);
    }

}
