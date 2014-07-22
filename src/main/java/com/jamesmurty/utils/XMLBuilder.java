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
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
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
 * The XMLBuilder class serves as a wrapper of {@link org.w3c.dom.Element} nodes,
 * and provides a number of utility methods that make it simple to
 * manipulate the underlying element and the document to which it belongs.
 * In essence, this class performs dual roles: it represents a specific XML
 * node, and also allows manipulation of the entire underlying XML document.
 * The platform's default {@link DocumentBuilderFactory} and
 * {@link DocumentBuilder} classes are used to build the document.
 * </p>
 *
 * @author James Murty
 */
public final class XMLBuilder extends BaseXMLBuilder {

    /**
     * Construct a new builder object that wraps the given XML document.
     * This constructor is for internal use only.
     *
     * @param xmlDocument
     * an XML document that the builder will manage and manipulate.
     */
    protected XMLBuilder(Document xmlDocument) {
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
    protected XMLBuilder(Node myNode, Node parentNode) {
        super(myNode, parentNode);
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
     *
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     */
    public static XMLBuilder create(String name, String namespaceURI)
        throws ParserConfigurationException, FactoryConfigurationError
    {
        return new XMLBuilder(createDocumentImpl(name, namespaceURI));
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
     *
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     */
    public static XMLBuilder create(String name)
        throws ParserConfigurationException, FactoryConfigurationError
    {
        return create(name, null);
    }

    /**
     * Construct a builder from an existing XML document. The provided XML
     * document will be parsed and an XMLBuilder object referencing the
     * document's root element will be returned.
     *
     * @param inputSource
     * an XML document input source that will be parsed into a DOM.
     * @return
     * a builder node that can be used to add more nodes to the XML document.
     * @throws ParserConfigurationException
     *
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static XMLBuilder parse(InputSource inputSource)
        throws ParserConfigurationException, SAXException, IOException
    {
        return new XMLBuilder(parseDocumentImpl(inputSource));
    }

    /**
     * Construct a builder from an existing XML document string.
     * The provided XML document will be parsed and an XMLBuilder
     * object referencing the document's root element will be returned.
     *
     * @param xmlString
     * an XML document string that will be parsed into a DOM.
     * @return
     * a builder node that can be used to add more nodes to the XML document.
     *
     * @throws ParserConfigurationException
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static XMLBuilder parse(String xmlString)
        throws ParserConfigurationException, SAXException, IOException
    {
        return XMLBuilder.parse(new InputSource(new StringReader(xmlString)));
    }

    /**
     * Construct a builder from an existing XML document file.
     * The provided XML document will be parsed and an XMLBuilder
     * object referencing the document's root element will be returned.
     *
     * @param xmlFile
     * an XML document file that will be parsed into a DOM.
     * @return
     * a builder node that can be used to add more nodes to the XML document.
     *
     * @throws ParserConfigurationException
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static XMLBuilder parse(File xmlFile)
        throws ParserConfigurationException, SAXException, IOException
    {
        return XMLBuilder.parse(new InputSource(new FileReader(xmlFile)));
    }

    @Override
    public XMLBuilder stripWhitespaceOnlyTextNodes()
        throws XPathExpressionException
    {
        super.stripWhitespaceOnlyTextNodesImpl();
        return this;
    }

    @Override
    public XMLBuilder importXMLBuilder(BaseXMLBuilder builder) {
        super.importXMLBuilderImpl(builder);
        return this;
    }

    @Override
    public XMLBuilder root() {
        return new XMLBuilder(getDocument());
    }

    @Override
    public XMLBuilder xpathFind(String xpath, NamespaceContext nsContext)
        throws XPathExpressionException
    {
        Node foundNode = super.xpathFindImpl(xpath, nsContext);
        return new XMLBuilder(foundNode, null);
    }

    @Override
    public XMLBuilder xpathFind(String xpath) throws XPathExpressionException {
        return xpathFind(xpath, null);
    }

    @Override
    public XMLBuilder element(String name) {
        String namespaceURI = super.lookupNamespaceURIImpl(name);
        return element(name, namespaceURI);
    }

    @Override
    public XMLBuilder elem(String name) {
        return element(name);
    }

    @Override
    public XMLBuilder e(String name) {
        return element(name);
    }

    @Override
    public XMLBuilder element(String name, String namespaceURI) {
        Element elem = super.elementImpl(name, namespaceURI);
        return new XMLBuilder(elem, this.getElement());
    }

    @Override
    public XMLBuilder elementBefore(String name) {
        Element newElement = super.elementBeforeImpl(name);
        return new XMLBuilder(newElement, null);
    }

    @Override
    public XMLBuilder elementBefore(String name, String namespaceURI) {
        Element newElement = super.elementBeforeImpl(name, namespaceURI);
        return new XMLBuilder(newElement, null);
    }

    @Override
    public XMLBuilder attribute(String name, String value) {
        super.attributeImpl(name, value);
        return this;
    }

    @Override
    public XMLBuilder attr(String name, String value) {
        return attribute(name, value);
    }

    @Override
    public XMLBuilder a(String name, String value) {
        return attribute(name, value);
    }


    @Override
    public XMLBuilder text(String value, boolean replaceText) {
        super.textImpl(value, replaceText);
        return this;
    }

    @Override
    public XMLBuilder text(String value) {
        return this.text(value, false);
    }

    @Override
    public XMLBuilder t(String value) {
        return text(value);
    }

    @Override
    public XMLBuilder cdata(String data) {
        super.cdataImpl(data);
        return this;
    }

    @Override
    public XMLBuilder data(String data) {
        return cdata(data);
    }

    @Override
    public XMLBuilder d(String data) {
        return cdata(data);
    }

    @Override
    public XMLBuilder cdata(byte[] data) {
        super.cdataImpl(data);
        return this;
    }

    @Override
    public XMLBuilder data(byte[] data) {
        return cdata(data);
    }

    @Override
    public XMLBuilder d(byte[] data) {
        return cdata(data);
    }

    @Override
    public XMLBuilder comment(String comment) {
        super.commentImpl(comment);
        return this;
    }

    @Override
    public XMLBuilder cmnt(String comment) {
        return comment(comment);
    }

    @Override
    public XMLBuilder c(String comment) {
        return comment(comment);
    }

    @Override
    public XMLBuilder instruction(String target, String data) {
        super.instructionImpl(target, data);
        return this;
    }

    @Override
    public XMLBuilder inst(String target, String data) {
        return instruction(target, data);
    }

    @Override
    public XMLBuilder i(String target, String data) {
        return instruction(target, data);
    }

    @Override
    public XMLBuilder insertInstruction(String target, String data) {
        super.insertInstructionImpl(target, data);
        return this;
    }

    @Override
    public XMLBuilder reference(String name) {
        super.referenceImpl(name);
        return this;
    }

    @Override
    public XMLBuilder ref(String name) {
        return reference(name);
    }

    @Override
    public XMLBuilder r(String name) {
        return reference(name);
    }

    @Override
    public XMLBuilder namespace(String prefix, String namespaceURI) {
        super.namespaceImpl(prefix, namespaceURI);
        return this;
    }

    @Override
    public XMLBuilder ns(String prefix, String namespaceURI) {
        return attribute(prefix, namespaceURI);
    }

    @Override
    public XMLBuilder namespace(String namespaceURI) {
        this.namespace(null, namespaceURI);
        return this;
    }

    @Override
    public XMLBuilder ns(String namespaceURI) {
        return namespace(namespaceURI);
    }

    @Override
    public XMLBuilder up(int steps) {
        Node currNode = super.upImpl(steps);
        if (currNode instanceof Document) {
            return new XMLBuilder((Document) currNode);
        } else {
            return new XMLBuilder(currNode, null);
        }
    }

    @Override
    public XMLBuilder up() {
        return up(1);
    }

    @Override
    public XMLBuilder document() {
        return new XMLBuilder(getDocument(), null);
    }

}
