package org.dwfa.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

/**
 * 
 * Class to add a property value to the database. 
 * This should be executed from the amt-id project.
 * 
 * 
 * @goal setproperties
 * @phase process-classes
 * 
 * 
 */ 
public class DBPropertyCreator extends AbstractMojo {
	
	/**
     * Map to pass property key and value pair to, for setting DB properties from a project.
     *
     * @parameter
     */
	private Map<String,String> propertyMap;
		
	/**
	 * test param
	 * @parameter expression="${project.build.directory}"
	 * @required
	 * 
	 */
	File inputDirectory;
	
	/**
     * File name
     * 
     * @parameter expression="default.txt"
     * @required
     */
    private String fileName;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			try {
				if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName())) {
					return;
	            }
	        } catch (NoSuchAlgorithmException e) {
	        	throw new MojoExecutionException(e.getLocalizedMessage(), e);
	        } 
	        I_TermFactory termFact = LocalVersionedTerminology.get();
	        
	        File f = inputDirectory;
//	        boolean fileRead = false;
	        if ( f.exists() )
	        {
	        System.out.println("-------------------------------------------");		        	
	        System.out.println("--------"+ inputDirectory+" exists-----------------");
	        	File file = new File( f, fileName );
	        	
	        	if(file.exists()){
	        		System.out.println("--------"+ fileName+" exists-----------------");
		        	
		        	BufferedReader reader = new BufferedReader( new FileReader( file ) );
		        	String line = reader.readLine();
		    		while( line != null ){
		    			String [] tokens = line.split("=");
		    			termFact.setProperty(keyTokens[0], tokens[1]);
		    			
		    			line = reader.readLine();
		    		}//End while loop
	        	}//End if
	        }//End if
	        
	        if( propertyMap != null ){
		        for( String key: propertyMap.keySet() ){
		        	String value = propertyMap.get( key );
					termFact.setProperty(key, value);
				}//End for loop	
	        }//End if

		}catch (IOException e){
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}//End method execute
}//End class DBPropertyCreator