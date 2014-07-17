package com.jamesmurty.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Element;

/**
 * Mappings between prefix strings and namespace URI strings, as required to
 * perform XPath queries on namespaced XML documents.
 *
 * @author jmurty
 */
public class NamespaceContextImpl implements NamespaceContext {
    protected Element element = null;
    protected Map<String, String> prefixToNsUriMap = new HashMap<String, String>();
    protected Map<String, Set<String>> nsUriToPrefixesMap = new HashMap<String, Set<String>>();

    /**
     * Create an empty namespace context.
     */
    public NamespaceContextImpl() {
    }

    /**
     * Create a namespace context that will lookup namespace
     * information in the given element.
     *
     * @param element
     * Element in which to look up namespace information.
     */
    public NamespaceContextImpl(Element element) {
        this.element = element;
    }

    /**
     * Add a custom mapping from prefix to a namespace. This mapping will
     * override any mappings present in this class's XML Element (if provided).
     *
     * @param prefix
     * the namespace's prefix. Use an empty string for the
     * default prefix.
     * @param namespaceURI
     * the namespace URI to map.
     */
    public void addNamespace(String prefix, String namespaceURI) {
        this.prefixToNsUriMap.put(prefix, namespaceURI);
        if (this.nsUriToPrefixesMap.get(namespaceURI) == null) {
            this.nsUriToPrefixesMap.put(namespaceURI, new HashSet<String>());
        }
        this.nsUriToPrefixesMap.get(namespaceURI).add(prefix);
    }

    public String getNamespaceURI(String prefix) {
        String namespaceURI = this.prefixToNsUriMap.get(prefix);
        if (namespaceURI == null && this.element != null) {
            // Need null to find default namespace, not an empty string
            if (prefix != null && prefix.length() == 0) {
                prefix = null;
            }
            namespaceURI = this.element.lookupNamespaceURI(prefix);
        }
        return namespaceURI;
    }

    public String getPrefix(String namespaceURI) {
        Set<String> prefixes = this.nsUriToPrefixesMap.get(namespaceURI);
        if (prefixes != null && prefixes.size() > 0) {
            return prefixes.iterator().next();
        }
        if (this.element != null) {
            return this.element.lookupPrefix(namespaceURI);
        }
        return null;
    }

    // Not implemented
    @SuppressWarnings({ "rawtypes" })
    public Iterator getPrefixes(String namespaceURI) {
        return Collections.EMPTY_LIST.iterator();
    }

}
