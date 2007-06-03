package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_ProcessDescriptions;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;

/**
 * 
 * @goal vodb-count
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbCount extends AbstractMojo {

	private class Counter implements I_ProcessDescriptions {

		private int descCount = 0;
		
		public void processDescription(I_DescriptionVersioned desc) throws Exception {
			descCount++;
		}

		public int getDescCount() {
			return descCount;
		}

		public void setDescCount(int descCount) {
			this.descCount = descCount;
		}
		
	}
	
	public void execute() throws MojoExecutionException, MojoFailureException {
			I_TermFactory termFactory = LocalVersionedTerminology.get();
			Counter descCounter = new Counter();
			try {
				termFactory.iterateDescriptions(descCounter);
				getLog().info("Desc count: " + descCounter.getDescCount());
			} catch (Exception e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}		
	}
	
}
