package nl.chrishofstede.bulktransform.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

public class HRefParser {

	/** Marker to identify the anchor in an HREF. */
	public static final char ANCHOR_MARKER = '#';

	// String containing the HREF.
	private String href;

	/**
	 * Constructor.
	 */
	public HRefParser() {
		this.href = null;
	}

	/**
	 * Constructor.
	 * 
	 * @param href
	 *             String containing the HRef.
	 */
	public HRefParser(String href) {
		this.href = href;
	}

	/**
	 * Sets the HREF for parsing.
	 * 
	 * @param hRef
	 *             String containing the HRef.
	 */
	public void parse(String hRef) {
		this.href = hRef;
	}

	/**
	 * Retrieves the name of file referenced by the HREF.
	 * 
	 * @return The name of the reference file or null otherwise.
	 */
	public String getFileName() {
		String fileName = null;
		if (href != null) {

			// Determine the first letter of the file name
			int firstLetterFileName = href.lastIndexOf("/");
			firstLetterFileName = (firstLetterFileName < 0) ? 0 : firstLetterFileName + 1;

			// Determine the anchor part of the HREF
			int anchorIndex = href.lastIndexOf(ANCHOR_MARKER);
			if (anchorIndex < 0 || anchorIndex < firstLetterFileName) {
				anchorIndex = href.length();
			}

			// Get the file name part
			if (anchorIndex > firstLetterFileName) {
				fileName = unescapeUrlPath(href.substring(firstLetterFileName, anchorIndex));
			}
		}
		return fileName;
	}

	/**
	 * Retrieves the path of file referenced by the HREF.
	 * 
	 * @return The path of the reference file or null otherwise.
	 */
	public String getPath() {
		String path = null;
		if (href != null) {

			// Determine the last slash in the HREF
			final int lastPathSeparatorIndex = href.lastIndexOf("/");
			if (lastPathSeparatorIndex >= 0) {
				path = unescapeUrlPath(href.substring(0, lastPathSeparatorIndex));
			}
		}
		return path;
	}

	/**
	 * Retrieves the anchor referenced by the HREF.
	 * 
	 * @return The anchor in the reference file or null otherwise.
	 */
	public String getAnchor() {
		String anchor = null;
		if (href != null) {

			// Determine the first letter of the file name
			int firstLetterFileName = href.lastIndexOf("/");
			firstLetterFileName = (firstLetterFileName < 0) ? 0 : firstLetterFileName + 1;

			// Determine the anchor part of the HREF
			final int anchorIndex = href.lastIndexOf(ANCHOR_MARKER);
			if (anchorIndex >= 0 && anchorIndex >= firstLetterFileName) {
				anchor = unescapeUrlPath(href.substring(anchorIndex + 1));
			}
		}
		return anchor;
	}

	/**
	 * Gets the length of HREF.
	 * 
	 * @return Length of the HREF.
	 */
	public int length() {
		return (href != null) ? href.length() : 0;
	}

	/**
	 * Opens a connection to this HREF and returns an InputStream for reading from
	 * that connection.
	 * 
	 * @throws IOException
	 *                     Signals that an I/O exception of some sort has occurred.
	 *                     This class is the general class of
	 *                     exceptions produced by failed or interrupted I/O
	 *                     operations.
	 * @return An input stream for reading from the HREF connection.
	 */
	public InputStream openStream() throws IOException {
		if (href == null) {
			throw new IllegalArgumentException("href is null");
		}
		final URL url = new URL(href);
		return url.openStream();
	}

	/**
	 * Opens a connection to this HREF within the given context and returns an
	 * InputStream for reading from that
	 * connection.
	 * 
	 * @param context
	 *                The context in which to parse the HREF.
	 * @throws IOException
	 *                     Signals that an I/O exception of some sort has occurred.
	 *                     This class is the general class of
	 *                     exceptions produced by failed or interrupted I/O
	 *                     operations.
	 * @return An input stream for reading from the HREF connection.
	 */
	public InputStream openStream(URL context) throws IOException {
		if (href == null || context == null) {
			throw new IllegalArgumentException("href or context is null");
		}
		final URL url = new URL(context, href);
		return url.openStream();
	}

	/**
	 * Verifies if the resource pointed by the HREF exists.
	 * 
	 * @return True if the resource is accessible and thus exists.
	 */
	public boolean exists() {

		// Check if the resource pointed by the HREF exists by trying to open it.
		if (href != null) {
			try (InputStream input = openStream()) {
				return true;
			} catch (final IOException io) {

				// The resource cannot be opened so it does not exist as far as the servlet is
				// concerned.
			}
		}
		return false;
	}

	/**
	 * Verifies if the resource pointed by the HREF exists in a certain context.
	 * 
	 * @param context
	 *                The context in which to verify the HREF.
	 * @return True if the resource is accessible and thus exists.
	 */
	public boolean exists(URL context) {
		if (context == null) {
			throw new IllegalArgumentException("context is null");
		}
		try {

			// Check if the resource pointed by the HREF exists by trying to open it.
			if (href != null) {
				final URL url = new URL(context, href);
				try (InputStream input = url.openStream()) {
					return true;
				}
			}
		} catch (final IOException io) {

			// The resource cannot be opened so it does not exist as far as the servlet is
			// concerned.
		}
		return false;
	}

	/**
	 * Unescapes escaped UTF-8 characters in a URL path to regular characters.
	 * 
	 * @param path
	 *             URL path to be unescaped.
	 * @return An unescaped URL path.
	 */
	public static String unescapeUrlPath(String path) {
		final StringWriter writer = new StringWriter();
		if (path != null) {
			final int len = path.length();
			int moreBytes = 0;
			int sumBytes = 0;
			for (int i = 0; i < len; i++) {
				char c = path.charAt(i);
				if (c == '%' && i + 2 < len) {
					char kar;
					c = path.charAt(++i);
					kar = (char) ((Character.isDigit(c) ? c - '0' : 10 + Character.toUpperCase(c) - 'A') << 4);
					c = path.charAt(++i);
					kar |= Character.isDigit(c) ? c - '0' : 10 + Character.toUpperCase(c) - 'A';
					c = kar;
				}

				// Decode char c as UTF-8, sumBytes collects incomplete chars
				if ((c & 0xC0) == 0x80) {

					// 10xxxxxx (continuation byte)
					// Add 6 bits to sumBytes
					sumBytes = (sumBytes << 6) | (c & 0x3F);
					if (--moreBytes == 0) {

						// Write special character
						writer.write((char) sumBytes);
					}
				} else if ((c & 0x80) == 0x00) {

					// 0xxxxxxx (yields 7 bits)
					// Write regular character
					writer.write(c);
				} else if ((c & 0xE0) == 0xC0) {

					// 110xxxxx (yields 5 bits)
					sumBytes = c & 0x1F;

					// Expect 1 more byte
					moreBytes = 1;
				} else if ((c & 0xF0) == 0xE0) {

					// 1110xxxx (yields 4 bits)
					sumBytes = c & 0x0F;

					// Expect 2 more bytes
					moreBytes = 2;
				} else if ((c & 0xF8) == 0xF0) {

					// 11110xxx (yields 3 bits)
					sumBytes = c & 0x07;

					// Expect 3 more bytes
					moreBytes = 3;
				} else if ((c & 0xFC) == 0xF8) {

					// 111110xx (yields 2 bits)
					sumBytes = c & 0x03;

					// Expect 4 more bytes
					moreBytes = 4;
				} else { // if ((c & 0xFE) == 0xFC)

					// 1111110x (yields 1 bit)
					sumBytes = c & 0x01;

					// Expect 5 more bytes
					moreBytes = 5;
				}

				// We don't test if the UTF-8 encoding is well-formed
			}
		}
		return writer.toString();
	}

	/**
	 * Indicates if some other object is "equal to" this one.
	 * 
	 * @param obj
	 *            The reference object with which to compare.
	 * @return True if this object is the same as the object argument; false
	 *         otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		return (href != null) ? href.equals(obj) : false;
	}

	/**
	 * Returns a hash code value for the object. This method is supported for the
	 * benefit of hashtables such as those
	 * provided by <code>java.util.Hashtable</code>.
	 * 
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode() {
		return href.hashCode();
	}

	/**
	 * Returns a string representation of the object.
	 * 
	 * @return A string representation of the object.
	 */
	@Override
	public final String toString() {
		return href;
	}
}
