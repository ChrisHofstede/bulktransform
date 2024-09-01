package nl.chrishofstede.bulktransform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	 * @param stylesheet
	 *            Stylesheet used for the transformation.
	 * @return An instance of the Transformer class can transform a source tree into a result tree.
	 * @throws Exception
	 *             Signals that a non user recoverable error has occurred.
	 */
	public Transformer getTransformer(String stylesheet) throws Exception {
		if (stylesheet == null) {
			throw new IllegalArgumentException("stylesheet is null");
		}

		// Retrieve the stylesheet and return a transformer
		final Templates templates = getTranslet(stylesheet);
		return templates.newTransformer();
	}

	/**
	 * Gets an XSL stylesheet translet from a file.
	 *  
	 * @param stylesheet
	 *            Stylesheet file name.
	 * @return Compiled stylesheet Templates.
	 * @throws Exception
	 *             Signals that a non user recoverable error has occurred.
	 */
	public Templates getTranslet(String stylesheet) throws Exception {
		if (stylesheet == null) {
			throw new IllegalArgumentException("stylesheet is null");
		}

		// Get compiled stylesheet 
		if (translet == null) {
			// Compiled stylesheet not available, retrieve it from the local
			// filesystem (stylesheets folder) or from the database

			// Create a compiled stylesheet via a transformer factory
			// Make sure Xalan is used
			final TransformerFactory tFactory = TransformerFactory.newInstance(
					"org.apache.xalan.processor.TransformerFactoryImpl", null);

			final File lf = new File(stylesheet);
			if (lf.exists()) {
				// Load the stylesheet from local file
				try (InputStream fis = new FileInputStream(lf)) {
					final Source xslt = new StreamSource(fis);
					xslt.setSystemId(stylesheet);
					translet = tFactory.newTemplates(xslt);
					if (translet == null)
							throw new Exception("Could not parse style sheet (see previous error messages for details)");
				}
			
			}
		}

		// Throw IdsServletException if stylesheet has not been found which
		// eases error handling.
		if (translet == null) {
			throw new FileNotFoundException("Stylesheet not found or loaded: " + stylesheet);
		}

		// Return a compiled stylesheet
		return translet;
	}

	/**
	 * Transforms the content of an XML source to a node by using an XSLT stylesheet.
	 * 
	 * @param node
	 *            Node to be transformed.
	 * @param stylesheet
	 *            Stylesheet used for the transformation.
	 * @param parameters
	 *            Properties object with transformation parameters.
	 * @param result
	 *            Transformation result output node.
	 * @param log
	 *            Writer to receive the transformer log output.
	 * @throws Exception
	 *             Signals that a non user recoverable error has occurred.
	 */
	public void transformNodeToNode(Node node, String stylesheet, Parameters parameters, Node result, Writer log)
			throws Exception {
		if (node == null || result == null) {
			throw new IllegalArgumentException("node or result is null");
		}
		transformSourceToResult(new DOMSource(node), stylesheet, parameters, new DOMResult(result), log);
	}

	/**
	 * Transforms the content of an XML source to a node by using an XSLT stylesheet.
	 * 
	 * @param node
	 *            Node to be transformed.
	 * @param stylesheet
	 *            Stylesheet used for the transformation.
	 * @param parameters
	 *            Properties object with transformation parameters.
	 * @param result
	 *            Transformation result output node.
	 * @param log
	 *            Writer to receive the transformer log output.
	 * @throws Exception
	 *             Signals that a non user recoverable error has occurred.
	 */
	public void transformNodeToResult(Node node, String stylesheet, Parameters parameters, Result result,
			Writer log) throws Exception {
		if (node == null || result == null) {
			throw new IllegalArgumentException("node or result is null");
		}
		transformSourceToResult(new DOMSource(node), stylesheet, parameters, result, log);
	}

	/**
	 * Transforms the content of an XML source to a stream by using an XSLT stylesheet.
	 * 
	 * @param node
	 *            Node to be transformed.
	 * @param stylesheet
	 *            Stylesheet used for the transformation.
	 * @param parameters
	 *            Properties object with transformation parameters.
	 * @param result
	 *            Transformation result output stream.
	 * @param log
	 *            Writer to receive the transformer log output.
	 * @throws Exception
	 *             Signals that a non user recoverable error has occurred.
	 */
	public void transformNodeToStream(Node node, String stylesheet, Parameters parameters, OutputStream result,
			Writer log) throws Exception {
		if (node == null || result == null) {
			throw new IllegalArgumentException("node or result is null");
		}
		transformSourceToResult(new DOMSource(node), stylesheet, parameters, new StreamResult(result), log);
	}

	/**
	 * Transforms the content of an XML source to a writer by using an XSLT stylesheet.
	 * 
	 * @param node
	 *            Node to be transformed.
	 * @param stylesheet
	 *            Stylesheet used for the transformation.
	 * @param parameters
	 *            Properties object with transformation parameters.
	 * @param result
	 *            Transformation result output stream.
	 * @param log
	 *            Writer to receive the transformer log output.
	 * @throws Exception
	 *             Signals that a non user recoverable error has occurred.
	 */
	public void transformNodeToWriter(Node node, String stylesheet, Parameters parameters, StringWriter result,
			Writer log) throws Exception {
		if (node == null || result == null) {
			throw new IllegalArgumentException("node or result is null");
		}
		transformSourceToResult(new DOMSource(node), stylesheet, parameters, new StreamResult(result), log);
	}

	/**
	 * Transforms the content of an XML document in a DOM by using an XSLT stylesheet.
	 * 
	 * @param node
	 *            Node to be transformed.
	 * @param stylesheet
	 *            Stylesheet used for the transformation.
	 * @param parameters
	 *            Properties object with transformation parameters.
	 * @return The transformed document as a string.
	 * @throws Exception
	 *             Signals that a non user recoverable error has occurred.
	 */
	public String transformNodeToString(Node node, String stylesheet, Parameters parameters) throws Exception {
		if (node == null) {
			throw new IllegalArgumentException("node is null");
		}
		final StringWriter stringWriter = new StringWriter();

		// Transform XML and place the result in a string
		transformNodeToWriter(node, stylesheet, parameters, stringWriter, null);

		// Return the result as string
		return stringWriter.toString();
	}

	private void transformSourceToResult(Source source, String stylesheet, Parameters parameters, Result result,
			Writer log) throws Exception {

		// Obtain a transformer
		final Transformer transformer = getTransformer(stylesheet);

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