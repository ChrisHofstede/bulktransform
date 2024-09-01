package nl.chrishofstede.bulktransform.utils;

import java.io.Writer;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.utils.SAXSourceLocator;
import org.apache.xml.utils.WrappedRuntimeException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TransformerLoggingErrorListener implements ErrorListener {

	// Writer to receive the log output
	private final Writer writer;

	// OS independent line feed
	public static final String LF = System.getProperty("line.separator");

	/**
	 * Constructor TransformerErrorListener
	 * 
	 * @param writer
	 *               Writer to receive the log output
	 */
	public TransformerLoggingErrorListener(Writer writer) {
		this.writer = writer;
	}

	/**
	 * Receive notification of a warning.
	 * 
	 * <p>
	 * Transformer can use this method to report conditions that are not errors or
	 * fatal errors. The default behaviour
	 * is to take no action.
	 * </p>
	 * 
	 * <p>
	 * After invoking this method, the Transformer must continue with the
	 * transformation. It should still be possible
	 * for the application to process the document through to the end.
	 * </p>
	 * 
	 * @param exception
	 *                  The warning information encapsulated in a transformer
	 *                  exception.
	 * @throws TransformerException
	 *                              This class specifies an exceptional condition
	 *                              that occurred during the transformation process.
	 * @see javax.xml.transform.TransformerException
	 */
	@Override
	public void warning(TransformerException exception) throws TransformerException {
		printMessage(exception);
	}

	/**
	 * Receive notification of a recoverable error.
	 * 
	 * <p>
	 * The transformer must continue to try and provide normal transformation after
	 * invoking this method. It should
	 * still be possible for the application to process the document through to the
	 * end if no other errors are
	 * encountered.
	 * </p>
	 * 
	 * @param exception
	 *                  The warning information encapsulated in a transformer
	 *                  exception.
	 * @throws TransformerException
	 *                              This class specifies an exceptional condition
	 *                              that occurred during the transformation process.
	 * @see javax.xml.transform.TransformerException
	 */
	@Override
	public void error(TransformerException exception) throws TransformerException {
		printMessage(exception);

		// throw exception;
	}

	/**
	 * Receive notification of a non-recoverable error.
	 * 
	 * <p>
	 * The Transformer must continue to try and provide normal transformation after
	 * invoking this method. It should
	 * still be possible for the application to process the document through to the
	 * end if no other errors are
	 * encountered, but there is no guarantee that the output will be useable.
	 * </p>
	 * 
	 * @param exception
	 *                  The warning information encapsulated in a transformer
	 *                  exception.
	 * @throws TransformerException
	 *                              This class specifies an exceptional condition
	 *                              that occurred during the transformation process.
	 * @see javax.xml.transform.TransformerException
	 */
	@Override
	public void fatalError(TransformerException exception) throws TransformerException {
		printMessage(exception);

		// Unrecoverable error
		throw exception;
	}

	/**
	 * Formats the error or warning message and writes it to the log.
	 * 
	 * @param exception
	 *                  superclass of all errors and exceptions.
	 * @throws TransformerException
	 *                              This class specifies an exceptional condition
	 *                              that occurred during the transformation process.
	 */
	public void printMessage(Throwable exception) throws TransformerException {
		try {
			SourceLocator locator = null;
			Throwable cause = exception;

			// Try to find the locator closest to the cause.
			do {

				// Find the locator of the deepest cause
				if (cause instanceof SAXParseException) {
					locator = new SAXSourceLocator((SAXParseException) cause);
				} else if (cause instanceof TransformerException) {
					final SourceLocator causeLocator = ((TransformerException) cause).getLocator();
					if (causeLocator != null) {
						locator = causeLocator;
					}
				}

				// Find the deepest cause
				if (cause instanceof TransformerException) {
					cause = ((TransformerException) cause).getCause();
				} else if (cause instanceof WrappedRuntimeException) {
					cause = ((WrappedRuntimeException) cause).getException();
				} else if (cause instanceof SAXException) {
					cause = ((SAXException) cause).getException();
				} else {
					cause = null;
				}
			} while (cause != null);

			if (locator != null) {

				// A locator has been found
				final HRefParser href = new HRefParser();

				String id;
				href.parse(locator.getPublicId());
				if (href.length() != 0) {

					// Public ID
					id = href.getFileName();
				} else {
					href.parse(locator.getSystemId());
					if (href.length() != 0) {

						// System ID
						id = href.getFileName();
					} else {

						// "SystemId Unknown";
						id = XMLMessages.createXMLMessage(XMLErrorResources.ER_SYSTEMID_UNKNOWN, null);
					}
				}

				writer.write(id);
			} else {
				writer.write("(" + XMLMessages.createXMLMessage(XMLErrorResources.ER_LOCATION_UNKNOWN, null) + ")");
			}

			// Write the message
			writer.write(": " + exception.getLocalizedMessage() + LF);
		} catch (final Exception e) {

			// Rethrow the exception as a TransformerException
			throw new TransformerException(e);
		}
	}
}
