package org.dwfa.maven.transform;

import java.util.Map;
import java.util.UUID;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

public class UuidToNative extends AbstractTransform implements
		I_ReadAndTransform {

	private Map uuidToNativeMap;
	private Map nativeToUuidMap;

	public void setup(Transform transformer) {
		uuidToNativeMap = transformer.getUuidToNativeMap();
		nativeToUuidMap = transformer.getNativeToUuidMap();
	}

	public String transform(String input) throws Exception {
		UUID sourceUuid = UUID.fromString(input);
		Integer nativeId = (Integer) uuidToNativeMap.get(sourceUuid);
		if (nativeId == null) {
			nativeId = Integer.MIN_VALUE + nativeToUuidMap.size();
			uuidToNativeMap.put(sourceUuid, nativeId);
			nativeToUuidMap.put(nativeId, sourceUuid);
		}
		return setLastTransform(nativeId.toString());
	}

}
