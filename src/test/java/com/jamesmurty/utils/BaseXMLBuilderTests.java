package com.jamesmurty.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;
import net.iharder.Base64;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public abstract class BaseXMLBuilderTests extends TestCase {

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

	protected abstract Class<? extends BaseXMLBuilder> XMLBuilderToTest() throws Exception;

	protected abstract boolean isRuntimeExceptionsOnly();

	protected BaseXMLBuilder XMLBuilder_create(String name) throws Exception {
        return (BaseXMLBuilder) XMLBuilderToTest().getMethod(
            "create", String.class).invoke(null, name);
	}

    protected BaseXMLBuilder XMLBuilder_create(String name, String nsURI) throws Exception {
        return (BaseXMLBuilder) XMLBuilderToTest().getMethod(
            "create", String.class, String.class).invoke(null, name, nsURI);
    }

    protected BaseXMLBuilder XMLBuilder_parse(InputSource source) throws Exception {
        return (BaseXMLBuilder) XMLBuilderToTest().getMethod(
            "parse", InputSource.class).invoke(null, source);
    }

    protected BaseXMLBuilder XMLBuilder_parse(
        String documentString, boolean enableExternalEntities,
        boolean isNamespaceAware) throws Exception
    {
        return (BaseXMLBuilder) XMLBuilderToTest().getMethod(
            "parse", String.class, boolean.class, boolean.class).invoke(
                null, documentString, enableExternalEntities, isNamespaceAware);
    }

    protected BaseXMLBuilder XMLBuilder_parse(String documentString) throws Exception {
        return (BaseXMLBuilder) XMLBuilderToTest().getMethod(
            "parse", String.class).invoke(null, documentString);
    }

    public void testXmlDocumentCreation() throws Exception {
		/* Build XML document in-place */
		BaseXMLBuilder builder = XMLBuilder_create("Projects")
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
		BaseXMLBuilder projectsB = XMLBuilder_create("Projects");
		projectsB.e("java-xmlbuilder")
		        .a("language", "Java")
		        .a("scm","SVN")
		        .e("Location")
		            .a("type", "URL")
		            .t("http://code.google.com/p/java-xmlbuilder/");
		BaseXMLBuilder jets3tB = projectsB.e("JetS3t")
		        .a("language", "Java")
		        .a("scm","CVS");
		jets3tB.e("Location")
		            .a("type", "URL")
		            .t("http://jets3t.s3.amazonaws.com/index.html");

		assertEquals(builder.asString(), projectsB.asString());
	}

	public void testParseAndXPath() throws Exception {
		// Parse an existing XML document
		BaseXMLBuilder builder = XMLBuilder_parse(
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
		} catch (Exception e) {
            if (isRuntimeExceptionsOnly()) {
                assertEquals(XMLBuilderRuntimeException.class, e.getClass());
                e = (Exception) e.getCause();
            }
            assertEquals(XPathExpressionException.class, e.getClass());
			assertTrue(e.getMessage().contains("does not resolve to an Element"));
		}

		/* Perform full-strength XPath queries that do not have to
		 * resolve to an Element, and do not return BaseXMLBuilder instances
		 */

	    // Find the Location value for the JetS3t project
        String location = (String) builder.xpathQuery(
            "//JetS3t/Location/.", XPathConstants.STRING);
        assertEquals("http://jets3t.s3.amazonaws.com/index.html", location);

        // Count the number of projects (count returned as String)
        String countAsString = (String) builder.xpathQuery(
            "count(/Projects/*)", XPathConstants.STRING);
        assertEquals("2", countAsString);

        // Count the number of projects (count returned as "Number" - actually Double)
        Number countAsNumber = (Number) builder.xpathQuery(
            "count(/Projects/*)", XPathConstants.NUMBER);
        assertEquals(2.0, countAsNumber);

        // Find all nodes under Projects
        NodeList nodes = (NodeList) builder.xpathQuery(
            "/Projects/*", XPathConstants.NODESET);
        assertEquals(2, nodes.getLength());
        assertEquals("JetS3t", nodes.item(1).getNodeName());

        // Returns null if nothing found when a NODE type is requested...
        assertNull(builder.xpathQuery("//WrongName", XPathConstants.NODE));
        // ... or an empty String if a STRING type is requested...
        assertEquals("", builder.xpathQuery("//WrongName", XPathConstants.STRING));
        // ... or NaN if a NUMBER type is requested...
        assertEquals(Double.NaN, builder.xpathQuery("//WrongName", XPathConstants.NUMBER));

		/* Add a new XML element at a specific XPath location in an existing document */

		// Use XPath to get a builder at the insert location
		BaseXMLBuilder xpathLocB = builder.xpathFind("//JetS3t");
		assertEquals("JetS3t", xpathLocB.getElement().getNodeName());

		// Append a new element with the location's builder
		BaseXMLBuilder location2B = xpathLocB.elem("Location2").attr("type", "Testing");
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

    public void testParseAndAmendDocWithWhitespaceNodes() throws Exception {
        // Parse example XML document and output with indenting, to add whitespace nodes
        Properties outputProperties = new Properties();
        outputProperties.put(OutputKeys.INDENT, "yes");
        outputProperties.put("{http://xml.apache.org/xslt}indent-amount", "2");
        String xmlWithWhitespaceNodes =
            XMLBuilder_parse(EXAMPLE_XML_DOC).asString(outputProperties);

        // Re-parse document that now has whitespace nodes
        BaseXMLBuilder builder = XMLBuilder_parse(xmlWithWhitespaceNodes);

        // Ensure we can add a node to the document (re issue #17)
        builder.xpathFind("//JetS3t")
            .elem("AnotherLocation").attr("type", "Testing");
        String xmlWithAmendments = builder.asString(outputProperties);
        assertTrue(xmlWithAmendments.contains("<AnotherLocation type=\"Testing\"/>"));
    }

    public void testStripWhitespaceNodesFromDocument() throws Exception {
        // Parse example XML document and output with indenting, to add whitespace nodes
        Properties outputProperties = new Properties();
        outputProperties.put(OutputKeys.INDENT, "yes");
        outputProperties.put("{http://xml.apache.org/xslt}indent-amount", "2");
        String xmlWithWhitespaceNodes =
            XMLBuilder_parse(EXAMPLE_XML_DOC).asString(outputProperties);

        // Re-parse document that now has whitespace text nodes
        BaseXMLBuilder builder = XMLBuilder_parse(xmlWithWhitespaceNodes);
        assertTrue(builder.asString().contains("\n"));
        assertTrue(builder.asString().contains("  "));

        // Strip whitespace nodes
        builder.stripWhitespaceOnlyTextNodes();
        assertFalse(builder.asString().contains("\n"));
        assertFalse(builder.asString().contains("  "));
    }

	public void testSimpleXpath() throws Exception {
	    String xmlDoc = "<template_objects><report_objects/></template_objects>";
	    BaseXMLBuilder builder = XMLBuilder_parse(xmlDoc);
	    BaseXMLBuilder builderNode = builder.xpathFind("report_objects");
	    assertTrue("report_objects".equals(builderNode.getElement().getNodeName()));
	    assertTrue("<report_objects/>".equals(builderNode.elementAsString()));
	}

	/**
	 * Test for issue #11: https://code.google.com/p/java-xmlbuilder/issues/detail?id=11
	 * @throws Exception
	 */
	public void testAddElementsInLoop() throws Exception {
        BaseXMLBuilder builder = XMLBuilder_create("DocRoot");
        BaseXMLBuilder parentBuilder = builder.element("Parent");

        // Add set of elements to Parent using a loop...
        for (int i = 1; i <= 10; i++) {
            parentBuilder.elem("IntegerValue" + i).text("" + i);
        }

        // ...and confirm element set is within parent after a call to up()
	    parentBuilder.up();

	    assertEquals("Parent", parentBuilder.getElement().getNodeName());
        assertEquals("DocRoot", builder.getElement().getNodeName());
        assertEquals(1, builder.getElement().getChildNodes().getLength());
        assertEquals("Parent", builder.getElement().getChildNodes().item(0).getNodeName());
        assertEquals(10, parentBuilder.getElement().getChildNodes().getLength());
        assertEquals("IntegerValue1", parentBuilder.getElement().getChildNodes().item(0).getNodeName());
        assertEquals("1", parentBuilder.getElement().getChildNodes().item(0).getTextContent());
	}

    public void testTraversalDuringBuild() throws Exception {
        BaseXMLBuilder builder = XMLBuilder_create("ElemDepth1")
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

    public void testImport() throws Exception {
        BaseXMLBuilder importer = XMLBuilder_create("Importer")
            .elem("Imported")
            .elem("Element")
            .elem("Goes").attr("are-we-there-yet", "almost")
            .elem("Here");
        BaseXMLBuilder importee = XMLBuilder_create("Importee")
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

        BaseXMLBuilder invalidImporter = XMLBuilder_create("InvalidImporter")
            .text("BadBadBad");
        try {
            invalidImporter.importXMLBuilder(importee);
            fail("Should not be able to import XMLBuilder into "
                + "an element containing text nodes");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    public void testCDataNodes() throws Exception {
        String text = "Text data -- left as it is";
        String textForBytes = "Byte data is automatically base64-encoded";
        String textEncoded = Base64.encodeBytes(textForBytes.getBytes("UTF-8"));

        BaseXMLBuilder builder = XMLBuilder_create("TestCDataNodes")
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

    public void testElementAsString() throws Exception {
        BaseXMLBuilder builder = XMLBuilder_create("This")
            .elem("Is").elem("My").text("Test");
        // By default, entire XML document is serialized regardless of starting-point
        assertEquals("<This><Is><My>Test</My></Is></This>", builder.asString());
        assertEquals("<This><Is><My>Test</My></Is></This>", builder.xpathFind("//My").asString());
        // Serialize a specific Element and its descendants with elementAsString
        assertEquals("<My>Test</My>", builder.xpathFind("//My").elementAsString());
    }

    public void testNamespaces() throws Exception {
        BaseXMLBuilder builder = XMLBuilder_create("NamespaceTest", "urn:default")
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
        } catch (Exception e) {
            if (isRuntimeExceptionsOnly()) {
                assertEquals(XMLBuilderRuntimeException.class, e.getClass());
                e = (Exception) e.getCause();
            }
            assertEquals(XPathExpressionException.class, e.getClass());
        }
        try {
            builder.xpathFind("//NSDefaultImplicit", context);
            fail("XPath query without prefixes on namespaced docs is invalid");
        } catch (Exception e) {
            if (isRuntimeExceptionsOnly()) {
                assertEquals(XMLBuilderRuntimeException.class, e.getClass());
                e = (Exception) e.getCause();
            }
            assertEquals(XPathExpressionException.class, e.getClass());
        }

        // Find nodes with default namespace
        builder.xpathFind("/:NamespaceTest", context);
        builder.xpathFind("//:NSDefaultImplicit", context);
        builder.xpathFind("//:NSDefaultExplicit", context);

        // Must use namespace-aware xpath to find namespaced nodes
        try {
            builder.xpathFind("//NSDefaultExplicit");
            fail();
        } catch (Exception e) {
            if (isRuntimeExceptionsOnly()) {
                assertEquals(XMLBuilderRuntimeException.class, e.getClass());
                e = (Exception) e.getCause();
            }
            assertEquals(XPathExpressionException.class, e.getClass());
        }
        try {
            builder.xpathFind("//:NSDefaultExplicit");
            fail();
        } catch (Exception e) {
            if (isRuntimeExceptionsOnly()) {
                assertEquals(XMLBuilderRuntimeException.class, e.getClass());
                e = (Exception) e.getCause();
            }
            assertEquals(XPathExpressionException.class, e.getClass());
        }
        try {
            builder.xpathFind("//NSDefaultExplicit", context);
            fail();
        } catch (Exception e) {
            if (isRuntimeExceptionsOnly()) {
                assertEquals(XMLBuilderRuntimeException.class, e.getClass());
                e = (Exception) e.getCause();
            }
            assertEquals(XPathExpressionException.class, e.getClass());
        }

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
        } catch (Exception e) {
            if (isRuntimeExceptionsOnly()) {
                assertEquals(XMLBuilderRuntimeException.class, e.getClass());
                e = (Exception) e.getCause();
            }
            assertEquals(XPathExpressionException.class, e.getClass());
        }

        // Users are not prevented from creating elements that reference
        // an undefined namespace prefix -- user beware
        builder.element("undefined-prefix:ElementName");
    }

    public void testNamespaceUnawareBuilder() throws Exception {
        String XML_WITH_NAMESPACES =
            "<ns1:NamespaceUnwareTest xmlns:ns1=\"urn:1\" xmlns:ns2=\"urn:2\">" +
                "<ns2:NestedElement>Found me</ns2:NestedElement>" +
            "</ns1:NamespaceUnwareTest>";

        // Builder set to be unaware of namespaces can traverse DOM with
        // namespaces without using namespace prefixes
        BaseXMLBuilder result = XMLBuilder_parse(
            XML_WITH_NAMESPACES,
            false,  // enableExternalEntities
            false  // isNamespaceAware
        ).xpathFind("/NamespaceUnwareTest/NestedElement");
        assertEquals("Found me", result.getElement().getTextContent());

        // Builder set to be aware of namespaces (per default) cannot traverse
        // DOM with namespaces without using namespace prefixes
        try {
            result = XMLBuilder_parse(XML_WITH_NAMESPACES)
                .xpathFind("/NamespaceUnwareTest/NestedElement");
        } catch (Exception ex) {
            Throwable cause = null;
            if (this instanceof TestXMLBuilder2) {
                cause = ex.getCause();  // Exception wrapped in runtime ex
            } else {
                cause = ex;
            }
            assertEquals(
                cause.getClass(), XPathExpressionException.class);
            assertTrue(
                cause.getMessage().contains(
                    "XPath expression \"/NamespaceUnwareTest/NestedElement\""
                    + " does not resolve to an Element in context"
                ));
        }
        assertEquals("Found me", result.getElement().getTextContent());
    }

    public void testElementBefore() throws Exception {
        BaseXMLBuilder builder = XMLBuilder_create("TestDocument", "urn:default")
                .namespace("custom", "urn:custom")
                .elem("Before").up()
                .elem("After");
        NamespaceContextImpl context = builder.buildDocumentNamespaceContext();

        // Ensure XML structure is correct before insert
        assertEquals("<TestDocument xmlns=\"urn:default\" xmlns:custom=\"urn:custom\">"
            + "<Before/><After/></TestDocument>", builder.asString());

        // Insert an element before the "After" element, no explicit namespace (will use default)
        BaseXMLBuilder testDoc = XMLBuilder_parse(builder.asString())
            .xpathFind("/:TestDocument/:After", context);
        BaseXMLBuilder insertedBuilder = testDoc.elementBefore("Inserted");
        assertEquals("Inserted", insertedBuilder.getElement().getNodeName());
        assertEquals("<TestDocument xmlns=\"urn:default\" xmlns:custom=\"urn:custom\">"
            + "<Before/><Inserted/><After/></TestDocument>", testDoc.asString());

        // Insert another element, this time with a custom namespace prefix
        insertedBuilder = insertedBuilder.elementBefore("custom:InsertedAgain");
        assertEquals("custom:InsertedAgain", insertedBuilder.getElement().getNodeName());
        assertEquals("<TestDocument xmlns=\"urn:default\" xmlns:custom=\"urn:custom\">"
            + "<Before/><custom:InsertedAgain/><Inserted/><After/></TestDocument>",
            testDoc.asString());

        // Insert another element, this time with a custom namespace ref
        insertedBuilder = insertedBuilder.elementBefore("InsertedYetAgain", "urn:custom2");
        assertEquals("InsertedYetAgain", insertedBuilder.getElement().getNodeName());
        assertEquals("<TestDocument xmlns=\"urn:default\" xmlns:custom=\"urn:custom\">"
            + "<Before/><InsertedYetAgain xmlns=\"urn:custom2\"/><custom:InsertedAgain/>"
            + "<Inserted/><After/></TestDocument>",
            testDoc.asString());
    }

    public void testTextNodes() throws Exception {
        BaseXMLBuilder builder = XMLBuilder_create("TestDocument")
                .elem("TextElement")
                    .text("Initial");

        BaseXMLBuilder textElementBuilder = builder.xpathFind("//TextElement");
        assertEquals("Initial", textElementBuilder.getElement().getTextContent());

        // By default, text methods append value to existing text
        textElementBuilder.text("Appended");
        assertEquals("InitialAppended", textElementBuilder.getElement().getTextContent());

        // Use boolean flag to replace text nodes with a new value
        textElementBuilder.text("Replacement", true);
        assertEquals("Replacement", textElementBuilder.getElement().getTextContent());

        // Fail-fast if a null text value is provided.
        try {
            textElementBuilder.text(null);
            fail("null text value should cause IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("Illegal null text value", ex.getMessage());
        }

        try {
            textElementBuilder.text(null, true);
            fail("null text value should cause IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertEquals("Illegal null text value", ex.getMessage());
        }

    }

    public void testProcessingInstructionNodes() throws Exception {
        // Add instruction to root document element node (usual append-in-node behaviour)
        BaseXMLBuilder builder = XMLBuilder_create("TestDocument").instruction("test", "data");
        assertEquals("<TestDocument><?test data?></TestDocument>", builder.asString());

        // Add instruction after the root document element (not within it)
        builder = XMLBuilder_create("TestDocument3").document().instruction("test", "data");
        assertEquals("<TestDocument3/><?test data?>", builder.asString().trim());

        // Insert instruction as first node of the root document
        builder = XMLBuilder_create("TestDocument3").insertInstruction("test", "data");
        assertEquals(
            "<?test data?><TestDocument3/>",
            // Remove newlines from XML as this differs across platforms
            builder.asString().replace("\n", ""));

        // Insert instruction as first node of the root document, second example
        builder = XMLBuilder_create("TestDocument4").elem("ChildElem")
            .root().insertInstruction("test", "data");
        assertEquals(
            "<?test data?><TestDocument4><ChildElem/></TestDocument4>",
            // Remove newlines from XML as this differs across platforms
            builder.asString().replace("\n", ""));
    }

    /**
     * Test for strange issue raised by user on comments form where OutputKeys.STANDALONE setting
     * in transformer is ignored.
     *
     * @throws Exception
     */
    public void testSetStandaloneToYes() throws Exception {
        String xmlDoc = "<RootNode><InnerNode/></RootNode>";
        BaseXMLBuilder builder = XMLBuilder_parse(
            new InputSource(new StringReader(xmlDoc)));

        // Basic output settings
        Properties outputProperties = new Properties();
        outputProperties.put(javax.xml.transform.OutputKeys.VERSION, "1.0");
        outputProperties.put(javax.xml.transform.OutputKeys.METHOD, "xml");
        outputProperties.put(javax.xml.transform.OutputKeys.ENCODING, "UTF-8");

        // Use Document@setXmlStandalone(true) to ensure OutputKeys.STANDALONE is respected.
        builder.getDocument().setXmlStandalone(true);
        outputProperties.put(javax.xml.transform.OutputKeys.STANDALONE, "yes");

        /* Serialize builder document */
        StringWriter writer = new StringWriter();
        builder.toWriter(writer, outputProperties);

        assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + xmlDoc,
            writer.toString());
    }

    /**
     * Test the {@link BaseXMLBuilder#asString(Properties)} method output a document
     * of the correct size when the document is moderately large, re issue
     * #1 (on GitHub).
     *
     * @throws Exception
     */
    public void testModerateDocumentSizeAsString() throws Exception {
        // Create a moderate document around 0.5 MB
        long expectedByteSize = 505021;
        BaseXMLBuilder builder = XMLBuilder_create("RootNode");
        for (int i = 0; i < 5000; i++) {
            builder
                .e("TreeRoot")
                    .e("TreeTrunk")
                        .e("TreeBranch")
                            .e("TreeLeaf")
                                .t("Some Aphids");

        }
        // Omit XML declaration, which will otherwise be included in file
        // via #toWriter but not in string via #asString
        Properties outputProperties = new Properties();
        outputProperties.put(
            javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
        // Ensure XML as string has expected length...
        String xmlString = builder.asString(outputProperties);
        assertEquals(expectedByteSize, xmlString.length());
        // ...and matches size of XML written to file
        File f = File.createTempFile(
            "java-xmlbuilder-testmoderatedocumentsizeasstring", ".xml");
        builder.toWriter(new FileWriter(f), outputProperties);
        assertEquals(expectedByteSize, f.length());
        f.delete();
    }

    /**
     * Ensure XML Builder parse methods use a default configuration that
     * prevents XML External Entity (XXE) injection attacks.
     *
     * @throws Exception
     */
    public void testXMLBuilderParserImmuneToXXEAttackByDefault() throws Exception {
        String externalFilePath = "src/test/java/com/jamesmurty/utils/external.txt";
        File externalFile = new File(externalFilePath);
        String XML_DOC_WITH_XXE =
            "<?xml version=\"1.0\"?>" +
            "<!DOCTYPE Projects [" +
                "<!ELEMENT JetS3t ANY>" +
                "<!ENTITY xx1 SYSTEM \"" + externalFile.toURI() + "\"> ]>" +
            EXAMPLE_XML_DOC_START + "&xx1;" + EXAMPLE_XML_DOC_END;
        // By default, builder is immune from XXE injection
        BaseXMLBuilder builder = XMLBuilder_parse(XML_DOC_WITH_XXE);
        String parsedXml = builder.asString();
        assertFalse(parsedXml.indexOf("Injected XXE Data") >= 0);
        // If you enable external entity processing, builder becomes subject to XXE injection
        builder = XMLBuilder_parse(XML_DOC_WITH_XXE, true, true);
        parsedXml = builder.asString();
        assertTrue(parsedXml.indexOf("Injected XXE Data") >= 0);
    }

}
