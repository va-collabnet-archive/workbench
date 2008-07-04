package org.dwfa.mojo.refset;

import java.text.SimpleDateFormat;
import java.util.UUID;

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
import org.dwfa.ace.refset.RefsetExtensionType;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.mojo.file.FileHandler;

/**
 * Processes a tab delimited file containing refset specification extensions in the export format.
 * 
 * <p>File columns are:
 * <ul>
 * 		<li>Refset UUID</li>
 * 		<li>Refset Member UUID</li>
 * 		<li>Status UUID</li>
 * 		<li>Component/Concept UUID</li>
 * 		<li>Effective Date</li>
 * 		<li>Path UUID</li>
 * 		<li>Extension Value UUID</li>
 * </ul>
 * 
 * @see org.dwfa.mojo.refset.ExportRefSet
 */
public class ExportedRefsetHandler extends FileHandler<I_ThinExtByRefVersioned> {

	private final SimpleDateFormat EXPORTED_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	
	protected RefsetExtensionType refsetType;

	public ExportedRefsetHandler() {
		super();
		setHasHeader(true);
	}
	
	/**
	 * Get the refset type to be used. This may be defined in the mojo configuration otherwise 
	 * it is determined from the name of the source file being handled.  
	 */
	public RefsetExtensionType getRefsetType() {
		if (refsetType == null && sourceFile != null) {
			return RefsetExtensionType.findByFilename(sourceFile.getName());
		} else {
			return refsetType;
		}
	}

	/**
	 * Sets a specific refset type to be used. 
	 * 
	 * @param extensionType Must be a literal name from the enumeration {@link org.dwfa.ace.refset.RefsetExtensionType}
	 */
	public void setRefsetType(String refsetType) {
		this.refsetType = RefsetExtensionType.valueOf(refsetType);
	}

	@Override
	protected I_ThinExtByRefVersioned processLine(String line) {
		try {
			String[] tokens = line.split( "\t" );
			
			I_TermFactory termFactory = LocalVersionedTerminology.get();
			
			UUID refsetId = UUID.fromString(tokens[0]);
			int refsetNid = termFactory.uuidToNative(refsetId);
			
			UUID memberUUID = UUID.fromString( tokens[1] );
			int statusId = termFactory.uuidToNative( UUID.fromString( tokens[2] ) );
			int componentId = termFactory.uuidToNative( UUID.fromString( tokens[3] ) );
			long versionTime = EXPORTED_DATE_FORMAT.parse( tokens[4] ).getTime();
	
			UUID pathId = UUID.fromString(tokens[5]);
			int pathNid = termFactory.uuidToNative(pathId);
			
			int typeId = termFactory.uuidToNative( 
					getRefsetType().getAuxiliaryConcept().getUids().iterator().next() );
	
			int memberId = termFactory.uuidToNativeWithGeneration( memberUUID,
	                ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
	                termFactory.getPaths(), Integer.MAX_VALUE );
			I_ThinExtByRefVersioned extension = termFactory.newExtension( refsetNid, memberId, componentId, typeId );
			
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
				extPart.setPathId( pathNid );
				extPart.setStatus( statusId );
				extPart.setVersion( termFactory.convertToThinVersion(versionTime) );
			
				extension.addVersion(extPart);
			}		 
			
			return extension;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
