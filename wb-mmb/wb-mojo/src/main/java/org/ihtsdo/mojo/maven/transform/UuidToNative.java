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

public class UuidToNative extends AbstractTransform implements I_ReadAndTransform {

    private Map<UUID, Integer> uuidToNativeMap;
    private Map<Integer, UUID> nativeToUuidMap;

    public void setupImpl(Transform transformer) {
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
