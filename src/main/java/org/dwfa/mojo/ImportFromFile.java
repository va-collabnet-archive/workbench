package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.mojo.file.FileHandler;

/**
 * Extends the abstract mojo to ensure a plug-able file handler is provided to the concrete implementation.
 * Any implementations can make use of the extensions created by the defined file handler.
 * 
 */
public abstract class ImportFromFile extends AbstractMojo {

	/**
	 * Defines the FileHandler implementation to be used.
	 *
	 * <p>
	 * Example configuration:
	 * <pre>
	 * &lt;fileHandler implementation="org.dwfa.mojo.refset.ExportedRefsetHandler"&gt;
	 *    ...
	 * &lt;/fileHandler&gt;
	 * 
	 * @parameter 
	 * @required
	 */
	public FileHandler<I_ThinExtByRefVersioned> fileHandler;

}
