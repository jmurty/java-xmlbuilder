package com.jamesmurty.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import net.iharder.base64.Base64;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class TestXmlBuilder extends TestCase {

	public static final String EXAMPLE_XML_DOC_START =
		"<Projects>" +
		  "<java-xmlbuilder language=\"Java\" scm=\"SVN\">" +
		    "<Location type=\"URL\">http://code.google.com/p/java-xmlbuilder/</Location>" +
		  "</java-xmlbuilder>" +
		  "<JetS3t language=\"Java\" scm=\"CVS\">" +
		    "<Location type=\"URL\">http://jets3t.s3.amazonaws.com/index.html</Location>";

	public static final String EXAMPLE_XML_DOC_END =
		  "</JetS3t>" +
		"</Projects>";

	public static final String EXAMPLE_XML_DOC = EXAMPLE_XML_DOC_START + EXAMPLE_XML_DOC_END;

	public void testXmlDocumentCreation() throws ParserConfigurationException,
		FactoryConfigurationError, TransformerException
	{
		/* Build XML document in-place */
		XMLBuilder builder = XMLBuilder.create("Projects")
		    .e("java-xmlbuilder")
		        .a("language", "Java")
		        .a("scm","SVN")
		        .e("Location")
		            .a("type", "URL")
		            .t("http://code.google.com/p/java-xmlbuilder/")
		        .up()
		    .up()
		    .e("JetS3t")
		        .a("language", "Java")
		        .a("scm","CVS")
		        .e("Location")
		            .a("type", "URL")
		            .t("http://jets3t.s3.amazonaws.com/index.html");

		/* Set output properties */
		Properties outputProperties = new Properties();
		// Explicitly identify the output as an XML document
		outputProperties.put(javax.xml.transform.OutputKeys.METHOD, "xml");
		// Pretty-print the XML output (doesn't work in all cases)
		outputProperties.put(javax.xml.transform.OutputKeys.INDENT, "no");
		// Omit the XML declaration, which can differ depending on the test's run context.
		outputProperties.put(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");

		/* Serialize builder document */
		StringWriter writer = new StringWriter();
		builder.toWriter(writer, outputProperties);

		assertEquals(EXAMPLE_XML_DOC, writer.toString());

		/* Build XML document in segments*/
		XMLBuilder projectsB = XMLBuilder.create("Projects");
		projectsB.e("java-xmlbuilder")
		        .a("language", "Java")
		        .a("scm","SVN")
		        .e("Location")
		            .a("type", "URL")
		            .t("http://code.google.com/p/java-xmlbuilder/");
		XMLBuilder jets3tB = projectsB.e("JetS3t")
		        .a("language", "Java")
		        .a("scm","CVS");
		jets3tB.e("Location")
		            .a("type", "URL")
		            .t("http://jets3t.s3.amazonaws.com/index.html");

		assertEquals(builder.asString(), projectsB.asString());
	}

	public void testParseAndXPath() throws ParserConfigurationException, SAXException,
		IOException, XPathExpressionException, TransformerException
	{
		// Parse an existing XML document
		XMLBuilder builder = XMLBuilder.parse(
				new InputSource(new StringReader(EXAMPLE_XML_DOC)));
		assertEquals("Projects", builder.root().getElement().getNodeName());
		assertEquals("Invalid current element", "Projects", builder.getElement().getNodeName());

		// Find the first Location element
		builder = builder.xpathFind("//Location");
		assertEquals("Location", builder.getElement().getNodeName());
		assertEquals("http://code.google.com/p/java-xmlbuilder/",
				builder.getElement().getTextContent());

		// Find JetS3t's Location element
		builder = builder.xpathFind("//JetS3t/Location");
		assertEquals("Location", builder.getElement().getNodeName());
		assertEquals("http://jets3t.s3.amazonaws.com/index.html",
				builder.getElement().getTextContent());

		// Find the project with the scm attribute 'CVS' (should be JetS3t)
		builder = builder.xpathFind("//*[@scm = 'CVS']");
		assertEquals("JetS3t", builder.getElement().getNodeName());

		// Try an invalid XPath that does not resolve to an element
		try {
			builder.xpathFind("//@language");
			fail("Non-Element XPath expression should have failed");
		} catch (XPathExpressionException e) {
			assertTrue(e.getMessage().contains("does not resolve to an Element"));
		}

		/* Add a new XML element at a specific XPath location in an existing document */
		// Use XPath to get a builder at the insert location
		XMLBuilder xpathLocB = builder.xpathFind("//JetS3t");
		assertEquals("JetS3t", xpathLocB.getElement().getNodeName());

		// Append a new element with the location's builder
		XMLBuilder location2B = xpathLocB.elem("Location2").attr("type", "Testing");
		assertEquals("Location2", location2B.getElement().getNodeName());
		assertEquals("JetS3t", location2B.up().getElement().getNodeName());
		assertEquals(xpathLocB.getElement(), location2B.up().getElement());
		assertEquals(builder.root(), location2B.root());

		// Sanity-check the entire resultant XML document
		Properties outputProperties = new Properties();
		outputProperties.put(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
		String xmlAsString = location2B.asString(outputProperties);

		assertFalse(EXAMPLE_XML_DOC.equals(xmlAsString));
		assertTrue(xmlAsString.contains("<Location2 type=\"Testing\"/>"));
		assertEquals(
			EXAMPLE_XML_DOC_START + "<Location2 type=\"Testing\"/>" + EXAMPLE_XML_DOC_END,
			xmlAsString);
	}

    public void testTraversalDuringBuild() throws ParserConfigurationException, SAXException,
        IOException, XPathExpressionException, TransformerException
    {
        XMLBuilder builder = XMLBuilder.create("ElemDepth1")
            .e("ElemDepth2")
            .e("ElemDepth3")
            .e("ElemDepth4");
        assertEquals("ElemDepth3", builder.up().getElement().getNodeName());
        assertEquals("ElemDepth1", builder.up(3).getElement().getNodeName());
        // Traverse too far up the node tree...
        assertEquals("ElemDepth1", builder.up(4).getElement().getNodeName());
        // Traverse way too far up the node tree...
        assertEquals("ElemDepth1", builder.up(100).getElement().getNodeName());
    }

    public void testImport() throws ParserConfigurationException,
        FactoryConfigurationError
    {
        XMLBuilder importer = XMLBuilder.create("Importer")
            .elem("Imported")
            .elem("Element")
            .elem("Goes").attr("are-we-there-yet", "almost")
            .elem("Here");
        XMLBuilder importee = XMLBuilder.create("Importee")
            .elem("Importee").attr("awating-my", "new-home")
            .elem("IsEntireSubtree")
            .elem("Included");
        importer.importXMLBuilder(importee);

        // Ensure we're at the same point in the XML doc
        assertEquals("Here", importer.getElement().getNodeName());

        try {
            importer.xpathFind("//Importee");
            importer.xpathFind("//IsEntireSubtree");
            importer.xpathFind("//IsEntireSubtree");
            importer.xpathFind("//Included");
        } catch (XPathExpressionException e) {
            fail("XMLBuilder import failed: " + e.getMessage());
        }

        XMLBuilder invalidImporter = XMLBuilder.create("InvalidImporter")
            .text("BadBadBad");
        try {
            invalidImporter.importXMLBuilder(importee);
            fail("Should not be able to import XMLBuilder into "
                + "an element containing text nodes");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCDataNodes() throws ParserConfigurationException,
        FactoryConfigurationError, UnsupportedEncodingException, XPathExpressionException
    {
        String text = "Text data -- left as it is";
        String textForBytes = "Byte data is automatically base64-encoded";
        String textEncoded = Base64.encodeBytes(textForBytes.getBytes("UTF-8"));

        XMLBuilder builder = XMLBuilder.create("TestCDataNodes")
            .elem("CDataTextElem")
                .cdata(text)
                .up()
            .elem("CDataBytesElem")
                .cdata(textForBytes.getBytes("UTF-8"));

        Node cdataTextNode = builder.xpathFind("//CDataTextElem")
            .getElement().getChildNodes().item(0);
        assertEquals(Node.CDATA_SECTION_NODE, cdataTextNode.getNodeType());
        assertEquals(text, cdataTextNode.getNodeValue());

        Node cdataBytesNode = builder.xpathFind("//CDataBytesElem")
            .getElement().getChildNodes().item(0);
        assertEquals(Node.CDATA_SECTION_NODE, cdataBytesNode.getNodeType());
        assertEquals(textEncoded, cdataBytesNode.getNodeValue());
        String base64Decoded = new String(Base64.decode(cdataBytesNode.getNodeValue()));
        assertEquals(textForBytes, base64Decoded);
    }

    public void testElementAsString() throws ParserConfigurationException,
        FactoryConfigurationError, TransformerException, XPathExpressionException
    {
        XMLBuilder builder = XMLBuilder.create("This")
            .elem("Is").elem("My").text("Test");
        // By default, entire XML document is serialized regardless of starting-point
        assertEquals("<This><Is><My>Test</My></Is></This>", builder.asString());
        assertEquals("<This><Is><My>Test</My></Is></This>", builder.xpathFind("//My").asString());
        // Serialize a specific Element and its descendants with elementAsString
        assertEquals("<My>Test</My>", builder.xpathFind("//My").elementAsString());
    }

    public void testNamespaces() throws ParserConfigurationException,
        FactoryConfigurationError, TransformerException, XPathExpressionException
    {
        XMLBuilder builder = XMLBuilder
            .create("NamespaceTest", "urn:default")
                .namespace("prefix1", "urn:ns1")

                .element("NSDefaultImplicit").up()
                .element("NSDefaultExplicit", "urn:default").up()

                .element("NS1Explicit", "urn:ns1").up()
                .element("prefix1:NS1WithPrefixExplicit", "urn:ns1").up()
                .element("prefix1:NS1WithPrefixImplicit").up();

        // Build a namespace context from the builder's document
        NamespaceContextImpl context = builder.buildDocumentNamespaceContext();

        // All elements in a namespaced document inherit a namespace URI,
        // for namespaced document any non-namespaced XPath query will fail.
        try {
            builder.xpathFind("//:NSDefaultImplicit");
            fail("Namespaced xpath query without context is invalid");
        } catch (XPathExpressionException e) {}
        try {
            builder.xpathFind("//NSDefaultImplicit", context);
            fail("XPath query without prefixes on namespaced docs is invalid");
        } catch (XPathExpressionException e) {}

        // Find nodes with default namespace
        builder.xpathFind("/:NamespaceTest", context);
        builder.xpathFind("//:NSDefaultExplicit", context);
        builder.xpathFind("//:NSDefaultImplicit", context);

        // Must use namespace-aware xpath to find namespaced nodes
        try {
            builder.xpathFind("//NSDefaultExplicit");
            fail();
        } catch (XPathExpressionException e) {}
        try {
            builder.xpathFind("//:NSDefaultExplicit");
            fail();
        } catch (XPathExpressionException e) {}
        try {
            builder.xpathFind("//NSDefaultExplicit", context);
            fail();
        } catch (XPathExpressionException e) {}

        // Find node with namespace prefix
        builder.xpathFind("//prefix1:NS1Explicit", context);
        builder.xpathFind("//prefix1:NS1WithPrefixExplicit", context);
        builder.xpathFind("//prefix1:NS1WithPrefixImplicit", context);

        // Find nodes with user-defined prefix "aliases"
        context.addNamespace("default-alias", "urn:default");
        context.addNamespace("prefix1-alias", "urn:ns1");
        builder.xpathFind("//default-alias:NSDefaultExplicit", context);
        builder.xpathFind("//prefix1-alias:NS1Explicit", context);

        // User can override context mappings, for better or worse
        context.addNamespace("", "urn:default");
        builder.xpathFind("//:NSDefaultExplicit", context);

        context.addNamespace("", "urn:wrong");
        try {
            builder.xpathFind("//:NSDefaultExplicit", context);
            fail();
        } catch (XPathExpressionException e) {}

    }

}
