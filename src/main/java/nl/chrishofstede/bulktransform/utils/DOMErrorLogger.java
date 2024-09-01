package nl.chrishofstede.bulktransform.utils;

import java.io.IOException;
import java.io.Writer;

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.Node;

public class DOMErrorLogger implements org.w3c.dom.DOMErrorHandler {

	// Writer to receive the log output
	private final Writer writer;

	// Error occurred
	private boolean bError = false;

	// OS independent line feed
	public static final String LF = System.getProperty("line.separator");

	/**
	 * Constructor TransformerErrorListener
	 * 
	 * @param writer
	 *               Writer to receive the log output
	 */
	public DOMErrorLogger(Writer writer) {
		this.writer = writer;
	}

	/**
	 * This method is called on the error handler when an error occurs. If an
	 * exception is thrown from this method, it
	 * is considered to be equivalent of returning <code>true</code>.
	 * 
	 * @param error
	 *              The error object that describes the error. This object may be
	 *              reused by the DOM implementation across
	 *              multiple calls to the <code>handleError</code> method.
	 * @return If the <code>handleError</code> method returns <code>false</code>,
	 *         the DOM implementation should stop the
	 *         current processing when possible. If the method returns
	 *         <code>true</code>, the processing may continue
	 *         depending on <code>DOMError.severity</code>.
	 */
	@Override
	public boolean handleError(DOMError error) {
		boolean bNonFatal = true;
		try {
			switch (error.getSeverity()) {

				/*
				 * A SEVERITY_WARNING will not cause the processing to stop, unless
				 * DOMErrorHandler.handleError() returns
				 * false.
				 */
				case DOMError.SEVERITY_WARNING:
					writer.write("Warning: ");
					break;

				/*
				 * A SEVERITY_ERROR may not cause the processing to stop if the error can be
				 * recovered, unless
				 * DOMErrorHandler.handleError() returns false.
				 */
				case DOMError.SEVERITY_ERROR:
					writer.write("Error: ");
					bError = true;
					break;

				/*
				 * A SEVERITY_FATAL_ERROR will cause the normal processing to stop. The return
				 * value of
				 * DOMErrorHandler.handleError() is ignored unless the implementation chooses to
				 * continue, in which case the
				 * behavior becomes undefined.
				 */
				case DOMError.SEVERITY_FATAL_ERROR:
					writer.write("Fatal: ");
					bError = true;
					bNonFatal = false;
					break;
			}

			// Log the error message
			writer.write(error.getMessage() + LF);

			// Retrieve information about the location
			final DOMLocator locator = error.getLocation();
			writer.write("Location: " + LF);
			final int line = locator.getLineNumber();
			if (line >= 0) {
				writer.write("- Line  : " + line + LF);
			}
			final int column = locator.getColumnNumber();
			if (column >= 0) {
				writer.write("- Column: " + column + LF);
			}
			final Node node = locator.getRelatedNode();
			if (node != null) {
				writer.write("- Node  : ");
				if (node.getNodeType() == Node.ATTRIBUTE_NODE) {

					// Attribute
					writer.write(node.getNodeName() + " " + node.getNodeValue());
				} else {

					// Element or so, compact white space
					writer.write(NodeWriter.writeNode(node).replaceAll("\\s+", " "));
				}
				writer.write(LF);
			}
			final String uri = locator.getUri();
			if (uri != null) {
				writer.write("- URI   : " + uri + LF);
			}
			writer.write(LF);
		} catch (final IOException io) {

			// This should better not happen
			System.err.println(io.getLocalizedMessage());
		}

		// Continue to check the whole document unless a fatal error has occurred
		return bNonFatal;
	}

	/**
	 * Returns <code>true</code> if the document is valid.
	 * 
	 * @return True if the document is valid.
	 */
	public boolean isValid() {
		return !bError;
	}
}
