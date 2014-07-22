/*
 * Copyright 2008-2014 James Murty (www.jamesmurty.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This code is available from the GitHub code repository at:
 * https://github.com/jmurty/java-xmlbuilder
 */
package com.jamesmurty.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Properties;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML Builder is a utility that creates simple XML documents using relatively
 * sparse Java code. It is intended to allow for quick and painless creation of
 * XML documents where you might otherwise be tempted to use concatenated
 * strings, rather than face the tedium and verbosity of coding with
 * JAXP (http://jaxp.dev.java.net/).
 * <p>
 * Internally, XML Builder uses JAXP to build a standard W3C
 * {@link org.w3c.dom.Document} model (DOM) that you can easily export as a
 * string, or access and manipulate further if you have special requirements.
 * </p>
 * <p>
 * The XMLBuilder2 class serves as a wrapper of {@link org.w3c.dom.Element} nodes,
 * and provides a number of utility methods that make it simple to
 * manipulate the underlying element and the document to which it belongs.
 * In essence, this class performs dual roles: it represents a specific XML
 * node, and also allows manipulation of the entire underlying XML document.
 * The platform's default {@link DocumentBuilderFactory} and
 * {@link DocumentBuilder} classes are used to build the document.
 * </p>
 * <p>
 * XMLBuilder2 has an feature set to the original XMLBuilder, but only ever
 * throws runtime exceptions (as opposed to checked exceptions). Any internal
 * checked exceptions are caught and wrapped in an
 * {@link XMLBuilderRuntimeException} object.
 * </p>
 *
 * @author James Murty
 */
public final class XMLBuilder2 extends BaseXMLBuilder {

    /**
     * Construct a new builder object that wraps the given XML document.
     * This constructor is for internal use only.
     *
     * @param xmlDocument
     * an XML document that the builder will manage and manipulate.
     */
    protected XMLBuilder2(Document xmlDocument) {
        super(xmlDocument);
    }

    /**
     * Construct a new builder object that wraps the given XML document and node.
     * This constructor is for internal use only.
     *
     * @param myNode
     * the XML node that this builder node will wrap. This node may
     * be part of the XML document, or it may be a new element that is to be
     * added to the document.
     * @param parentNode
     * If not null, the given myElement will be appended as child node of the
     * parentNode node.
     */
    protected XMLBuilder2(Node myNode, Node parentNode) {
        super(myNode, parentNode);
    }

    private static RuntimeException wrapExceptionAsRuntimeException(Exception e) {
        // Don't wrap (or re-wrap) runtime exceptions.
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new XMLBuilderRuntimeException(e);
        }
    }

    /**
     * Construct a builder for new XML document with a default namespace.
     * The document will be created with the given root element, and the builder
     * returned by this method will serve as the starting-point for any further
     * document additions.
     *
     * @param name
     * the name of the document's root element.
     * @param namespaceURI
     * default namespace URI for document, ignored if null or empty.
     * @return
     * a builder node that can be used to add more nodes to the XML document.
     * @throws XMLBuilderRuntimeException
     * to wrap {@link ParserConfigurationException}
     */
    public static XMLBuilder2 create(String name, String namespaceURI)
    {
        try {
            return new XMLBuilder2(createDocumentImpl(name, namespaceURI));
        } catch (ParserConfigurationException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

    /**
     * Construct a builder for new XML document. The document will be created
     * with the given root element, and the builder returned by this method
     * will serve as the starting-point for any further document additions.
     *
     * @param name
     * the name of the document's root element.
     * @return
     * a builder node that can be used to add more nodes to the XML document.
     * @throws XMLBuilderRuntimeException
     * to wrap {@link ParserConfigurationException}
     */
    public static XMLBuilder2 create(String name)
    {
        return create(name, null);
    }

    /**
     * Construct a builder from an existing XML document. The provided XML
     * document will be parsed and an XMLBuilder2 object referencing the
     * document's root element will be returned.
     *
     * @param inputSource
     * an XML document input source that will be parsed into a DOM.
     * @return
     * a builder node that can be used to add more nodes to the XML document.
     * @throws XMLBuilderRuntimeException
     * to wrap {@link ParserConfigurationException}, {@link SAXException},
     * {@link IOException}
     */
    public static XMLBuilder2 parse(InputSource inputSource)
    {
        try {
            return new XMLBuilder2(parseDocumentImpl(inputSource));
        } catch (ParserConfigurationException e) {
            throw wrapExceptionAsRuntimeException(e);
        } catch (SAXException e) {
            throw wrapExceptionAsRuntimeException(e);
        } catch (IOException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

    /**
     * Construct a builder from an existing XML document string.
     * The provided XML document will be parsed and an XMLBuilder2
     * object referencing the document's root element will be returned.
     *
     * @param xmlString
     * an XML document string that will be parsed into a DOM.
     * @return
     * a builder node that can be used to add more nodes to the XML document.
     */
    public static XMLBuilder2 parse(String xmlString)
    {
        return XMLBuilder2.parse(new InputSource(new StringReader(xmlString)));
    }

    /**
     * Construct a builder from an existing XML document file.
     * The provided XML document will be parsed and an XMLBuilder2
     * object referencing the document's root element will be returned.
     *
     * @param xmlFile
     * an XML document file that will be parsed into a DOM.
     * @return
     * a builder node that can be used to add more nodes to the XML document.
     * @throws XMLBuilderRuntimeException
     * to wrap {@link ParserConfigurationException}, {@link SAXException},
     * {@link IOException}, {@link FileNotFoundException}
     */
    public static XMLBuilder2 parse(File xmlFile)
    {
        try {
            return XMLBuilder2.parse(new InputSource(new FileReader(xmlFile)));
        } catch (FileNotFoundException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

    /**
     * @throws XMLBuilderRuntimeException
     * to wrap {@link XPathExpressionException}
     */
    @Override
    public XMLBuilder2 stripWhitespaceOnlyTextNodes()
    {
        try {
            super.stripWhitespaceOnlyTextNodesImpl();
            return this;
        } catch (XPathExpressionException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

    @Override
    public XMLBuilder2 importXMLBuilder(BaseXMLBuilder builder) {
        super.importXMLBuilderImpl(builder);
        return this;
    }

    @Override
    public XMLBuilder2 root() {
        return new XMLBuilder2(getDocument());
    }

    /**
     * @throws XMLBuilderRuntimeException
     * to wrap {@link XPathExpressionException}
     */
    @Override
    public XMLBuilder2 xpathFind(String xpath, NamespaceContext nsContext)
    {
        try {
            Node foundNode = super.xpathFindImpl(xpath, nsContext);
            return new XMLBuilder2(foundNode, null);
        } catch (XPathExpressionException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

    @Override
    public XMLBuilder2 xpathFind(String xpath) {
        return xpathFind(xpath, null);
    }

    @Override
    public XMLBuilder2 element(String name) {
        String namespaceURI = super.lookupNamespaceURIImpl(name);
        return element(name, namespaceURI);
    }

    @Override
    public XMLBuilder2 elem(String name) {
        return element(name);
    }

    @Override
    public XMLBuilder2 e(String name) {
        return element(name);
    }

    @Override
    public XMLBuilder2 element(String name, String namespaceURI) {
        Element elem = super.elementImpl(name, namespaceURI);
        return new XMLBuilder2(elem, this.getElement());
    }

    @Override
    public XMLBuilder2 elementBefore(String name) {
        Element newElement = super.elementBeforeImpl(name);
        return new XMLBuilder2(newElement, null);
    }

    @Override
    public XMLBuilder2 elementBefore(String name, String namespaceURI) {
        Element newElement = super.elementBeforeImpl(name, namespaceURI);
        return new XMLBuilder2(newElement, null);
    }

    @Override
    public XMLBuilder2 attribute(String name, String value) {
        super.attributeImpl(name, value);
        return this;
    }

    @Override
    public XMLBuilder2 attr(String name, String value) {
        return attribute(name, value);
    }

    @Override
    public XMLBuilder2 a(String name, String value) {
        return attribute(name, value);
    }


    @Override
    public XMLBuilder2 text(String value, boolean replaceText) {
        super.textImpl(value, replaceText);
        return this;
    }

    @Override
    public XMLBuilder2 text(String value) {
        return this.text(value, false);
    }

    @Override
    public XMLBuilder2 t(String value) {
        return text(value);
    }

    @Override
    public XMLBuilder2 cdata(String data) {
        super.cdataImpl(data);
        return this;
    }

    @Override
    public XMLBuilder2 data(String data) {
        return cdata(data);
    }

    @Override
    public XMLBuilder2 d(String data) {
        return cdata(data);
    }

    @Override
    public XMLBuilder2 cdata(byte[] data) {
        super.cdataImpl(data);
        return this;
    }

    @Override
    public XMLBuilder2 data(byte[] data) {
        return cdata(data);
    }

    @Override
    public XMLBuilder2 d(byte[] data) {
        return cdata(data);
    }

    @Override
    public XMLBuilder2 comment(String comment) {
        super.commentImpl(comment);
        return this;
    }

    @Override
    public XMLBuilder2 cmnt(String comment) {
        return comment(comment);
    }

    @Override
    public XMLBuilder2 c(String comment) {
        return comment(comment);
    }

    @Override
    public XMLBuilder2 instruction(String target, String data) {
        super.instructionImpl(target, data);
        return this;
    }

    @Override
    public XMLBuilder2 inst(String target, String data) {
        return instruction(target, data);
    }

    @Override
    public XMLBuilder2 i(String target, String data) {
        return instruction(target, data);
    }

    @Override
    public XMLBuilder2 insertInstruction(String target, String data) {
        super.insertInstructionImpl(target, data);
        return this;
    }

    @Override
    public XMLBuilder2 reference(String name) {
        super.referenceImpl(name);
        return this;
    }

    @Override
    public XMLBuilder2 ref(String name) {
        return reference(name);
    }

    @Override
    public XMLBuilder2 r(String name) {
        return reference(name);
    }

    @Override
    public XMLBuilder2 namespace(String prefix, String namespaceURI) {
        super.namespaceImpl(prefix, namespaceURI);
        return this;
    }

    @Override
    public XMLBuilder2 ns(String prefix, String namespaceURI) {
        return attribute(prefix, namespaceURI);
    }

    @Override
    public XMLBuilder2 namespace(String namespaceURI) {
        this.namespace(null, namespaceURI);
        return this;
    }

    @Override
    public XMLBuilder2 ns(String namespaceURI) {
        return namespace(namespaceURI);
    }

    @Override
    public XMLBuilder2 up(int steps) {
        Node currNode = super.upImpl(steps);
        if (currNode instanceof Document) {
            return new XMLBuilder2((Document) currNode);
        } else {
            return new XMLBuilder2(currNode, null);
        }
    }

    @Override
    public XMLBuilder2 up() {
        return up(1);
    }

    @Override
    public XMLBuilder2 document() {
        return new XMLBuilder2(getDocument(), null);
    }

    /**
     * @throws XMLBuilderRuntimeException
     * to wrap {@link TransformerException}
     *
     */
    @Override
    public String asString() {
        try {
            return super.asString();
        } catch (TransformerException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

    /**
     * @throws XMLBuilderRuntimeException
     * to wrap {@link TransformerException}
     *
     */
    @Override
    public String asString(Properties properties) {
        try {
            return super.asString(properties);
        } catch (TransformerException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

    /**
     * @throws XMLBuilderRuntimeException
     * to wrap {@link TransformerException}
     *
     */
    @Override
    public String elementAsString() {
        try {
            return super.elementAsString();
        } catch (TransformerException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

    /**
     * @throws XMLBuilderRuntimeException
     * to wrap {@link TransformerException}
     *
     */
    @Override
    public String elementAsString(Properties outputProperties) {
        try {
            return super.elementAsString(outputProperties);
        } catch (TransformerException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

    /**
     * @throws XMLBuilderRuntimeException
     * to wrap {@link TransformerException}
     *
     */
    @Override
    public void toWriter(boolean wholeDocument, Writer writer, Properties outputProperties)
    {
        try {
            super.toWriter(wholeDocument, writer, outputProperties);
        } catch (TransformerException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

    /**
     * @throws XMLBuilderRuntimeException
     * to wrap {@link TransformerException}
     *
     */
    @Override
    public void toWriter(Writer writer, Properties outputProperties)
    {
        try {
            super.toWriter(writer, outputProperties);
        } catch (TransformerException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

    /**
     * @throws XMLBuilderRuntimeException
     * to wrap {@link XPathExpressionException}
     *
     */
    @Override
    public Object xpathQuery(String xpath, QName type, NamespaceContext nsContext)
    {
        try {
            return super.xpathQuery(xpath, type, nsContext);
        } catch (XPathExpressionException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

    /**
     * @throws XMLBuilderRuntimeException
     * to wrap {@link XPathExpressionException}
     *
     */
    @Override
    public Object xpathQuery(String xpath, QName type)
    {
        try {
            return super.xpathQuery(xpath, type);
        } catch (XPathExpressionException e) {
            throw wrapExceptionAsRuntimeException(e);
        }
    }

}
