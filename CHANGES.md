Release Notes for java-xmlbuilder
=================================

Version 1.1 - 22 July 2014
--------------------------

Added a new `XMLBuilder2` implementation that avoids checked exceptions in the
API and throws runtime exceptions instead. This should make the library much
more pleasant to use, and your code much cleaner, in situations where low-level
exceptions are unlikely -- which is probably most situations where you would
use this library.  

For example when creating a new document with the `#create` method, instead of
needing to explicitly catch the unlikely `ParserConfigurationException`, if you
use `XMLBuilder2` this exception automatically gets wrapped in the new
`XMLBuilderRuntimeException` class and can be left to propagate out.  

Aside from the removal of checked exceptions, `XMLBuilder2` has the same API as
the original `XMLBuilder` and should therefore be a drop-in replacement in
existing code.

For further discussion and rationale see:
https://github.com/jmurty/java-xmlbuilder/issues/4

Version 1.0 - 6 March 2014
--------------------------

Jumped version number from 0.7 to 1.0 to better reflect this project's age
and stability, as well as to celebrate the move to GitHub. 

* Migrated project from 
  [Google Code](https://code.google.com/p/java-xmlbuilder/) to
  [GitHub](https://github.com/jmurty/java-xmlbuilder). Whew, that's better!
* Test cases for edge-case issues and questions reported by users.
* Add `parse` methods to parse XML directly from a String or File.
* Allow sub-elements to be added when they will have Text node siblings,
  provided they are whitespace-only text nodes.
* Add `stripWhitespaceOnlyTextNodes` method to strip document of
  whitespace-only text nodes.

Version 0.6 - 28 April 2013
---------------------------

* Add Apache 2.0 LICENSE file to be more explicit about licensing.
* Update iharder Base64 library to 2.3.8 and source via Maven dependency,
  rather than including the source directly.
* New methods 'document' to return builder for root Document node,
  and 'insertInstruction' to insert processing instruction before current node.
* Fail fast if null text value is provided to `text` methods.

Version 0.5 - 17 January 2013
-----------------------------

* Extra CDATA methods to create nodes with strings without any Base64-encoding.
* Serialization of document sub-trees via `toWriter`/`elementAsString` methods.
* Namespace support when building document, and when performing xpath queries
* Perform arbitrary XPath queries using the `xpathQuery` method.
* New `elementBefore()` methods insert a new XML node before the current node.
* Added `text()` method with boolean flag as second parameter to replace a
  node's text value, rather than always appending text.

Version 0.4 - 26 October 2010
-----------------------------

* Fix for ClassCastException that could occur if user attempts to traverse
  beyond the current document's root element using `up()` methods.
* Added method `importXMLBuilder` method to import another XMLBuilder document
  at the current document position.

Version 0.3 - 2 July 2010
-------------------------

* First release to Maven repository.
* Parse existing XML documents with `parse` method.
* Find specific nodes in document with an XPath with `xpathFind`. 
* Added JUnit tests

Version 0.2 - 6 January 2009
----------------------------

* Updated CDATA methods (`cdata`, `data`, `d`) to automatically Base64-encode
  text values using Base64 implementation from http://iharder.net/base64

Version 0.1 - 14 December 2008
------------------------------

* Initial public release
