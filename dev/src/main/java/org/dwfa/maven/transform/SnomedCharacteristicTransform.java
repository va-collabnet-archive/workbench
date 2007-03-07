package org.dwfa.maven.transform;

import java.util.Map;
import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;
import org.dwfa.tapi.I_ConceptualizeUniversally;

public class SnomedCharacteristicTransform extends AbstractTransform implements I_ReadAndTransform {

	private Map uuidToNativeMap;
	public void setup(Transform transformer) {
		uuidToNativeMap = transformer.getUuidToNativeMap();
	}

	public String transform(String input) throws Exception {
		I_ConceptualizeUniversally characteristic = ArchitectonicAuxiliary.getSnomedCharacteristicType(Integer.parseInt(input));
		UUID uid = characteristic.getUids().iterator().next();
		return setLastTransform(uuidToNativeMap.get(uid).toString());
	}

}