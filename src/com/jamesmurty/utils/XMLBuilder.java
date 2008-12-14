/*
 * Copyright 2008 James Murty (www.jamesmurty.com)
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
 * This code is available from the Google Code repository at:
 * http://code.google.com/p/java-xmlbuilder
 */
package com.jamesmurty.utils;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
public class XMLBuilder {
    /**
     * A DOM Document that stores the underlying XML document operated on by 
     * XMLBuilder instances. This document object belongs to the root node
     * of a document, and is shared by this node with all other XMLBuilder 
     * instances via the {@link #getDocument()} method. 
     * This instance variable must only be created once, by the root node for 
     * any given document. 
     */
    private Document xmlDocument = null;

    /**
     * This node's parent XML builder node, if any. All element nodes except 
     * the docuemnt's root will have a parent.
     */
    private XMLBuilder myParent = null;
    
    /**
     * The underlying element represented by this builder node. 
     */
    private Element xmlElement = null;

    /**
     * Construct a new builder object that wraps the given XML element, which
     * in turn will belong to an underlying XML document.  
     * This constructor is for internal use only.
     * 
     * @param xmlDocument
     * a new and empty XML document which the builder will manage and manipulate.
     * @param myElement
     * the XML element that this builder node will wrap. This element will be
     * added as the root of the underlying XML document. 
     */
    protected XMLBuilder(Document xmlDocument, Element myElement) {
        this.myParent = null;
        this.xmlElement = myElement;
        this.xmlDocument = xmlDocument;
        this.xmlDocument.appendChild(myElement);
    }

    /**
     * Construct a new builder object that wraps the given XML element, and
     * is the child of an existing XML builder node.
     * This constructor is for internal use only.
     * 
     * @param parent
     * the builder node that will contain (be the parent of) the new 
     * XML element.
     * @param myElement
     * the XML element that this builder node will wrap. This element will be
     * added as child node of the parent's XML element.
     */
    protected XMLBuilder(XMLBuilder parent, Element myElement) {
        this.myParent = parent;
        this.xmlElement = myElement;
        parent.xmlElement.appendChild(myElement);
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
        // Init DOM builder and Document.
        DocumentBuilder builder = 
            DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.newDocument();
        return new XMLBuilder(document, document.createElement(name));
    }
    
    /**
     * @return
     * the XML element that this builder node will manipulate.
     */
    public Element getElement() {
        return xmlElement;
    }

    /**
     * @return
     * the builder node representing the root element of the XML document.
     * In other words, the same builder node returned by the 
     * {@link #create(String)} method.
     */
    public XMLBuilder root() {
        // Navigate back through all parents to find the document's root node.
        XMLBuilder curr = this;
        while (curr.myParent != null) {
            curr = curr.myParent;
        }
        return curr;
    }
    
    /**
     * @return
     * the XML document constructed by all builder nodes.
     */
    public Document getDocument() {
        return root().xmlDocument;
    }
    
    /**
     * Add a named XML element to the document as a child of this builder node,
     * and return the builder node representing the new child. 
     * 
     * @param name
     * the name of the XML element.
     * 
     * @return
     * a builder node representing the new child.
     * 
     * @throws IllegalStateException
     * if you attempt to add a child element to an XML node that already 
     * contains a text node value.
     */
    public XMLBuilder element(String name) {
        // Ensure we don't create sub-elements in Elements that already have text node values.
        Node textNode = null;
        NodeList childNodes = xmlElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (Element.TEXT_NODE == childNodes.item(i).getNodeType()) {
                textNode = childNodes.item(i);
                break;
            }
        }
        if (textNode != null) {
            throw new IllegalStateException("Cannot add sub-element <" +
                name + "> to element <" + xmlElement.getNodeName() 
                + "> that already contains the Text node: " + textNode);
        }
        
        XMLBuilder child = new XMLBuilder(this, getDocument().createElement(name));        
        return child;
    }
    
    /**
     * Synonym for {@link #element(String)}. 
     * 
     * @param name
     * the name of the XML element.
     * 
     * @return
     * a builder node representing the new child.
     * 
     * @throws IllegalStateException
     * if you attempt to add a child element to an XML node that already 
     * contains a text node value.
     */
    public XMLBuilder elem(String name) {
        return element(name);
    }

    /**
     * Synonym for {@link #element(String)}. 
     * 
     * @param name
     * the name of the XML element.
     * 
     * @return
     * a builder node representing the new child.
     * 
     * @throws IllegalStateException
     * if you attempt to add a child element to an XML node that already 
     * contains a text node value.
     */
    public XMLBuilder e(String name) {
        return element(name);
    }
    
    /**
     * Add a named attribute value to the element represented by this builder
     * node, and return the node representing the element to which the 
     * attribute was added (<strong>not</strong> the new attribute node).
     * 
     * @param name
     * the attribute's name.
     * @param value
     * the attribute's value.
     * 
     * @return
     * the builder node representing the element to which the attribute was
     * added.
     */
    public XMLBuilder attribute(String name, String value) {
        xmlElement.setAttribute(name, value);
        return this;
    }

    /**
     * Synonym for {@link #attribute(String, String)}.
     * 
     * @param name
     * the attribute's name.
     * @param value
     * the attribute's value.
     * 
     * @return
     * the builder node representing the element to which the attribute was
     * added.
     */
    public XMLBuilder attr(String name, String value) {
        return attribute(name, value);
    }

    /**
     * Synonym for {@link #attribute(String, String)}.
     * 
     * @param name
     * the attribute's name.
     * @param value
     * the attribute's value.
     * 
     * @return
     * the builder node representing the element to which the attribute was
     * added.
     */
    public XMLBuilder a(String name, String value) {
        return attribute(name, value);
    }

    /**
     * Add a text value to the element represented by this builder node, and 
     * return the node representing the element to which the text 
     * was added (<strong>not</strong> the new text node).
     * 
     * @param value
     * the text value to add to the element.
     * 
     * @return
     * the builder node representing the element to which the text was added.
     */
    public XMLBuilder text(String value) {
        xmlElement.appendChild(getDocument().createTextNode(value));
        return this;
    }

    /**
     * Synonmy for {@link #text(String)}.
     * 
     * @param value
     * the text value to add to the element.
     * 
     * @return
     * the builder node representing the element to which the text was added.
     */
    public XMLBuilder t(String value) {
        return text(value);
    }
    
    /**
     * Add a CDATA value to the element represented by this builder node, and 
     * return the node representing the element to which the data 
     * was added (<strong>not</strong> the new CDATA node).
     * 
     * @param data
     * the data value to add to the element.
     * 
     * @return
     * the builder node representing the element to which the data was added.
     */
    public XMLBuilder cdata(String data) {
        xmlElement.appendChild(getDocument().createCDATASection(data));
        return this;
    }

    /**
     * Synonym for {@link #cdata(String)}.
     * 
     * @param data
     * the data value to add to the element.
     * 
     * @return
     * the builder node representing the element to which the data was added.
     */
    public XMLBuilder data(String data) {
        return cdata(data);
    }

    /**
     * Synonym for {@link #cdata(String)}.
     * 
     * @param data
     * the data value to add to the element.
     * 
     * @return
     * the builder node representing the element to which the data was added.
     */
    public XMLBuilder d(String data) {
        return cdata(data);
    }

    /**
     * Add a comment to the element represented by this builder node, and 
     * return the node representing the element to which the comment 
     * was added (<strong>not</strong> the new comment node).
     * 
     * @param comment
     * the comment to add to the element.
     * 
     * @return
     * the builder node representing the element to which the comment was added.
     */
    public XMLBuilder comment(String comment) {
        xmlElement.appendChild(getDocument().createComment(comment));
        return this;
    }

    /**
     * Synonym for {@link #comment(String)}.
     * 
     * @param comment
     * the comment to add to the element.
     * 
     * @return
     * the builder node representing the element to which the comment was added.
     */
    public XMLBuilder cmnt(String comment) {
        return comment(comment);
    }

    /**
     * Synonym for {@link #comment(String)}.
     * 
     * @param comment
     * the comment to add to the element.
     * 
     * @return
     * the builder node representing the element to which the comment was added.
     */
    public XMLBuilder c(String comment) {
        return comment(comment);
    }

    /**
     * Add an instruction to the element represented by this builder node, and 
     * return the node representing the element to which the instruction 
     * was added (<strong>not</strong> the new instruction node).
     * 
     * @param target
     * the target value for the instruction.
     * @param data
     * the data value for the instruction
     * 
     * @return
     * the builder node representing the element to which the instruction was 
     * added.
     */
    public XMLBuilder instruction(String target, String data) {
        xmlElement.appendChild(getDocument().createProcessingInstruction(target, data));
        return this;
    }

    /**
     * Synonym for {@link #instruction(String, String)}.
     * 
     * @param target
     * the target value for the instruction.
     * @param data
     * the data value for the instruction
     * 
     * @return
     * the builder node representing the element to which the instruction was 
     * added.
     */
    public XMLBuilder inst(String target, String data) {
        return instruction(target, data);
    }

    /**
     * Synonym for {@link #instruction(String, String)}.
     * 
     * @param target
     * the target value for the instruction.
     * @param data
     * the data value for the instruction
     * 
     * @return
     * the builder node representing the element to which the instruction was 
     * added.
     */
    public XMLBuilder i(String target, String data) {
        return instruction(target, data);
    }

    /**
     * Add a reference to the element represented by this builder node, and 
     * return the node representing the element to which the reference 
     * was added (<strong>not</strong> the new reference node).
     * 
     * @param name
     * the name value for the reference.
     * 
     * @return
     * the builder node representing the element to which the reference was 
     * added.
     */
    public XMLBuilder reference(String name) {
        xmlElement.appendChild(getDocument().createEntityReference(name));
        return this;
    }

    /**
     * Synonym for {@link #reference(String)}.
     * 
     * @param name
     * the name value for the reference.
     * 
     * @return
     * the builder node representing the element to which the reference was 
     * added.
     */
    public XMLBuilder ref(String name) {
        return reference(name);
    }

    /**
     * Synonym for {@link #reference(String)}.
     * 
     * @param name
     * the name value for the reference.
     * 
     * @return
     * the builder node representing the element to which the reference was 
     * added.
     */
    public XMLBuilder r(String name) {
        return reference(name);
    }

    /**
     * Return the builder node representing the n<em>th</em> ancestor element 
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
    public XMLBuilder up(int steps) {
        XMLBuilder curr = this;
        int stepCount = 0;
        while (curr.myParent != null && stepCount < steps) {
            curr = curr.myParent;            
            stepCount++;
        }        
        return curr;
    }
    
    /**
     * Return the builder node representing the parent of the current node.
     * 
     * @return
     * the parent of this node, or the root node if this method is called on the
     * root node. 
     */
    public XMLBuilder up() {
        return up(1);
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
        throws TransformerException 
    {
        StreamResult streamResult = new StreamResult(writer);
        
        DOMSource domSource = new DOMSource(getDocument());
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        
        if (outputProperties != null) {
            Iterator iter = outputProperties.entrySet().iterator();
            while (iter.hasNext()) {
                Entry entry = (Entry) iter.next();
                serializer.setOutputProperty((String) entry.getKey(), (String) entry.getValue());
            }
        }
        serializer.transform(domSource, streamResult);
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
    public String asString(Properties outputProperties) 
        throws TransformerException 
    {
        StringWriter writer = new StringWriter();
        toWriter(writer, outputProperties);
        return writer.toString();        
    }
    
}
