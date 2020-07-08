java-xmlbuilder
===============

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jamesmurty.utils/java-xmlbuilder/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jamesmurty.utils/java-xmlbuilder)

XML Builder is a utility that allows simple XML documents to be constructed
using relatively sparse Java code.

It allows for quick and painless creation of XML documents where you might
otherwise be tempted to use concatenated strings, and where you would rather
not face the tedium and verbosity of coding with
[JAXP](http://jaxp.dev.java.net/).

Internally, XML Builder uses JAXP to build a standard W3C Document model (DOM)
that you can easily export as a string, or access directly to manipulate
further if you have special requirements.

### License

[Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

XMLBuilder versus XMLBuilder2
-----------------------------

Since version 1.1 this library provides two builder implementations and APIs:

 * `XMLBuilder` – the original API – follows standard Java practice of
   re-throwing lower level checked exceptions when you do things like create a
   new document.  
   You must explicitly `catch` these checked exceptions in your codebase, even
   though they are unlikely to occur in tested code. 
 * `XMLBuilder2` is a newer API that removes checked exceptions altogether, and
   will instead wrap and propagate lower level exceptions in an unchecked
   `XMLBuilderRuntimeException`.  
   Use this class if you don't like the code mess or overhead of try/catching
   many low-level exceptions that are unlikely to occur in practice. 

Both these versions work identically apart from the handling of errors, so you
can use whichever version you prefer or "upgrade" from one to the other in
existing code.

Quick Example
-------------

Easily build XML documents using code structured like the final document.

This code:

```java
XMLBuilder2 builder = XMLBuilder2.create("Projects")
    .e("java-xmlbuilder").a("language", "Java").a("scm","SVN")
        .e("Location").a("type", "URL")
            .t("http://code.google.com/p/java-xmlbuilder/")
        .up()
    .up()
    .e("JetS3t").a("language", "Java").a("scm","CVS")
        .e("Location").a("type", "URL")
            .t("http://jets3t.s3.amazonaws.com/index.html");
```

Produces this XML document:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Projects>
    <java-xmlbuilder language="Java" scm="SVN">
        <Location type="URL">http://code.google.com/p/java-xmlbuilder/</Location>
    </java-xmlbuilder>
    <JetS3t language="Java" scm="CVS">
        <Location type="URL">http://jets3t.s3.amazonaws.com/index.html</Location>
    </JetS3t>
</Projects>
```

Getting Started
---------------

See further example usage below and in the 
[JavaDoc documentation](http://s3.jamesmurty.com/java-xmlbuilder/index.html).

Download a Jar file containing the latest version
[java-xmlbuilder-1.2.jar](http://s3.jamesmurty.com/java-xmlbuilder/java-xmlbuilder-1.2.jar).

Maven users can add this project as a dependency with the following additions
to a POM.xml file:

```maven
<dependencies>
  . . .
  <dependency>
    <groupId>com.jamesmurty.utils</groupId>
    <artifactId>java-xmlbuilder</artifactId>
    <version>1.2</version>
  </dependency>
  . . .
</dependencies>
```

How to use the XMLBuilder
-------------------------

Read below for examples that show how you would use the XMLBuilder utility to
create and manipulate XML documents like the following:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Projects>
    <java-xmlbuilder language="Java" scm="SVN">
        <Location type="URL">http://code.google.com/p/java-xmlbuilder/</Location>
    </java-xmlbuilder>
    <JetS3t language="Java" scm="CVS">
        <Location type="URL">http://jets3t.s3.amazonaws.com/index.html</Location>
    </JetS3t>
</Projects>
```

### Create a New XML Document

To begin, you create a new builder and XML document by specifying the name of
the document's root element. See the parsing methods below if you want to start
with an existing XML document.

```java
XMLBuilder builder = XMLBuilder.create("Projects");
```

The XMLBuilder object returned by the `create` method, and by all other XML
manipulation methods, provides methods that you can use to add more nodes to
the document. For example, to add the `java-xmlbuilder` and `JetS3t` nodes to
the root element you could do the following.

```java
XMLBuilder e1 = builder.element("java-xmlbuilder");
XMLBuilder e2 = builder.element("JetS3t");
```

And to add attributes or further sub-elements to the two new elements, you
could call the appropriate methods on the variables assigned to each new node
like so:

```java
e1.attribute("language", "Java");
e1.attribute("scm", "SVN");
```

This is straight-forward enough, but it is far more verbose than necessary
because the code does not take advantage of XMLBuilder's method-chaining
feature.

### Method Chaining

Every XMLBuilder method that adds something to the XML document will return an
XMLBuilder object that represents either a newly-added element, or the element
to which something has been added.  This feature means that you can chain
together many method calls without the need to assign intermediate objects to
variables.

With this in mind, here is code that performs the same job as the code above
without any unnecessary variables.

```java
XMLBuilder builder = XMLBuilder.create("Projects")
    .element("java-xmlbuilder")
        .attribute("language", "Java")
        .attribute("scm", "SVN")
        .element("Location")
        .up()
    .up()
    .element("JetS3t");
```

There are two important things to notice in the code above:

  * When you add a new element to the document with the `element` method, the
    XMLBuilder node returned will represent that new element. If you invoke
    methods on this node, attributes and elements will be added to the new node
    rather than to the document's root.
  * Once you have finished adding items to a new element, you can call the
    `up()` method to retrieve the XMLBuilder node that represents the parent of
    the current node. If you balance every call to `element()` with a call to
    `up()`, you can write code that closely resembles the structure of the XML
    document you are creating.

### Shorthand Methods

To make your XML building code even shorter and easier to type, there are
shorthand synonyms for every XML manipulation method. Instead of calling
`element()` you can use the `elem()` or `e()` methods, and instead of typing
`attribute()` you can use `attr()` or `a()`.

Here is the complete code to build our example XML document using shorthand
methods.

```java
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
```

The following methods are available for adding items to the XML document:

| XML Node             | Methods                    |
| -------------------- | -------------------------- |
| Element              | `element`, `elem`, `e`     |
| Attribute            | `attribute`, `attr`, `a`   |
| Text (Element Value) | `text`, `t`                |
| CDATA                | `cdata`, `data`, `d`       |
| Comment              | `comment`, `cmnt`, `c`     |
| Process Instruction  | `instruction`, `inst`, `i` |
| Reference            | `reference`, `ref`, `r`    |

### Output

XMLBuilder includes two convenient methods for outputting a document. 

You can use the `toWriter` method to print the document to an output stream or
file:
```java
PrintWriter writer = new PrintWriter(new FileOutputStream("projects.xml"));
builder.toWriter(writer, outputProperties);
```

Or you can convert the document straight to a text string:
```java
builder.asString(outputProperties);
```

Both of these output methods take an `outputProperties` parameter that you can
use to control how the output is generated. Any output properties you provide
are forwarded to the underlying Transformer object that is used to serialize
the XML document. 

You might specify any non-standard properties like so:

```java
Properties outputProperties = new Properties();

// Explicitly identify the output as an XML document
outputProperties.put(javax.xml.transform.OutputKeys.METHOD, "xml");

// Pretty-print the XML output (doesn't work in all cases)
outputProperties.put(javax.xml.transform.OutputKeys.INDENT, "yes");

// Get 2-space indenting when using the Apache transformer
outputProperties.put("{http://xml.apache.org/xslt}indent-amount", "2");

// Omit the XML declaration header
outputProperties.put(
    javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
```

If you do not wish to change the default properties for your output, you can
provide a null value for `outputProperties`.

### Accessing the Underlying Document

Because XMLBuilder merely acts as a layer on top of the standard JAXP XML
document building tools, you can easily access the underlying Element or
Document objects if you need to manipulate them in ways that XMLBuilder does
not allow.

To obtain the Element represented by any given XMLBuilder node:
```java
org.w3c.dom.Element element = xmlBuilderNode.getElement();
```

To obtain the entire XML document:
```java
org.w3c.dom.Document doc = builder.getDocument();
```

You can also use the `root()` method to quickly obtain the builder object that
represents the document's root element, no matter deep an element hierarchy
your code has built:

```java
org.w3c.dom.Element rootElement = 
    XMLBuilder.create("This")
        .e("Element")
            .e("Hierarchy")
                .e("Is")
                    .e("Really")
                        .e("Very")
                            .e("Deep")
                                .e("Indeed")
    .root().getElement();
```

### Parse XML

If you already have an XML document to which you need to add nodes or
attributes, you can create a new XMLBuilder instance by parsing an
`InputSource`, `String`, or `File`:

```java
XMLBuilder builder = XMLBuilder.parse(YOUR_XML_DOCUMENT_STRING);
```

Parsing an existing document will produce an XMLBuilder object pointing at the
document's root Element node. If you add elements or attributes to this builder
object, they will be added to the document's root element.

If you need to add nodes elsewhere in the parsed document, you will need to
find the correct location in the document using XPath statements.

### Find Nodes with XPath

To add nodes at a specific point in an XML document, you can use XPath to
obtain an XMLBuilder at the correct location. The `XMLBuilder#xpathFind` method
takes an XPath query string and returns a builder object located at the *first*
Element that matches the query.

```java
XMLBuilder firstLocationBuilder = builder.xpathFind("//Location");
```

Note that the XPath query provided to this method *must resolve to at least one
Element node*.  If the query does not match any nodes, or if the first match is
anything other than an Element, the method will throw an
XPathExpressionException.

Like all other XMLBuilder methods, this method can be easily chained to others
when adding nodes.  Here is an example that adds a second element, `Location2`,
inside the `JetS3t` element of our example document.

```java
builder.xpathFind("//JetS3t").elem("Location2").attr("type", "Testing");
```

To produce:

```xml
<Projects>
  <java-xmlbuilder language="Java" scm="SVN">
    <Location type="URL">http://code.google.com/p/java-xmlbuilder/</Location>
  </java-xmlbuilder>
  <JetS3t language="Java" scm="CVS">
    <Location type="URL">http://jets3t.s3.amazonaws.com/index.html</Location>
    <Location2 type="Testing"/>
  </JetS3t>
</Projects>
```

### Configuring advanced features

When creating or parsing a document you can enable and disable advanced
features by using the more explicit versions of the `parse()` and `create()`
constructors.

You can:

* use the `enableExternalEntities` flag to enable or disable external entities.  
  NOTE: you should leave these disabled, as they are by default, unless you
  really need them because they open you to XML External Entity (XXE) injection
  attacks.
* use the `isNamespaceAware` flag to enable or disable namespace awareness in
  the underlying `DocumentBuilderFactory`.
 

Release History
---------------

See this project's version history in
[CHANGES.md](https://github.com/jmurty/java-xmlbuilder/blob/master/CHANGES.md)

This project was previously hosted on Google Code at
[https://code.google.com/p/java-xmlbuilder/](https://code.google.com/p/java-xmlbuilder/).
Please refer to this old location for historical issue reports and user
questions.
