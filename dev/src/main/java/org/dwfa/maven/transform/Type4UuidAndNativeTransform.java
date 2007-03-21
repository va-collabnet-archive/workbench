package org.dwfa.maven.transform;

import java.util.Map;
import java.util.UUID;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

public class Type4UuidAndNativeTransform extends AbstractTransform implements I_ReadAndTransform {
	
	/**
	 * @parameter
	 * @required
	 */
	private String source;

	private Map uuidToNativeMap;
	private Map nativeToUuidMap;
	private Map sourceToUuidMap;
	private Map uuidToSourceMap;
	
	public void setupImpl(Transform transformer) {
		uuidToNativeMap = transformer.getUuidToNativeMap();
		nativeToUuidMap = transformer.getNativeToUuidMap();
		sourceToUuidMap = transformer.getSourceToUuidMap(source);
		uuidToSourceMap = transformer.getUuidToSourceMap(source);
	}

	public String transform(String input) throws Exception {
		UUID sourceUuid = (UUID) sourceToUuidMap.get(input);
		if (sourceUuid == null) {
			sourceUuid = UUID.randomUUID();
			Integer nativeId = Integer.MIN_VALUE + nativeToUuidMap.size();
			if (uuidToNativeMap.containsKey(sourceUuid)) {
				throw new Exception("Duplicate UUID generated: " + 
						sourceUuid.toString() + " for keys: " + input + " & " + uuidToSourceMap.get(sourceUuid));
			}
			uuidToNativeMap.put(sourceUuid, nativeId);
			nativeToUuidMap.put(nativeId, sourceUuid);
			sourceToUuidMap.put(input, nativeId);
			uuidToSourceMap.put(nativeId, input);
		}
		return setLastTransform(uuidToNativeMap.get(sourceUuid).toString());
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}


}
