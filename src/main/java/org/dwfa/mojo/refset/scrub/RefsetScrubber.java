package org.dwfa.mojo.refset.scrub;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Mojo to find erroneous refset concept extensions and clean them up.
 * The implementing classes will be defined in the execution configuration.
 * 
 * <hr> Example configuration:
 * <pre> {@code
 * 
 * <configuration>
 *		<conceptFinder implementation="org.dwfa.mojo.refset.scrub.MemberSpecFinder">
 *			<validTypeConcepts>
 *				<concept>
 *					<uuid>125f3d04-de17-490e-afec-1431c2a39e29</uuid>
 *					<description>marked parent member</description>
 *				</concept>
 *				<concept>
 * 					<uuid>cc624429-b17d-4ac5-a69e-0b32448aaf3c</uuid>
 *					<description>normal member</description>
 *				</concept>
 *			</validTypeConcepts>
 *			<reportFile>target/candidates.txt</reportFile>
 *		</conceptFinder>
 *		<conceptHandler implementation="org.dwfa.mojo.refset.scrub.MemberSpecScrubber">
 *			...
 *		</conceptHandler>
 *      ...
 * </configuration>
 *                            
 * } </pre>
 * 
 * @goal scrub-refset 
 * @phase process-classes
 * @requiresDependencyResolution compile
 */
public class RefsetScrubber extends AbstractMojo {

	/**
	 * @parameter
	 * @required
	 */
	ConceptExtFinder conceptFinder;
	
	/**
	 * @parameter
	 * @required
	 */
	ConceptExtHandler conceptHandler;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println("Starting refset scrub");
		
		conceptHandler.process(conceptFinder);
	}

}
