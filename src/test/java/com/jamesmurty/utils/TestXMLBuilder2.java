package com.jamesmurty.utils;

public class TestXMLBuilder2 extends BaseXMLBuilderTests {

    @Override
    public Class<? extends BaseXMLBuilder> XMLBuilderToTest() throws Exception {
        return XMLBuilder2.class;
    }

    @Override
    protected boolean isRuntimeExceptionsOnly() {
        return true;
    }

}
