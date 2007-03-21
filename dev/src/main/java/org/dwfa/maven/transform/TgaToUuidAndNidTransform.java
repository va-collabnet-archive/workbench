package org.dwfa.maven.transform;

import org.dwfa.maven.Transform;
import org.dwfa.maven.transform.AbstractTransform;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.util.id.Type5UuidFactory;

import java.util.Map;
import java.util.UUID;

/**
 * 
 *
 */

public class TgaToUuidAndNidTransform extends AbstractTransform {
	
	private Map uuidToNativeMap;
	private Map nativeToUuidMap;
	private Map sourceToUuidMap;
	private Map uuidToSourceMap;
	
	private String namespace;
	
	private UUID conceptNameSpace = UUID.fromString("1148d572-d6aa-11db-8314-0800200c9a66");
	private UUID fullySpecNameSpace = UUID.fromString("1148d573-d6aa-11db-8314-0800200c9a66");
	private UUID prefTermNameSpace = UUID.fromString("1148d574-d6aa-11db-8314-0800200c9a66");
	private UUID ingredientSubstanceNameSpace = UUID.fromString("1148d575-d6aa-11db-8314-0800200c9a66");
	private UUID reservedNameSpace2 = UUID.fromString("1148d576-d6aa-11db-8314-0800200c9a66");
	
	public void setupImpl(Transform transformer) {
		uuidToNativeMap = transformer.getUuidToNativeMap();
		nativeToUuidMap = transformer.getNativeToUuidMap();
		sourceToUuidMap = transformer.getSourceToUuidMap("tga");
		uuidToSourceMap = transformer.getUuidToSourceMap("tga");
	}

	public String transform(String input) throws Exception {
		
		UUID namespaceUuid;
		if(namespace.equals("concept")) {
			namespaceUuid = conceptNameSpace;
		}
		else if(namespace.equals("fsn")) {
			namespaceUuid = fullySpecNameSpace;
		}
		else if(namespace.equals("pref")) {
			namespaceUuid = prefTermNameSpace;
		}
		else if(namespace.equals("ingredient substance rel")) {
			namespaceUuid = ingredientSubstanceNameSpace;
		}
		else {
			throw new UnsupportedOperationException("Don't know how to handle namespace: " + namespace);
		}
		
		UUID tgaUuid = Type5UuidFactory.get(namespaceUuid, input);
		Integer tgaNid = (Integer)uuidToNativeMap.get(tgaUuid);
		if (tgaNid == null) {

			Integer nativeId = Integer.MIN_VALUE + nativeToUuidMap.size();
			if (uuidToSourceMap.containsKey(tgaUuid)) {
				throw new Exception("Duplicate UUID generated: " + 
						tgaUuid.toString() + " for keys: " + input + " & " + uuidToSourceMap.get(tgaUuid));
			}
			uuidToNativeMap.put(tgaUuid, nativeId);
			nativeToUuidMap.put(nativeId, tgaUuid);
			sourceToUuidMap.put(input, tgaUuid);
			uuidToSourceMap.put(tgaUuid, input);
		}
		return setLastTransform(uuidToNativeMap.get(tgaUuid).toString());
	}
}
