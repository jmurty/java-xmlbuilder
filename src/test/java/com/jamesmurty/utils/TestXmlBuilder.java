package com.jamesmurty.utils;

import java.io.StringWriter;
import java.util.Properties;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

public class TestXmlBuilder extends TestCase {
	
	public static final String EXAMPLE_XML_DOC = 
		"<Projects>" +
		  "<java-xmlbuilder language=\"Java\" scm=\"SVN\">" +
		    "<Location type=\"URL\">http://code.google.com/p/java-xmlbuilder/</Location>" +
		  "</java-xmlbuilder>" +
		  "<JetS3t language=\"Java\" scm=\"CVS\">" +
		    "<Location type=\"URL\">http://jets3t.s3.amazonaws.com/index.html</Location>" +
		  "</JetS3t>" +
		"</Projects>";

	
	public void testXmlDocumentCreation() throws ParserConfigurationException, 
		FactoryConfigurationError, TransformerException 
	{
		/* Build XML document */
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
		
		System.out.println(writer.toString());
		
		assertEquals(EXAMPLE_XML_DOC, writer.toString());
	}
	
}
