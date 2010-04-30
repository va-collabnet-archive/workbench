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
package org.ihtsdo.mojo.mojo.comparator;

import java.util.Comparator;

import org.dwfa.ace.api.I_DescriptionTuple;

/**
 * This comparator sorts a list of tuples, firstly by description id (arbitrary
 * value,
 * but can be used to group versions) and then by description version
 */
public class TupleComparator implements Comparator {

    public int compare(Object o, Object o1) {

        I_DescriptionTuple t1 = (I_DescriptionTuple) o;
        I_DescriptionTuple t2 = (I_DescriptionTuple) o1;

        int result = Integer.valueOf(t1.getDescId()).compareTo(t2.getDescId());

        if (result != 0) {
            return result;
        } else {
            return Integer.valueOf(t1.getVersion()).compareTo(t2.getVersion());
        }
    }
}
