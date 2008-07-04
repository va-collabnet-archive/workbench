/**
 * 
 */
package org.dwfa.mojo.refset;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptInt;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.mojo.file.FileHandler;
import org.dwfa.mojo.refset.writers.BooleanRefsetHandler;
import org.dwfa.mojo.refset.writers.ConceptIntegerRefsetHandler;
import org.dwfa.mojo.refset.writers.ConceptRefsetHandler;
import org.dwfa.mojo.refset.writers.IntegerRefsetHandler;
import org.dwfa.mojo.refset.writers.MemberRefsetHandler;
import org.dwfa.mojo.refset.writers.StringRefsetHandler;

enum RefsetType {
	CONCEPT(ConceptRefsetHandler.class, ".concept.refset"),
	INTEGER(IntegerRefsetHandler.class, ".integer.refset"),
	STRING(StringRefsetHandler.class, ".string.refset"),
	BOOLEAN(BooleanRefsetHandler.class, ".boolean.refset"),
	CONCEPT_INTEGER(ConceptIntegerRefsetHandler.class, ".concept.integer.refset");

	private Class<? extends MemberRefsetHandler> refsetWriterClass;
	private MemberRefsetHandler refsetWriter = null;
	private String fileExtension = null;
	
	RefsetType(Class<? extends MemberRefsetHandler> refsetWriterClass, String fileExtension) {
		this.refsetWriterClass = refsetWriterClass;
		this.fileExtension = fileExtension;
	}
	
	public static RefsetType getType(I_ThinExtByRefTuple thinExtByRefTuple) {
		I_ThinExtByRefPart part = thinExtByRefTuple.getPart();
		if (part instanceof I_ThinExtByRefPartBoolean) {
			return BOOLEAN;
		} else if (part instanceof I_ThinExtByRefPartConcept) {
			return CONCEPT;
		} else if (part instanceof I_ThinExtByRefPartConceptInt) {
			return CONCEPT_INTEGER;
		} else if (part instanceof I_ThinExtByRefPartInteger) {
			return INTEGER;
		} else if (part instanceof I_ThinExtByRefPartString) {
			return STRING;
		}
		
		throw new EnumConstantNotPresentException(RefsetType.class, "No enum for the class " + thinExtByRefTuple.getClass() + " exists");
	}

	public MemberRefsetHandler getRefsetHandler() throws InstantiationException, IllegalAccessException {
		if (refsetWriter == null) {
			refsetWriter = refsetWriterClass.newInstance();
		}
	
		return refsetWriter;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public static FilenameFilter getFileNameFilter() {
		
		return new FilenameFilter() {
			List<String> filenameExtensions;
			
			public boolean accept(File dir, String name) {
				if (filenameExtensions == null) {
					filenameExtensions = new ArrayList<String>();
					for (RefsetType refsetType : RefsetType.values()) {
						filenameExtensions.add(refsetType.getFileExtension());
					}
				}
				
				for (String extension : filenameExtensions) {
					if (name.endsWith(extension)) {
						return true;
					}
				}
				return false;
			}
			
		};
	}

	public static FileHandler<I_ThinExtByRefPart> getHandlerForFile(File file) throws InstantiationException, IllegalAccessException {
		for (RefsetType refsetType : RefsetType.values()) {
			if (file.getName().endsWith(refsetType.fileExtension)) {
				return refsetType.getRefsetHandler();
			}
		}
		throw new EnumConstantNotPresentException(RefsetType.class, "No handler for " + file + " exists");
	}
	
	
}