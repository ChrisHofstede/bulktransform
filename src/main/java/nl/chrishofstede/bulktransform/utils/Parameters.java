package nl.chrishofstede.bulktransform.utils;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

public class Parameters extends Properties implements Iterable<String> {

	public String setParameter(String key, Object value) {
		return (String) super.setProperty(key, value.toString());
	}

	public String setParameter(String key, String value) {
		return (String) super.setProperty(key, value);
	}

	public String getParameter(String key) {
		return super.getProperty(key);
	}

	public String removeParameter(String key) {
		return (String) super.remove(key);
	}

	/**
	 * Returns an iterator over a set of elements of type T.
	 * 
	 * @return an Iterator.
	 */
	@Override
	public Iterator<String> iterator() {
		return new KeyIterator(super.propertyNames());
	}

	class KeyIterator implements Iterator<String> {

		/** Index of the current element. */
		private final Enumeration<?> keys;

		KeyIterator(Enumeration<?> keys) {
			this.keys = keys;
		}

		/**
		 * Returns <tt>true</tt> if the iteration has more elements. (In other words,
		 * returns <tt>true</tt> if
		 * <tt>next</tt> would return an element rather than throwing an exception.)
		 * 
		 * @return <tt>true</tt> if the iterator has more elements.
		 */
		@Override
		public boolean hasNext() {
			return keys.hasMoreElements();
		}

		/**
		 * Returns the next key in the iteration.
		 * 
		 * @return the next key in the iteration.
		 * @exception NoSuchElementException
		 *                                   iteration has no more elements.
		 */
		@Override
		public String next() {
			return (String) keys.nextElement();
		}

		/**
		 * Removes from the underlying collection the last element returned by the
		 * iterator (optional operation). This
		 * method can be called only once per call to <tt>next</tt>. The behavior of an
		 * iterator is unspecified if the
		 * underlying collection is modified while the iteration is in progress in any
		 * way other than by calling this
		 * method.
		 * 
		 * @exception UnsupportedOperationException
		 *                                          if the <tt>remove</tt> operation is
		 *                                          not supported by this Iterator.
		 * 
		 * @exception IllegalStateException
		 *                                          if the <tt>next</tt> method has not
		 *                                          yet been called, or the
		 *                                          <tt>remove</tt> method has already
		 *                                          been called after the last call to
		 *                                          the <tt>next</tt> method.
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
