package nl.chrishofstede.bulktransform.utils;

import java.io.StringWriter;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NodeWriter {

	/**
	 * Writes out the content of a node tree as a string.
	 * 
	 * @param node
	 *             Root of the node tree.
	 * @return The string with the content of the node tree.
	 */
	public static String writeNode(Node node) {
		final StringWriter writer = new StringWriter();
		if (node != null) {
			write(writer, node);
			writer.flush();
		}
		return writer.toString();
	}

	/**
	 * Writes out the children of a node tree as a string.
	 * 
	 * @param node
	 *             Root of the node tree.
	 * @return The string with the content of the node tree.
	 */
	public static String writeChildNodes(Node node) {
		final StringWriter writer = new StringWriter();
		if (node != null) {
			Node child = node.getFirstChild();
			while (child != null) {
				write(writer, child);
				child = child.getNextSibling();
			}
			writer.flush();
		}
		return writer.toString();
	}

	/**
	 * Recursively writes out the content of the nodes.
	 * 
	 * @param writer
	 *               The writer that receives the serialized node content.
	 * @param node
	 *               The current node.
	 */
	private static void write(StringWriter writer, Node node) {
		if (node != null) {

			final short type = node.getNodeType();
			switch (type) {

				// Document
				case Node.DOCUMENT_NODE:
					final Document document = (Document) node;
					write(writer, document.getDocumentElement());
					break;

				// Document type
				case Node.DOCUMENT_TYPE_NODE:
					final DocumentType doctype = (DocumentType) node;
					writer.write("<!DOCTYPE ");
					writer.write(doctype.getName());
					final String publicId = doctype.getPublicId();
					final String systemId = doctype.getSystemId();
					if (publicId != null) {
						writer.write(" PUBLIC '");
						writer.write(publicId);
						writer.write("' '");
						writer.write(systemId);
						writer.write('\'');
					} else if (systemId != null) {
						writer.write(" SYSTEM '");
						writer.write(systemId);
						writer.write('\'');
					}
					final String internalSubset = doctype.getInternalSubset();
					if (internalSubset != null) {
						writer.write(" [\n");
						writer.write(internalSubset);
						writer.write(']');
					}
					writer.write(">\n");
					break;

				// Element
				case Node.ELEMENT_NODE:

					// Empty element?
					final boolean bChildren = node.hasChildNodes();

					writer.write('<');
					writer.write(node.getNodeName());

					// Add attributes if any
					final NamedNodeMap attributes = node.getAttributes();
					if (attributes != null) {
						final int count = attributes.getLength();
						for (int i = 0; i < count; i++) {
							final Attr attribute = (Attr) attributes.item(i);
							writer.write(' ');
							writer.write(attribute.getNodeName());
							writer.write("=\"");
							normalizeAndWrite(writer, attribute.getNodeValue(), true);
							writer.write('"');
						}
					}
					if (!bChildren) {
						writer.write('/');
					}
					writer.write('>');

					Node child = node.getFirstChild();
					while (child != null) {
						write(writer, child);
						child = child.getNextSibling();
					}

					// Write end tag element
					if (bChildren) {
						writer.write("</");
						writer.write(node.getNodeName());
						writer.write('>');
					}
					writer.flush();
					break;

				// Entity Reference
				case Node.ENTITY_REFERENCE_NODE:
					writer.write('&');
					writer.write(node.getNodeName());
					writer.write(';');
					break;

				// CDATA section
				case Node.CDATA_SECTION_NODE:
					writer.write("<![CDATA[");
					writer.write(node.getNodeValue());
					writer.write("]]>");
					break;

				// Text
				case Node.TEXT_NODE:
					normalizeAndWrite(writer, node.getNodeValue(), false);
					break;

				// Processing instruction
				case Node.PROCESSING_INSTRUCTION_NODE:
					writer.write("<?");
					writer.write(node.getNodeName());
					final String data = node.getNodeValue();
					if (data != null && data.length() > 0) {
						writer.write(' ');
						writer.write(data);
					}
					writer.write("?>");
					writer.flush();
					break;

				// Comment
				case Node.COMMENT_NODE:
					writer.write("<!--");
					final String comment = node.getNodeValue();
					if (comment != null && comment.length() > 0) {
						writer.write(comment);
					}
					writer.write("-->");
					writer.flush();
			}
		}
	}

	/**
	 * Normalizes and writes the given value string.
	 * 
	 * @param writer
	 *                   The writer that receives the normalized string.
	 * @param value
	 *                   Value string to printed normalized.
	 * @param isAttValue
	 *                   Indicates if the value is an attribute value.
	 */
	private static void normalizeAndWrite(StringWriter writer, String value, boolean isAttValue) {
		if (value != null) {
			final int len = value.length();
			for (int i = 0; i < len; i++) {
				final char c = value.charAt(i);
				switch (c) {
					case '<':
						writer.write("&lt;");
						break;
					case '>':
						writer.write("&gt;");
						break;
					case '&':
						writer.write("&amp;");
						break;
					case '"':

						/*
						 * A '"' that appears in character data does not need to be escaped.
						 */
						if (isAttValue) {
							writer.write("&quot;");
						} else {
							writer.write("\"");
						}
						break;
					case '\r':

						/*
						 * If CR is part of the document's content, it must not be printed as a literal
						 * otherwise it would
						 * be normalized to LF when the document is reparsed.
						 */
						writer.write("&#xD;");
						break;
					default:

						/*
						 * In XML 1.1, control chars in the ranges [#x01-#x1F, #x7F-#x9F] must be
						 * escaped.
						 * 
						 * Escape space characters that would be normalized to #x20 in attribute values
						 * when the document is
						 * reparsed.
						 * 
						 * Escape NEL (0x85) and LSEP (0x2028) that appear in content if the document is
						 * XML 1.1, since they
						 * would be normalized to LF when the document is reparsed.
						 */
						if (((c >= 0x01 && c <= 0x1F && c != 0x09 && c != 0x0A) || (c >= 0x7F && c <= 0x9F)
								|| c == 0x2028)
								|| isAttValue && (c == 0x09 || c == 0x0A)) {
							writer.write("&#x");
							writer.write(Integer.toHexString(c).toUpperCase());
							writer.write(";");
						} else {
							writer.write(c);
						}
				}
			}
		}
	}
}
