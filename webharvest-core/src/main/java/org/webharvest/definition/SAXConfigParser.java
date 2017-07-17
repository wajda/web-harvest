package org.webharvest.definition;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.WHConstants;
import org.webharvest.definition.validation.SchemaComponentFactory;
import org.webharvest.exception.ParserException;
import org.webharvest.utils.Stack;
import org.webharvest.utils.XmlUtil;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

/**
 * Created by rba on 16.07.2017.
 */
// TODO rbala Missing javadoc
public class SAXConfigParser implements ConfigParser {

    private static final Logger LOG = LoggerFactory.getLogger(SAXConfigParser.class);

    private class Handler extends DefaultHandler {

        private XmlNode rootNode;

        // working stack of elements
        private Stack<XmlNode> elementStack = new Stack<XmlNode>();

        private Locator locator;

        public Handler() {
            this.setDocumentLocator(new LocatorImpl());
        }

        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        private XmlNode getCurrentNode() {
            return elementStack.isEmpty() ? null : elementStack.peek();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            XmlNode currNode = getCurrentNode();
            if (currNode != null) {
                currNode.addElement(new String(ch, start, length));
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            final XmlNode currNode = getCurrentNode();
            if (StringUtils.isEmpty(uri)
                    || WHConstants.XMLNS_CORE_10_ALIASES.contains(uri)) {
                // if there is no xmlns we assume it is the old WH-config schema,
                // aka 1.0
                uri = WHConstants.XMLNS_CORE_10;
            }
            final XmlNode newNode = new XmlNode(localName, qName, uri, currNode);
            newNode.setLocation(this.locator.getLineNumber(),
                    this.locator.getColumnNumber());
            elementStack.push(newNode);

            if (currNode == null) {
                this.rootNode = newNode;
            }

            final int attrsCount = attributes.getLength();
            for (int i = 0; i < attrsCount; i++) {
                newNode.addAttribute(attributes.getLocalName(i),
                        StringUtils.defaultIfEmpty(attributes.getURI(i), uri),
                        attributes.getValue(i));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (!elementStack.isEmpty()) {
                final XmlNode node = getCurrentNode();
                // addElement(String) adds to temporary buffer. Now consolidate
                // cached updates! String content as node elements (based on new
                // line)
                node.flushText();

                elementStack.pop();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void warning (final SAXParseException e) throws SAXException {
            LOG.warn(e.getMessage());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void error (final SAXParseException e) throws SAXException {
            throw e;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementDefProxy parse(ConfigSource configSource) {
        long startTime = System.currentTimeMillis();

        Handler handler = new Handler();
        try {
            final SAXParserFactory factory = XmlUtil.getSAXParserFactory(false, true);
            factory.setSchema(SchemaComponentFactory.getSchemaFactory().getSchema());
            factory.newSAXParser().parse(new InputSource(configSource.getReader()), handler);

            LOG.info("XML parsed in "
                    + (System.currentTimeMillis() - startTime) + "ms.");

        } catch (IOException e) {
            throw new ParserException(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new ParserException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new ParserException(e.getMessage(), e);
        }

        return new ElementDefProxy(handler.rootNode);
    }

}
