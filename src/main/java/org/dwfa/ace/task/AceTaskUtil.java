package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;

public class AceTaskUtil {
	@SuppressWarnings("unchecked")
	public static I_GetConceptData getConceptFromObject(Object obj) throws TerminologyException, IOException {
		I_GetConceptData concept = null;
		if (I_GetConceptData.class.isAssignableFrom(obj.getClass())) {
			concept = (I_GetConceptData) obj;
		} else if (TermEntry.class.isAssignableFrom(obj.getClass())) {
			TermEntry conceptEntry = (TermEntry) obj;
			concept = LocalVersionedTerminology.get().getConcept(conceptEntry.ids);
		} else if (UUID.class.isAssignableFrom(obj.getClass())) {
			UUID editPathUuid = (UUID) obj;
			concept = LocalVersionedTerminology.get().getConcept(new UUID[] { editPathUuid });
		} else if (Collection.class.isAssignableFrom(obj.getClass())) {
			Collection<UUID> collection = (Collection<UUID>) obj;
			concept = LocalVersionedTerminology.get().getConcept(collection);
		}
		return concept;
	}

	public static I_GetConceptData getConceptFromProperty(I_EncodeBusinessProcess process, 
			String propName) throws IntrospectionException, IllegalAccessException, InvocationTargetException, TerminologyException, IOException {
		Object editPathobj =  process.readProperty(propName);
		I_GetConceptData editPathConcept = AceTaskUtil.getConceptFromObject(editPathobj);
		return editPathConcept;
	}

}
