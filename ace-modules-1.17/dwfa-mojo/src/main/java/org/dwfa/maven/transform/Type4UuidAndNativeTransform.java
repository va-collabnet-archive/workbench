/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    private Map<UUID, Integer> uuidToNativeMap;
    private Map<Integer, UUID> nativeToUuidMap;
    private Map<String, UUID> sourceToUuidMap;
    private Map<UUID, String> uuidToSourceMap;

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
                throw new Exception("Duplicate UUID generated: " + sourceUuid.toString() + " for keys: " + input
                    + " & " + uuidToSourceMap.get(sourceUuid));
            }
            uuidToNativeMap.put(sourceUuid, nativeId);
            nativeToUuidMap.put(nativeId, sourceUuid);
            sourceToUuidMap.put(input.toString(), sourceUuid);
            uuidToSourceMap.put(sourceUuid, input.toString());
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
