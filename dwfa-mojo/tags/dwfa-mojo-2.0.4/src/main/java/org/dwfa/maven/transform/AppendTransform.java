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

import org.dwfa.maven.Transform;

/**
 * Transforms ingredient field into a Snomed FSD.
 *
 */

public class AppendTransform extends AbstractTransform {

    String param;

    public String transform(String input) throws Exception {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            sb.append(c);
        }
        sb.append(' ');
        sb.append(param);

        if (getChainedTransform() != null) {
            return setLastTransform(getChainedTransform().transform(
                sb.toString()));
        } else {
            return setLastTransform(sb.toString());
        }
    }

    public void setupImpl(Transform transformer) {

    }
}
