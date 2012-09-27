/*
 * 
 */
package org.ihtsdo.mojo.schema.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.ihtsdo.mojo.db.ConceptDescriptor;

/**
 * The Class TransformersConfigApi.<br>
 * This class provides an simplified access to the XML config files used to define the transformation parameters.<p>
 * Example:<br>
 * {@code
 * <config>
 * <transformer>
 *	<id>long-to-int</id>
 *	<class>org.ihtsdo.mojo.schema.transformer.LongToIntegerTransformer
 *	</class>
 *	<parameters>
 *		<refset>
 *			<uuid>cfaabccf-f66a-5d17-be55-84fab0e11919</uuid>
 *			<description>Sample refset concept</description>
 *		</refset>
 *		<scalar>1</scalar>
 *	</parameters>
 *</transformer>
 *</config>
 * }
 * In the example above the transformer preferences are stored within a 'config' tag, each transformer has an id, a reference to the class fully qualified name and a set of parameters.<br>
 * The refset key contains a concept descriptor as values, a reference to both the UUID and a description of a concept. And the scalar key contains a string value.
 */
public class TransformersConfigApi {

	/** The xml config. */
	XMLConfiguration xmlConfig;

	/**
	 * Instantiates a new transformers config api.
	 *
	 * @param configFile the config file
	 */
	public TransformersConfigApi(String configFile) {
		File fileConfigFile = new File(configFile);
		try {
			if (!fileConfigFile.exists()) {
				throw new FileNotFoundException(configFile);
			}
			xmlConfig = new XMLConfiguration(configFile);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a concept descriptor from a transformer's preferences, identified by the index and the preference key (path).
	 *
	 * @param intId the int id
	 * @param path the path
	 * @return the concept descriptor
	 */
	public ConceptDescriptor getConceptDescriptor(int intId, String path) {
		String descriptorUuid = xmlConfig.getString("transformer(" + intId + ")." + path + ".uuid");
		String descriptorDescription = xmlConfig.getString("transformer(" + intId + ")." + path + ".description");
		return new ConceptDescriptor(descriptorUuid, descriptorDescription);
	}

	/**
	 * Gets the transformers index
	 *
	 * @return the transformers index
	 */
	public Map<String, Integer> getTransformersIndex() {
		HashMap<String, Integer> transformers = new HashMap<String, Integer>();

		Object prop = xmlConfig.getProperty("transformer.id");
		List<String> ids = new LinkedList<String>();
		if (prop instanceof Collection) {
			ids.addAll((Collection) prop);
			int i = 0;
			for (String transf : ids) {
				transformers.put(transf, i);
				i++;
			}
		} else if (prop instanceof String) {
			ids.add((String) prop);
			transformers.put((String) prop, 0);
		}
		return transformers;
	}

	/**
	 * Gets the int index for a transformers id.
	 *
	 * @param strId the str id
	 * @return the int id
	 */
	public int getIntId(String strId) {
		return getTransformersIndex().get(strId);
	}

	/**
	 * Gets all the transformers names.
	 *
	 * @return the all transformers names
	 */
	public Collection<String> getAllTransformersNames() {
		return getTransformersIndex().keySet();
	}

	/**
	 * Gets the String value for transformers key, identified by the index and the preference key (path).
	 *
	 * @param intId the int id
	 * @param path the path
	 * @return the value at
	 */
	public String getValueAt(int intId, String path) {
		return xmlConfig.getString("transformer(" + intId + ")." + path);
	}

	/**
	 * Gets the collection value for transformers key, identified by the index and the preference key (path).
	 *
	 * @param intId the int id
	 * @param path the path
	 * @return the collection at
	 */
	public List<String> getCollectionAt(int intId, String path) {
		Object prop = xmlConfig.getProperty("transformer(" + intId + ")." + path);
		List<String> values = new LinkedList<String>();
		if (prop instanceof Collection) {
			values.addAll((Collection) prop);
		}
		return values;
	}
}
