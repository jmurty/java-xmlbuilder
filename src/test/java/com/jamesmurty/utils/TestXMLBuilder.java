package com.jamesmurty.utils;

public class TestXMLBuilder extends BaseXMLBuilderTests {

    @Override
    public Class<? extends BaseXMLBuilder> XMLBuilderToTest() throws Exception {
        return XMLBuilder.class;
    }

    @Override
    protected boolean isRuntimeExceptionsOnly() {
        return false;
    }

}
