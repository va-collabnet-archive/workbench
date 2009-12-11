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
package org.dwfa.mojo.relformat.util;

import java.util.ArrayList;
import java.util.List;

public final class StringArrayCleanerImpl implements StringArrayCleaner {

    public String[] clean(final String[] array) {
        List<String> cleanedValues = new ArrayList<String>();

        for (String value : array) {
            if (value.trim().length() != 0) {
                cleanedValues.add(value.trim());
            }
        }

        return cleanedValues.toArray(new String[cleanedValues.size()]);

    }
}
