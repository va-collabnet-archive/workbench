package org.ihtsdo.rf2.module.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

// TODO: Auto-generated Javadoc
/**
 * The Class Column.
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class Column {

	/** The name. */
	private String name;
	
	/** The descripton. */
	private String descripton;
	
	/** The delimiter. */
	private String delimiter;

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the descripton.
	 *
	 * @return the descripton
	 */
	public String getDescripton() {
		return descripton;
	}

	/**
	 * Sets the descripton.
	 *
	 * @param descripton the new descripton
	 */
	public void setDescripton(String descripton) {
		this.descripton = descripton;
	}

	/**
	 * Gets the delimiter.
	 *
	 * @return the delimiter
	 */
	public String getDelimiter() {
		return delimiter;
	}

	/**
	 * Sets the delimiter.
	 *
	 * @param delimiter the new delimiter
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

}
