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
package org.ihtsdo.mojo.maven.transform;

import java.util.Map;
import java.util.UUID;

import org.ihtsdo.mojo.maven.I_ReadAndTransform;
import org.ihtsdo.mojo.maven.Transform;
import org.dwfa.util.id.Type3UuidFactory;

public class SnomedType3UuidAndNative extends AbstractTransform implements I_ReadAndTransform {

    private Map<UUID, Integer> uuidToNativeMap;
    private Map<Integer, UUID> nativeToUuidMap;
    private Map<String, UUID> sourceToUuidMap;
    private Map<UUID, String> uuidToSourceMap;

    public void setupImpl(Transform transformer) {
        uuidToNativeMap = transformer.getUuidToNativeMap();
        nativeToUuidMap = transformer.getNativeToUuidMap();
        sourceToUuidMap = transformer.getSourceToUuidMap("snomed");
        uuidToSourceMap = transformer.getUuidToSourceMap("snomed");
    }

    public String transform(String input) throws Exception {
        UUID snomedUuid = (UUID) sourceToUuidMap.get(input);
        if (snomedUuid == null) {
            snomedUuid = Type3UuidFactory.fromSNOMED(input);

            Integer nativeId = Integer.MIN_VALUE + nativeToUuidMap.size();
            if (uuidToSourceMap.containsKey(snomedUuid)) {
                throw new Exception("Duplicate UUID generated: " + snomedUuid.toString() + " for keys: " + input
                    + " & " + uuidToSourceMap.get(snomedUuid));
            }
            uuidToNativeMap.put(snomedUuid, nativeId);
            nativeToUuidMap.put(nativeId, snomedUuid);
            sourceToUuidMap.put(input, snomedUuid);
            uuidToSourceMap.put(snomedUuid, input);
        }
        return setLastTransform(uuidToNativeMap.get(snomedUuid).toString());
    }

}
