package nl.chrishofstede.bulktransform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.serializer.Method;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import nl.chrishofstede.bulktransform.utils.DOMErrorLogger;

public class DOMBuilder implements Serializable {

	/** DOM builder factory variable. */
	private static DocumentBuilderFactory documentBuilderFactory;
	static {

		// Make sure Xerces is chosen
		documentBuilderFactory = DocumentBuilderFactory.newInstance(
				"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl", null);
		documentBuilderFactory.setNamespaceAware(true);
	}

	private static DocumentBuilder getDocumentBuilder() throws Exception {

		// Set up the document builder
		synchronized (documentBuilderFactory) {
			return documentBuilderFactory.newDocumentBuilder();
		}

	}

	/**
	 * Creates an empty XML document with a <code>Document</code> interface that
	 * represents the XML content.
	 * 
	 * @return Document interface representing the entire XML document.
	 *         Conceptually, it is the root of the document
	 *         tree, and provides the primary access to the document's data.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public final static Document newDocument() throws Exception {

		// Set up the document builder and create an empty document
		final DocumentBuilder builder = getDocumentBuilder();
		return builder.newDocument();
	}

	/**
	 * Creates a document with an empty root element. The document has a
	 * <code>Document</code> interface that represents
	 * the XML content.
	 * 
	 * @param root
	 *             Empty root element of the new document.
	 * 
	 * @return Document interface representing the entire XML document.
	 *         Conceptually, it is the root of the document
	 *         tree, and provides the primary access to the document's data.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public final static Document newRoot(final String root) throws Exception {

		// Create an empty document
		final Document document = newDocument();

		// Create an empty root element and insert it in the document
		document.appendChild(document.createElementNS(null, root));

		// Return the document with an empty root element.
		return document;
	}

	/**
	 * Parses an XML document and creates a <code>Document</code> interface
	 * representation of the XML content.
	 * 
	 * @param path
	 *             Path to document to be parsed in the servlet context.
	 * @return Document interface representing the entire XML document.
	 *         Conceptually, it is the root of the document
	 *         tree, and provides the primary access to the document's data.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public final static Document parseDocumentAtPath(final String path) throws Exception {

		// Set up the document builder
		final DocumentBuilder builder = getDocumentBuilder();

		// Load the XML file in DOM
		return builder.parse(path);
	}

	/**
	 * Parses an XML document and creates a <code>Document</code> interface
	 * representation of the XML content.
	 * 
	 * @param path
	 *             Path to document to be parsed in the servlet context.
	 * @return Document interface representing the entire XML document.
	 *         Conceptually, it is the root of the document
	 *         tree, and provides the primary access to the document's data.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public final static Document parseDocumentAtPath(final File path) throws Exception {

		// Set up the document builder
		final DocumentBuilder builder = getDocumentBuilder();

		// Load the XML file in DOM
		return builder.parse(path);
	}

	/**
	 * Parses an XML string and creates a <code>Document</code> interface
	 * representation of the XML content.
	 * 
	 * @param string
	 *               String with XML content be parsed in the servlet context.
	 * @return Document interface representing the entire XML document.
	 *         Conceptually, it is the root of the document
	 *         tree, and provides the primary access to the document's data.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public final static Document parseString(final String string) throws Exception {

		// Set up the document builder
		final DocumentBuilder builder = getDocumentBuilder();

		// Load the XML file in DOM
		return builder.parse(new InputSource(new StringReader(string)));
	}

	/**
	 * Parses an XML document and creates a <code>Document</code> interface
	 * representation of the XML content.
	 * 
	 * @param input
	 *              InputStream of the document to be parsed in the servlet context.
	 * @return Document interface representing the entire XML document.
	 *         Conceptually, it is the root of the document
	 *         tree, and provides the primary access to the document's data.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public final static Document parseDocumentStream(InputStream input) throws Exception {
		if (input == null) {
			throw new IllegalArgumentException("input is null");
		}

		// Set up the document builder
		final DocumentBuilder builder = getDocumentBuilder();

		// Load the XML file in DOM
		return builder.parse(input);
	}

	/**
	 * Serializes an XML document to an output stream using default values.
	 * 
	 * @param document
	 *                 XML document to be serialized.
	 * @param out
	 *                 Output stream to which the document is serialized.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public final static void serializeXML(Document document, OutputStream out) throws Exception {
		if (document == null || out == null) {
			throw new IllegalArgumentException("document or out is null");
		}

		// Retrieve the XML serializer
		final Properties outputProperties = OutputPropertiesFactory.getDefaultMethodProperties(Method.XML);
		final Serializer serializer = SerializerFactory.getSerializer(outputProperties);

		// Create a writer to serialize the document to
		serializer.setOutputStream(out);

		// A DOM will be serialized
		serializer.asDOMSerializer().serialize(document);
	}

	/**
	 * Serializes an XML document to a file.
	 * 
	 * @param document
	 *                 XML document to be serialized.
	 * @param file
	 *                 File to which the document is serialized.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public final static void serializeXML(Document document, String file) throws Exception {
		if (document == null || file == null) {
			throw new IllegalArgumentException("document or file is null");
		}

		// Serialize to a file
		try (OutputStream out = new FileOutputStream(file)) {
			serializeXML(document, out);
		}
	}

	/**
	 * Walks through the node tree and compacts white space. This function is called
	 * recursively.
	 * 
	 * @param node
	 *             Root node which is searched for white space.
	 */
	public final static void compactWhiteSpace(Node node) {
		if (node != null) {
			switch (node.getNodeType()) {

				// Element
				case Node.ELEMENT_NODE:

					/*
					 * Don't compact the white spaces in attribute values since they hardly ever
					 * contain superfluous white
					 * spaces
					 */

					// Walk the child nodes
					Node child = node.getFirstChild();
					while (child != null) {
						compactWhiteSpace(child);
						child = child.getNextSibling();
					}
					break;

				// Text
				case Node.TEXT_NODE:

					// Compact white space
					final String value = node.getNodeValue();
					if (value != null) {
						node.setNodeValue(value.replaceAll("\\s+", " "));
					}
					break;
			}
		}
	}

	/**
	 * Appends a child node to a parent node of another DOM.
	 * 
	 * @param parent
	 *                 Parent node to which the child node is appended.
	 * @param newChild
	 *                 Child node to be appended.
	 * @return The child node appended.
	 */
	public final static Node appendChild(Node parent, Node newChild) {
		if (parent == null) {
			throw new IllegalArgumentException("parent is null");
		}

		// Retrieve the document node
		final Document document = (parent.getNodeType() == Node.DOCUMENT_NODE) ? (Document) parent
				: parent.getOwnerDocument();
		if (document != null) {
			return parent.appendChild(document.importNode(newChild, true));
		}
		throw new DOMException(DOMException.NOT_FOUND_ERR, "Parent node has no document element");
	}

	/**
	 * Validates the document through as if the document was going through a save
	 * and load cycle.
	 * 
	 * @param document
	 *                 Document to be validated.
	 * @param log
	 *                 Logs the errors and warnings in the document
	 * @return True if the document is valid. Warnings are however ignored.
	 */
	public final static boolean validate(Document document, Writer log) {

		// Setup DOM error handler
		final DOMErrorLogger errorHandler = new DOMErrorLogger(log);

		// Set the DOM configuration
		final DOMConfiguration config = document.getDomConfig();
		config.setParameter("error-handler", errorHandler);
		config.setParameter("schema-type", XMLConstants.W3C_XML_SCHEMA_NS_URI);
		config.setParameter("validate", Boolean.TRUE);

		/*
		 * This method acts as if the document was going through a save and load cycle,
		 * putting the document in a
		 * "normal" form.
		 */
		document.normalizeDocument();

		// True if the document is valid
		return errorHandler.isValid();
	}

	/**
	 * This method acts as if the document was going through a save and load cycle,
	 * putting the document in a "normal"
	 * form.
	 * 
	 * @param document
	 *                 Document to be normalized.
	 */
	public final static void normalize(Document document) {

		// Set the DOM configuration
		final DOMConfiguration config = document.getDomConfig();
		config.setParameter("schema-type", XMLConstants.W3C_XML_SCHEMA_NS_URI);
		config.setParameter("validate", Boolean.TRUE);

		/*
		 * This method acts as if the document was going through a save and load cycle,
		 * putting the document in a
		 * "normal" form.
		 */
		document.normalizeDocument();
	}
}
