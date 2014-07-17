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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.iharder.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Base abstract class for all XML Builder implementations.
 * Most of the work is done here.
 *
 * @author jmurty
 */
public abstract class BaseXMLBuilder {

    /**
     * A DOM Document that stores the underlying XML document operated on by
     * BaseXMLBuilder instances. This document object belongs to the root node
     * of a document, and is shared by this node with all other BaseXMLBuilder
     * instances via the {@link #getDocument()} method.
     * This instance variable must only be created once, by the root node for
     * any given document.
     */
    private Document xmlDocument = null;

    /**
     * The underlying node represented by this builder node.
     */
    private Node xmlNode = null;

    private static boolean isNamespaceAware = true;

    /**
     * Construct a new builder object that wraps the given XML document.
     * This constructor is for internal use only.
     *
     * @param xmlDocument
     * an XML document that the builder will manage and manipulate.
     */
    protected BaseXMLBuilder(Document xmlDocument) {
        this.xmlDocument = xmlDocument;
        this.xmlNode = xmlDocument.getDocumentElement();
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
    protected BaseXMLBuilder(Node myNode, Node parentNode) {
        this.xmlNode = myNode;
        if (myNode instanceof Document) {
            this.xmlDocument = (Document) myNode;
        } else {
            this.xmlDocument = myNode.getOwnerDocument();
        }
        if (parentNode != null) {
            parentNode.appendChild(myNode);
        }
    }

    /**
     * Construct an XML Document with a default namespace with the given
     * root element.
     *
     * @param name
     * the name of the document's root element.
     * @param namespaceURI
     * default namespace URI for document, ignored if null or empty.
     * @return
     * an XML Document.
     *
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     */
    protected static Document createDocumentImpl(String name, String namespaceURI)
        throws ParserConfigurationException, FactoryConfigurationError
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(isNamespaceAware);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element rootElement = null;
        if (namespaceURI != null && namespaceURI.length() > 0) {
            rootElement = document.createElementNS(namespaceURI, name);
        } else {
            rootElement = document.createElement(name);
        }
        document.appendChild(rootElement);
        return document;
    }

    /**
     * Return an XML Document parsed from the given input source.
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
    protected static Document parseDocumentImpl(InputSource inputSource)
        throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(isNamespaceAware);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputSource);
        return document;
    }

    /**
     * Find and delete from the underlying Document any text nodes that
     * contain nothing but whitespace, such as newlines and tab or space
     * characters used to indent or pretty-print an XML document.
     *
     * Uses approach I documented on StackOverflow:
     * http://stackoverflow.com/a/979606/4970
     *
     * @throws XPathExpressionException
     */
    protected void stripWhitespaceOnlyTextNodesImpl()
        throws XPathExpressionException
    {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        // XPath to find empty text nodes.
        XPathExpression xpathExp = xpathFactory.newXPath().compile(
            "//text()[normalize-space(.) = '']");
        NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(
            this.getDocument(), XPathConstants.NODESET);

        // Remove each empty text node from document.
        for (int i = 0; i < emptyTextNodes.getLength(); i++) {
            Node emptyTextNode = emptyTextNodes.item(i);
            emptyTextNode.getParentNode().removeChild(emptyTextNode);
        }
    }

    /**
     * Imports another BaseXMLBuilder document into this document at the
     * current position. The entire document provided is imported.
     *
     * @param builder
     * the BaseXMLBuilder document to be imported.
     */
    protected void importXMLBuilderImpl(BaseXMLBuilder builder) {
        assertElementContainsNoOrWhitespaceOnlyTextNodes(this.xmlNode);
        Node importedNode = getDocument().importNode(
            builder.getDocument().getDocumentElement(), true);
        this.xmlNode.appendChild(importedNode);
    }

    /**
     * @return
     * true if the XML Document and Element objects wrapped by this
     * builder are equal to the other's wrapped objects.
     */
    @Override
    public boolean equals(Object obj) {
    	if (obj != null && obj instanceof BaseXMLBuilder) {
    	    BaseXMLBuilder other = (BaseXMLBuilder) obj;
    		return
    			this.xmlDocument.equals(other.getDocument())
    			&& this.xmlNode.equals(other.getElement());
    	}
    	return false;
    }

    /**
     * @return
     * the XML element wrapped by this builder node, or null if the builder node wraps the
     * root Document node.
     */
    public Element getElement() {
        if (this.xmlNode instanceof Element) {
            return (Element) this.xmlNode;
        } else {
            return null;
        }
    }

    /**
     * @return
     * the XML document constructed by all builder nodes.
     */
    public Document getDocument() {
    	return this.xmlDocument;
    }

    /**
     * Return the result of evaluating an XPath query on the builder's DOM
     * using the given namespace. Returns null if the query finds nothing,
     * or finds a node that does not match the type specified by returnType.
     *
     * @param xpath
     * an XPath expression
     * @param type
     * the type the XPath is expected to resolve to, e.g:
     * {@link XPathConstants#NODE}, {@link XPathConstants#NODESET},
     * {@link XPathConstants#STRING}.
     * @param nsContext
     * a mapping of prefixes to namespace URIs that allows the XPath expression
     * to use namespaces, or null for a non-namespaced document.
     *
     * @return
     * a builder node representing the first Element that matches the
     * XPath expression.
     *
     * @throws XPathExpressionException
     * If the XPath is invalid, or if does not resolve to at least one
     * {@link Node#ELEMENT_NODE}.
     */
    public Object xpathQuery(String xpath, QName type, NamespaceContext nsContext)
        throws XPathExpressionException {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xPath = xpathFactory.newXPath();
            if (nsContext != null) {
                xPath.setNamespaceContext(nsContext);
            }
            XPathExpression xpathExp = xPath.compile(xpath);
            try {
                return xpathExp.evaluate(this.xmlNode, type);
            } catch (IllegalArgumentException e) {
                // Thrown if item found does not match expected type
                return null;
            }
        }

    /**
     * Return the result of evaluating an XPath query on the builder's DOM.
     * Returns null if the query finds nothing,
     * or finds a node that does not match the type specified by returnType.
     *
     * @param xpath
     * an XPath expression
     * @param type
     * the type the XPath is expected to resolve to, e.g:
     * {@link XPathConstants#NODE}, {@link XPathConstants#NODESET},
     * {@link XPathConstants#STRING}
     *
     * @return
     * a builder node representing the first Element that matches the
     * XPath expression.
     *
     * @throws XPathExpressionException
     * If the XPath is invalid, or if does not resolve to at least one
     * {@link Node#ELEMENT_NODE}.
     */
    public Object xpathQuery(String xpath, QName type)
        throws XPathExpressionException {
            return xpathQuery(xpath, type, null);
        }

    /**
     * Find the first element in the builder's DOM matching the given
     * XPath expression, where the expression may include namespaces if
     * a {@link NamespaceContext} is provided.
     *
     * @param xpath
     * An XPath expression that *must* resolve to an existing Element within
     * the document object model.
     * @param nsContext
     * a mapping of prefixes to namespace URIs that allows the XPath expression
     * to use namespaces.
     *
     * @return
     * the first Element that matches the XPath expression.
     *
     * @throws XPathExpressionException
     * If the XPath is invalid, or if does not resolve to at least one
     * {@link Node#ELEMENT_NODE}.
     */
    protected Node xpathFindImpl(String xpath, NamespaceContext nsContext)
        throws XPathExpressionException
    {
    	Node foundNode = (Node) this.xpathQuery(xpath, XPathConstants.NODE, nsContext);
    	if (foundNode == null || foundNode.getNodeType() != Node.ELEMENT_NODE) {
    		throw new XPathExpressionException("XPath expression \""
    			+ xpath + "\" does not resolve to an Element in context "
    			+ this.xmlNode + ": " + foundNode);
    	}
    	return foundNode;
    }

    /**
     * Look up the namespace matching the current builder node's qualified
     * name prefix (if any) or the document's default namespace.
     *
     * @param name
     * the name of the XML element.
     *
     * @return
     * The namespace URI, or null if none applies.
     */
    protected String lookupNamespaceURIImpl(String name) {
        String prefix = getPrefixFromQualifiedName(name);
        String namespaceURI = this.xmlNode.lookupNamespaceURI(prefix);
        return namespaceURI;
    }

    /**
     * Add a named and namespaced XML element to the document as a child of
     * this builder's node.
     *
     * @param name
     * the name of the XML element.
     * @param namespaceURI
     * a namespace URI
     * @return
     *
     * @throws IllegalStateException
     * if you attempt to add a child element to an XML node that already
     * contains a text node value.
     */
    protected Element elementImpl(String name, String namespaceURI) {
        assertElementContainsNoOrWhitespaceOnlyTextNodes(this.xmlNode);
        if (namespaceURI == null) {
            return getDocument().createElement(name);
        } else {
            return getDocument().createElementNS(namespaceURI, name);
        }
    }

    /**
     * Add a named XML element to the document as a sibling element
     * that precedes the position of this builder node.
     *
     * When adding an element to a namespaced document, the new node will be
     * assigned a namespace matching it's qualified name prefix (if any) or
     * the document's default namespace. NOTE: If the element has a prefix that
     * does not match any known namespaces, the element will be created
     * without any namespace.
     *
     * @param name
     * the name of the XML element.
     *
     * @throws IllegalStateException
     * if you attempt to add a sibling element to a node where there are already
     * one or more siblings that are text nodes.
     */
    protected Element elementBeforeImpl(String name) {
        String prefix = getPrefixFromQualifiedName(name);
        String namespaceURI = this.xmlNode.lookupNamespaceURI(prefix);
        return elementBeforeImpl(name, namespaceURI);
    }

    /**
     * Add a named and namespaced XML element to the document as a sibling element
     * that precedes the position of this builder node.
     *
     * @param name
     * the name of the XML element.
     * @param namespaceURI
     * a namespace URI
     *
     * @throws IllegalStateException
     * if you attempt to add a sibling element to a node where there are already
     * one or more siblings that are text nodes.
     */
    protected Element elementBeforeImpl(String name, String namespaceURI) {
        Node parentNode = this.xmlNode.getParentNode();
        assertElementContainsNoOrWhitespaceOnlyTextNodes(parentNode);

        Element newElement = (namespaceURI == null
            ? getDocument().createElement(name)
            : getDocument().createElementNS(namespaceURI, name));

        // Insert new element before the current element
        parentNode.insertBefore(newElement, this.xmlNode);
        // Return a new builder node pointing at the new element
        return newElement;
    }

    /**
     * Add a named attribute value to the element for this builder node.
     *
     * @param name
     * the attribute's name.
     * @param value
     * the attribute's value.
     */
    protected void attributeImpl(String name, String value) {
        if (! (this.xmlNode instanceof Element)) {
            throw new RuntimeException(
                "Cannot add an attribute to non-Element underlying node: "
                + this.xmlNode);
        }
        ((Element) xmlNode).setAttribute(name, value);
    }

    /**
     * Add or replace the text value of an element for this builder node.
     *
     * @param value
     * the text value to set or add to the element.
     * @param replaceText
     * if True any existing text content of the node is replaced with the
     * given text value, if the given value is appended to any existing text.
     */
    protected void textImpl(String value, boolean replaceText) {
        // Issue 10: null text values cause exceptions on subsequent call to
        // Transformer to render document, so we fail-fast here on bad data.
        if (value == null) {
            throw new IllegalArgumentException("Illegal null text value");
        }

        if (replaceText) {
            xmlNode.setTextContent(value);
        } else {
            xmlNode.appendChild(getDocument().createTextNode(value));
        }
    }


    /**
     * Add a CDATA node with String content to the element for this builder node.
     *
     * @param data
     * the String value that will be added to a CDATA element.
     */
    protected void cdataImpl(String data) {
        xmlNode.appendChild(
            getDocument().createCDATASection(data));
    }

    /**
     * Add a CDATA node with Base64-encoded byte data content to the element
     * for this builder node.
     *
     * @param data
     * the data value that will be Base64-encoded and added to a CDATA element.
     */
    protected void cdataImpl(byte[] data) {
        xmlNode.appendChild(
            getDocument().createCDATASection(
                Base64.encodeBytes(data)));
    }

    /**
     * Add a comment to the element represented by this builder node.
     *
     * @param comment
     * the comment to add to the element.
     */
    protected void commentImpl(String comment) {
        xmlNode.appendChild(getDocument().createComment(comment));
    }

    /**
     * Add an instruction to the element represented by this builder node.
     *
     * @param target
     * the target value for the instruction.
     * @param data
     * the data value for the instruction
     */
    protected void instructionImpl(String target, String data) {
        xmlNode.appendChild(getDocument().createProcessingInstruction(target, data));
    }

    /**
     * Insert an instruction before the element represented by this builder node.
     *
     * @param target
     * the target value for the instruction.
     * @param data
     * the data value for the instruction
     */
    protected void insertInstructionImpl(String target, String data) {
        getDocument().insertBefore(
            getDocument().createProcessingInstruction(target, data),
            xmlNode);
    }

    /**
     * Add a reference to the element represented by this builder node.
     *
     * @param name
     * the name value for the reference.
     */
    protected void referenceImpl(String name) {
        xmlNode.appendChild(getDocument().createEntityReference(name));
    }

    /**
     * Add an XML namespace attribute to this builder's element node.
     *
     * @param prefix
     * a prefix for the namespace URI within the document, may be null
     * or empty in which case a default "xmlns" attribute is created.
     * @param namespaceURI
     * a namespace uri
     */
    protected void namespaceImpl(String prefix, String namespaceURI) {
        if (! (this.xmlNode instanceof Element)) {
            throw new RuntimeException(
                "Cannot add an attribute to non-Element underlying node: "
                + this.xmlNode);
        }
        if (prefix != null && prefix.length() > 0) {
            ((Element) xmlNode).setAttributeNS("http://www.w3.org/2000/xmlns/",
                "xmlns:" + prefix, namespaceURI);
        } else {
            ((Element) xmlNode).setAttributeNS("http://www.w3.org/2000/xmlns/",
                "xmlns", namespaceURI);
        }
    }

    /**
     * Add an XML namespace attribute to this builder's element node
     * without a prefix.
     *
     * @param namespaceURI
     * a namespace uri
     */
    protected void namespaceImpl(String namespaceURI) {
        namespaceImpl(null, namespaceURI);
    }

    /**
     * Return the Document node representing the n<em>th</em> ancestor element
     * of this node, or the root node if n exceeds the document's depth.
     *
     * @param steps
     * the number of parent elements to step over while navigating up the chain
     * of node ancestors. A steps value of 1 will find a node's parent, 2 will
     * find its grandparent etc.
     *
     * @return
     * the n<em>th</em> ancestor of this node, or the root node if this is
     * reached before the n<em>th</em> parent is found.
     */
    protected Node upImpl(int steps) {
    	Node currNode = this.xmlNode;
        int stepCount = 0;
        while (currNode.getParentNode() != null && stepCount < steps) {
        	currNode = currNode.getParentNode();
            stepCount++;
        }
        return currNode;
    }

    /**
     * @throws IllegalStateException
     * if the current element contains any child text nodes that aren't pure whitespace.
     * We allow whitespace so parsed XML documents containing indenting or pretty-printing
     * can still be amended, per issue #17.
     */
    protected void assertElementContainsNoOrWhitespaceOnlyTextNodes(
        Node anXmlElement) {
            Node textNodeWithNonWhitespace = null;
            NodeList childNodes = anXmlElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (Element.TEXT_NODE == childNodes.item(i).getNodeType()) {
                    Node textNode = childNodes.item(i);
                    String textWithoutWhitespace =
                        textNode.getTextContent().replaceAll("\\s", "");
                    if (textWithoutWhitespace.length() > 0) {
                        textNodeWithNonWhitespace = textNode;
                        break;
                    }
                }
            }
            if (textNodeWithNonWhitespace != null) {
                throw new IllegalStateException(
                    "Cannot add sub-element to element <" + anXmlElement.getNodeName()
                    + "> that contains a Text node that isn't purely whitespace: "
                    + textNodeWithNonWhitespace);
            }
        }

    /**
     * Serialize either the specific Element wrapped by this BaseXMLBuilder,
     * or its entire XML document, to the given writer using the default
     * {@link TransformerFactory} and {@link Transformer} classes.
     * If output options are provided, these options are provided to the
     * {@link Transformer} serializer.
     *
     * @param wholeDocument
     * if true the whole XML document (i.e. the document root) is serialized,
     * if false just the current Element and its descendants are serialized.
     * @param writer
     * a writer to which the serialized document is written.
     * @param outputProperties
     * settings for the {@link Transformer} serializer. This parameter may be
     * null or an empty Properties object, in which case the default output
     * properties will be applied.
     *
     * @throws TransformerException
     */
    public void toWriter(boolean wholeDocument, Writer writer, Properties outputProperties)
        throws TransformerException {
            StreamResult streamResult = new StreamResult(writer);

            DOMSource domSource = null;
            if (wholeDocument) {
                domSource = new DOMSource(getDocument());
            } else {
                domSource = new DOMSource(getElement());
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();

            if (outputProperties != null) {
                for (Entry<Object, Object> entry: outputProperties.entrySet()) {
                    serializer.setOutputProperty(
                        (String) entry.getKey(),
                        (String) entry.getValue());
                }
            }
            serializer.transform(domSource, streamResult);
        }

    /**
     * Serialize the XML document to the given writer using the default
     * {@link TransformerFactory} and {@link Transformer} classes. If output
     * options are provided, these options are provided to the
     * {@link Transformer} serializer.
     *
     * @param writer
     * a writer to which the serialized document is written.
     * @param outputProperties
     * settings for the {@link Transformer} serializer. This parameter may be
     * null or an empty Properties object, in which case the default output
     * properties will be applied.
     *
     * @throws TransformerException
     */
    public void toWriter(Writer writer, Properties outputProperties)
        throws TransformerException {
            this.toWriter(true, writer, outputProperties);
        }

    /**
     * Serialize the XML document to a string by delegating to the
     * {@link #toWriter(Writer, Properties)} method. If output options are
     * provided, these options are provided to the {@link Transformer}
     * serializer.
     *
     * @param outputProperties
     * settings for the {@link Transformer} serializer. This parameter may be
     * null or an empty Properties object, in which case the default output
     * properties will be applied.
     *
     * @return
     * the XML document as a string
     *
     * @throws TransformerException
     */
    public String asString(Properties outputProperties) throws TransformerException {
        StringWriter writer = new StringWriter();
        toWriter(writer, outputProperties);
        return writer.toString();
    }

    /**
     * Serialize the current XML Element and its descendants to a string by
     * delegating to the {@link #toWriter(Writer, Properties)} method.
     * If output options are provided, these options are provided to the
     * {@link Transformer} serializer.
     *
     * @param outputProperties
     * settings for the {@link Transformer} serializer. This parameter may be
     * null or an empty Properties object, in which case the default output
     * properties will be applied.
     *
     * @return
     * the XML document as a string
     *
     * @throws TransformerException
     */
    public String elementAsString(Properties outputProperties) throws TransformerException {
        StringWriter writer = new StringWriter();
        toWriter(false, writer, outputProperties);
        return writer.toString();
    }

    /**
     * Serialize the XML document to a string excluding the XML declaration.
     *
     * @return
     * the XML document as a string without the XML declaration at the
     * beginning of the output.
     *
     * @throws TransformerException
     */
    public String asString() throws TransformerException {
        Properties outputProperties = new Properties();
        outputProperties.put(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        return asString(outputProperties);
    }

    /**
     * Serialize the current XML Element and its descendants to a string
     * excluding the XML declaration.
     *
     * @return
     * the XML document as a string without the XML declaration at the
     * beginning of the output.
     *
     * @throws TransformerException
     */
    public String elementAsString() throws TransformerException {
        Properties outputProperties = new Properties();
        outputProperties.put(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        return elementAsString(outputProperties);
    }

    /**
     * @return
     * a namespace context containing the prefixes and namespace URI's used
     * within this builder's document, to assist in running namespace-aware
     * XPath queries against the document.
     */
    protected NamespaceContextImpl buildDocumentNamespaceContext() {
        return new NamespaceContextImpl(xmlDocument.getDocumentElement());
    }

    protected String getPrefixFromQualifiedName(String qualifiedName) {
        int colonPos = qualifiedName.indexOf(':');
        if (colonPos > 0) {
            return qualifiedName.substring(0, colonPos);
        } else {
            return null;
        }
    }

}