package org.dwfa.ace.task.util;

import java.util.List;

/**
 * Copyright (c) 2010 International Health Terminology Standards Development Organisation
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public final class ListUtil {

    private ListUtil() {
    }

    public static String concat(List listOfValues, String seperator) {
        StringBuilder toReturn = new StringBuilder();
        String valueSeparator = "";
        for (Object value : listOfValues) {
            toReturn.append(valueSeparator).append(value.toString());
            valueSeparator = seperator;
        }
        return toReturn.toString();
    }
}
