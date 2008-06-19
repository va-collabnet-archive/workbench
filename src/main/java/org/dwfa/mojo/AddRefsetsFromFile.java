package org.dwfa.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguage;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.maven.MojoUtil;


/**
* <h1>AddRefsetsFromFile</h1>
* <br>
* <p>This class allows a file that adheres to the refset file specifications,
*    to be read and the refsets added to the database.</p>
* <br>
* <h2>Usage</h2>
* <p>There are two ways in which this goal can be executed.
* 	<ol><li> For the referenceSetFile parameter, pass a directory where all refset files reside. In order<br>
* 	for this to work, files mustfollow the naming standard outlined in the documentation (see below link).<br>
*   i.e boolean.refset, concept.refset ....</li>
* 	<li>For the referenceSetFile parameter, pass a full path to the file to be loaded. Also, pass an extensionType <br>
* 	parameter in the format specified in the parameters javadoc below.</li>
* 	</ol>
*
* </p>
* <br>
* 
* @see https://mgr.cubit.aceworkspace.net/pbl/cubitci/pub/ace-mojo/site/dataimport.html
* @goal load-refset-file
* @phase process-resources
* @requiresDependencyResolution compile
*/
public class AddRefsetsFromFile extends AbstractMojo{
	

	/**
	 * The full file path to the refset file you want to load from.
	 * 
	 * @parameter
	 * @required
	 */
	private File referenceSetFile;
	
	/**
	 * The type of extension you want to load.
	 * <li>i.e. BOOLEAN_EXTENSION, CONCEPT_EXTENSION...</li>
	 * <br></br>
	 * @parameter
	 * 
	 */
	private String extensionType;
	
	
	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	
	/** An internal cache of the new uncommitted extensions, indexed by the member uuid */ 
	private HashMap<UUID, I_ThinExtByRefVersioned> extensions = new HashMap<UUID, I_ThinExtByRefVersioned>();
	
	/*
	 * Mojo execution method.
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		try{
			try {
				if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName())) {
					return;
	            }
	        } catch (NoSuchAlgorithmException e) {
	        	throw new MojoExecutionException(e.getLocalizedMessage(), e);
	        } 

			if( referenceSetFile.isDirectory() && !referenceSetFile.isFile() ){
				/*
				 * Read all files in a given directory and determine the refset type of the file and process the file.
				 */
				for( File file : referenceSetFile.listFiles() ){					
					extensionType = findExtensionType( file.getName() );		
					if(extensionType != null) processFile( file );
				}//End for loop
			}else{
				processFile( referenceSetFile );
			}//End if/else
		}
		catch(Exception e){
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}//End method execute
	
	/*
	 * This method is used to determine the refset type of the file based on the file name.
	 * The naming convention applied to this method is <refset FSN>.<refset type>.refset
	 * Files that do not adhere to this convention may not be processed.
	 */
	private String findExtensionType( String fileName ){
		getLog().info("file name == " + fileName);
		String[] tmpType = fileName.split( "\\." );
		
		int typeIndex = tmpType.length - 2;
				
		
				
		if(typeIndex > -1 ){
			if( tmpType[typeIndex].equalsIgnoreCase( "boolean" ) ) 		return "BOOLEAN_EXTENSION";
			if( tmpType[typeIndex].equalsIgnoreCase( "concept" ) ) 		return "CONCEPT_EXTENSION";
			if( tmpType[typeIndex].equalsIgnoreCase( "conint" ) ) 		return "CONCEPT_INT_EXTENSION";
			if( tmpType[typeIndex].equalsIgnoreCase( "string" ) ) 		return "STRING_EXTENSION";
			if( tmpType[typeIndex].equalsIgnoreCase( "integer" ) ) 		return "INT_EXTENSION";
			if( tmpType[typeIndex].equalsIgnoreCase( "measurement" ) ) 	return "MEASUREMENT_EXTENSION";
			if( tmpType[typeIndex].equalsIgnoreCase( "language" ) ) 	return "LANGUAGE_EXTENSION";
		}
		return null;
	}

	/*
	 * This method reads each line in the file, excluding the first line which is a header, 
	 * and creates an appropriate extension based on the record details.
	 */
	private void processFile( File refsetFile ) throws MojoExecutionException {
		BufferedReader reader = null;
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		try{
			reader = new BufferedReader( new FileReader( refsetFile ) );
			String line = reader.readLine();
			line = reader.readLine();
						
			while ( line != null ) {
				String[] tokens = line.split( "\t" );
				
				int referenceSetId = termFactory.uuidToNative( UUID.fromString( tokens[0] ) );
				UUID memberUUID = UUID.fromString( tokens[1] );
				int statusId = termFactory.uuidToNative( UUID.fromString( tokens[2] ) );
				int componentId = termFactory.uuidToNative( UUID.fromString( tokens[3] ) );
				long versionTime = DATE_FORMAT.parse( tokens[4] ).getTime();
				int pathId = termFactory.uuidToNative( UUID.fromString( tokens[5] ) );
				
				int typeId = termFactory.uuidToNative( RefsetAuxiliary.Concept.valueOf( extensionType ).getUids().iterator().next() );

				// check if the member id already exists - if it does we should be adding a version to the existing
				// extension rather than creating a new extension 
				
				I_ThinExtByRefVersioned extension;
				if ( extensions.containsKey( memberUUID ) ) {
					extension = extensions.get( memberUUID );
				} else {
					int memberId = termFactory.uuidToNativeWithGeneration( memberUUID,
		                    ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
		                    termFactory.getPaths(), Integer.MAX_VALUE );
					extension = termFactory.newExtension( referenceSetId, memberId, componentId, typeId );
					extensions.put( memberUUID, extension );
				}
				
				I_ThinExtByRefPart extPart = null;
				
				if ( termFactory.uuidToNative( RefsetAuxiliary.Concept.BOOLEAN_EXTENSION.getUids() ) == typeId ) {
					extPart = termFactory.newBooleanExtensionPart();		
					((I_ThinExtByRefPartBoolean)extPart).setValue( new Boolean( tokens[6] ).booleanValue() );
				}
				else if ( termFactory.uuidToNative( RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids() ) == typeId ) { 
					extPart = termFactory.newConceptExtensionPart();			
					((I_ThinExtByRefPartConcept)extPart).setConceptId( termFactory.uuidToNative( UUID.fromString( tokens[6] ) ));
				}
				else if ( termFactory.uuidToNative( RefsetAuxiliary.Concept.CONCEPT_INT_EXTENSION.getUids() ) == typeId ) {
					extPart = termFactory.newConceptIntExtensionPart();
					((I_ThinExtByRefPartConceptInt)extPart).setConceptId( componentId );
					((I_ThinExtByRefPartConceptInt)extPart).setIntValue( new Integer( tokens[6] ).intValue() );
				}
				else if ( termFactory.uuidToNative( RefsetAuxiliary.Concept.STRING_EXTENSION.getUids() ) == typeId) {
					extPart = termFactory.newStringExtensionPart();
					((I_ThinExtByRefPartString)extPart).setStringValue( tokens[6] );
				}
				else if ( termFactory.uuidToNative( RefsetAuxiliary.Concept.INT_EXTENSION.getUids() ) == typeId ) {
					extPart = termFactory.newIntegerExtensionPart();
					((I_ThinExtByRefPartInteger)extPart).setValue( new Integer( tokens[6] ).intValue() );
				}
				else if ( termFactory.uuidToNative( RefsetAuxiliary.Concept.MEASUREMENT_EXTENSION.getUids() ) == typeId ) {
					extPart = termFactory.newMeasurementExtensionPart();
					((I_ThinExtByRefPartMeasurement)extPart).setMeasurementValue( new Double( tokens[6] ).doubleValue() );
					((I_ThinExtByRefPartMeasurement)extPart).setUnitsOfMeasureId( new Integer( tokens[7] ).intValue() );
				}
				else if ( termFactory.uuidToNative( RefsetAuxiliary.Concept.LANGUAGE_EXTENSION.getUids() ) == typeId ) {
					extPart = termFactory.newLanguageExtensionPart();
					((I_ThinExtByRefPartLanguage)extPart).setAcceptabilityId( new Integer( tokens[6] ).intValue() );
					((I_ThinExtByRefPartLanguage)extPart).setCorrectnessId( new Integer( tokens[7] ).intValue() );
					((I_ThinExtByRefPartLanguage)extPart).setDegreeOfSynonymyId( new Integer( tokens[8] ).intValue() );
				}
				
				if ( extPart != null ) {
					extPart.setPathId( pathId );
					extPart.setStatus( statusId );
					extPart.setVersion( Integer.MAX_VALUE );
					//extPart.setVersion( termFactory.convertToThinVersion(versionTime) );
					
					extension.addVersion( extPart );
					termFactory.addUncommitted( extension );
				}
							
				line = reader.readLine();
			}//End while loop
			
			reader.close();
			
			termFactory.commit();
			
		}
		catch(Exception e){
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}//End method processFile
}//End class AddRefsetFileToDB