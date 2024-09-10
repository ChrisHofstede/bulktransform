package nl.chrishofstede.bulktransform;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;

import nl.chrishofstede.bulktransform.utils.Parameters;
import nl.chrishofstede.bulktransform.utils.TransformerErrorListener;
import nl.chrishofstede.bulktransform.utils.TransformerLoggingErrorListener;

public class Stylesheet implements Serializable {

	private Templates translet = null;

	/**
	 * Gets a transformer instance of an specific XSLT stylesheet.
	 * 
	 * @return An instance of the Transformer class can transform a source tree into
	 *         a result tree.
	 ** @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	private Transformer getTransformer() throws Exception {
		if (translet == null) {
			throw new Exception("No translet for returning a transformer");
		}

		// Retrieve the translet and return a transformer
		return translet.newTransformer();
	}

	/**
	 * Sets an XSL stylesheet translet from a file.
	 * 
	 * @param stylesheet Stylesheet file name.
	 * @return Compiled stylesheet Templates.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	private void setTranslet(File stylesheet) throws Exception {
		if (stylesheet == null) {
			throw new IllegalArgumentException("stylesheet is null");
		}

		// Create a compiled stylesheet via a transformer factory
		// Make sure Xalan is used
		final TransformerFactory tFactory = TransformerFactory.newInstance(
				"org.apache.xalan.processor.TransformerFactoryImpl", null);

		if (stylesheet.exists()) {
			// Load the stylesheet from local file
			try (InputStream fis = new FileInputStream(stylesheet)) {
				final Source xslt = new StreamSource(fis);
				xslt.setSystemId(stylesheet.toURI().toURL().toString());
				translet = tFactory.newTemplates(xslt);
			}
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param stylesheet Stylesheet file name.
	 * @throws Exception Signals that a non user recoverable error has occurred.
	 */
	Stylesheet(File stylesheet) throws Exception {
		setTranslet(stylesheet);
	}

	/**
	 * Transforms the content of an XML source to a node by using an XSLT
	 * stylesheet.
	 * 
	 * @param node
	 *                   Node to be transformed.
	 * @param parameters
	 *                   Properties object with transformation parameters.
	 * @param result
	 *                   Transformation result output node.
	 * @param log
	 *                   Writer to receive the transformer log output.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public void transformNodeToNode(Node node, Parameters parameters, Node result, Writer log)
			throws Exception {
		if (node == null || result == null) {
			throw new IllegalArgumentException("node or result is null");
		}
		transformSourceToResult(new DOMSource(node), parameters, new DOMResult(result), log);
	}

	/**
	 * Transforms the content of an XML source to a node by using an XSLT
	 * stylesheet.
	 * 
	 * @param node
	 *                   Node to be transformed.
	 * @param parameters
	 *                   Properties object with transformation parameters.
	 * @param result
	 *                   Transformation result output node.
	 * @param log
	 *                   Writer to receive the transformer log output.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public void transformNodeToResult(Node node, Parameters parameters, Result result,
			Writer log) throws Exception {
		if (node == null || result == null) {
			throw new IllegalArgumentException("node or result is null");
		}
		transformSourceToResult(new DOMSource(node), parameters, result, log);
	}

	/**
	 * Transforms the content of an XML source to a stream by using an XSLT
	 * stylesheet.
	 * 
	 * @param node
	 *                   Node to be transformed.
	 * @param parameters
	 *                   Properties object with transformation parameters.
	 * @param result
	 *                   Transformation result output stream.
	 * @param log
	 *                   Writer to receive the transformer log output.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public void transformNodeToStream(Node node, Parameters parameters, OutputStream result,
			Writer log) throws Exception {
		if (node == null || result == null) {
			throw new IllegalArgumentException("node or result is null");
		}
		transformSourceToResult(new DOMSource(node), parameters, new StreamResult(result), log);
	}

	/**
	 * Transforms the content of an XML source to a writer by using an XSLT
	 * stylesheet.
	 * 
	 * @param node
	 *                   Node to be transformed.
	 * @param parameters
	 *                   Properties object with transformation parameters.
	 * @param result
	 *                   Transformation result output stream.
	 * @param log
	 *                   Writer to receive the transformer log output.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public void transformNodeToWriter(Node node, Parameters parameters, Writer result,
			Writer log) throws Exception {
		if (node == null || result == null) {
			throw new IllegalArgumentException("node or result is null");
		}
		transformSourceToResult(new DOMSource(node), parameters, new StreamResult(result), log);
	}

	/**
	 * Transforms the content of an XML document in a DOM by using an XSLT
	 * stylesheet.
	 * 
	 * @param node
	 *                   Node to be transformed.
	 * @param parameters
	 *                   Properties object with transformation parameters.
	 * @return The transformed document as a string.
	 * @throws Exception
	 *                   Signals that a non user recoverable error has occurred.
	 */
	public String transformNodeToString(Node node, Parameters parameters) throws Exception {
		if (node == null) {
			throw new IllegalArgumentException("node is null");
		}
		final StringWriter stringWriter = new StringWriter();

		// Transform XML and place the result in a string
		transformNodeToWriter(node, parameters, stringWriter, null);

		// Return the result as string
		return stringWriter.toString();
	}

	private void transformSourceToResult(Source source, Parameters parameters, Result result,
			Writer log) throws Exception {

		// Obtain a transformer
		final Transformer transformer = getTransformer();

		// Set an logging error listener if logging is required
		if (log != null) {
			transformer.setErrorListener(new TransformerLoggingErrorListener(log));
		} else {
			transformer.setErrorListener(new TransformerErrorListener());
		}

		// Set transformer parameters if any
		if (parameters != null) {
			for (final String key : parameters) {
				transformer.setParameter(key, parameters.getParameter(key));
			}
		}

		// Transform XML and place the result in a string
		transformer.transform(source, result);
	}

}