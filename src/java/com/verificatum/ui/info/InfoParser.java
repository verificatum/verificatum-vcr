
/*
 * Copyright 2008-2018 Douglas Wikstrom
 *
 * This file is part of Verificatum Core Routines (VCR).
 *
 * VCR is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * VCR is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with VCR. If not, see <http://www.gnu.org/licenses/>.
 */

package com.verificatum.ui.info;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

// Secure XML parsing is non-trivial and it seems that the
// implementors of parsers struggle to handle the complexity
// correctly. In retrospect it may have been easier to write a
// stand-alone recursive descent parser from scratch that is feasible
// to audit properly, since our requirements are modest. We may do
// this in the future, but for now we have shut down any resolution of
// entities DTDs etc to the best of our ability, and we have
// deliberately done this redundently.

/**
 * Parser of XML files. This is a simple wrapper of the SAX parser of
 * the Java class library. It takes an <code>Info</code> instance and
 * a file as input, reads the schema the instance represents, and then
 * parses the XML code from the file according to the schema. Any
 * values encountered are stored in the <code>Info</code> instance.
 *
 * @author Douglas Wikstrom
 */
public final class InfoParser extends DefaultHandler {

    /**
     * To allow nested <code>Info</code> instances, we store instances
     * in a stack in the obvious way.
     */
    private final Stack<Info> stack;

    /**
     * Keeps the content so far between the starting and ending tags
     * during parsing.
     */
    private StringBuffer currentContent = new StringBuffer();

    /**
     * Creates an empty instance.
     */
    public InfoParser() {
        stack = new Stack<Info>();
    }

    /**
     * Parses the XML code in the given file according to the schema
     * of the <code>RootInfo</code> instance and fills this instance
     * with the content parsed from file.
     *
     * @param pi <code>RootInfo</code> containing schema and
     * destination of values parsed from file.
     * @param file XML code.
     * @throws InfoException If the file is incorrectly formatted.
     */
    public void parse(final RootInfo pi, final File file) throws InfoException {

        try {

            // Create schema factory.
            final SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Create schema
            final String schemaString = pi.generateSchema();
            final StreamSource ss =
                new StreamSource(new StringReader(schemaString));
            final Schema schema = schemaFactory.newSchema(ss);

            // Use a validating parser following the schema read
            // above.
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // We make sure defaults are set as claimed.
            factory.setXIncludeAware(false);
            factory.setNamespaceAware(false);

            factory.setSchema(schema);

            // Create parser.
            final SAXParser saxParser = factory.newSAXParser();
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                                  XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Do the parsing
            try {
                stack.push(pi);
                saxParser.parse(file, this);
            } catch (final SAXParseException spe) {
                throw new InfoException("Error on line: " + spe.getLineNumber()
                                        + " column: "
                                        + spe.getColumnNumber() + ": "
                                        + spe.getMessage(),
                                        spe);
            }
        } catch (final SAXException saxe) {
            throw new InfoException("Unable to create or use parser!", saxe);
        } catch (final ParserConfigurationException pce) {
            throw new InfoException("Unable to configure parser!", pce);
        } catch (final IOException ioe) {
            throw new InfoException("Unable to read files!", ioe);
        }
    }

    // These methods are documented in the super class.

    @Override
    public void startElement(final String namespaceURI,
                             final String sName, // simple name (localName)
                             final String qName, // qualified name
                             final Attributes attrs)
        throws SAXException {

        final Info info = stack.peek().startElement(qName);

        // Push the info onto the stack if it is not null and not the
        // top-most info on the stack.
        if (info != null && info != stack.peek()) {
            stack.push(info);
        }
        currentContent = new StringBuffer();
    }

    @Override
    public void endElement(final String namespaceURI,
                           final String sName,
                           final String qName)
        throws SAXException {
        try {
            final Info info =
                stack.peek().endElement(currentContent.toString(), qName);
            if (info == null) {
                stack.pop();
            }
        } catch (final InfoException ie) {
            throw new SAXException("Invalid content!", ie);
        }
    }

    @Override
    public void characters(final char[] buf,
                           final int offset,
                           final int len)
        throws SAXException {
        currentContent.append(buf, offset, len);
    }

    // If any unexpected event occurs, then we throw an exception.

    @Override
    public InputSource resolveEntity(final String publicId,
                                     final String systemId)
        throws IOException, SAXException {
        throw new SAXException("Attempting to resolve entity!");
    }

    @Override
    public void notationDecl(final String name,
                             final String publicId,
                             final String systemId)
        throws SAXException {
        throw new SAXException("Notation declarations are not allowed! ("
                               + name + ")");
    }

    @Override
    public void processingInstruction(final String target,
                                      final String data)
        throws SAXException {
        throw new SAXException("Processing instructions are not allowed!");
    }

    @Override
    public void skippedEntity(final String name)
        throws SAXException {
        throw new SAXException("Skipped an entity! (" + name + ")");
    }

    @Override
    public void unparsedEntityDecl(final String name,
                                   final String publicId,
                                   final String systemId,
                                   final String notationName)
        throws SAXException {
        throw new SAXException("Unparsed entity declaration! (" + name + ")");
    }

    @Override
    public void startPrefixMapping(final String prefix,
                                   final String uri)
        throws SAXException {
        throw new SAXException("Prefix mappings are not allowed! ("
                               + prefix + ")");
    }

    @Override
    public void warning(final SAXParseException e)
        throws SAXParseException {
        throw e;
    }

    @Override
    public void error(final SAXParseException e)
        throws SAXParseException {
        throw e;
    }

    @Override
    public void fatalError(final SAXParseException e)
        throws SAXParseException {
        throw e;
    }
}
