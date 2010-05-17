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
package org.dwfa.util;

public class EqualUtil {
    public static boolean equals(Object thisValue, Object anotherValue) {
        if ((thisValue == anotherValue) == false) {
            if (thisValue == null) {
                return false;
            }
            if (thisValue.equals(anotherValue) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean unequal(Object thisValue, Object anotherValue) {
        return equals(thisValue, anotherValue) == false;
    }

}
