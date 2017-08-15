/*
 * Element.java
 */

package cz.kebrt.html2latex.parser;

/**
 * Abstract class for HTML start and end elements (tags).
 */
public abstract class MyElement {
	/** Element's name */
	protected String _element;

	/**
	 * Returns element's name.
	 * 
	 * @return element's name
	 */
	public String getElementName() {
		return _element;
	}
}
