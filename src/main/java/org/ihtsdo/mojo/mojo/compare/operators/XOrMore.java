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
package org.ihtsdo.mojo.mojo.compare.operators;

import java.util.HashSet;
import java.util.List;

import org.ihtsdo.mojo.mojo.compare.CompareOperator;
import org.ihtsdo.mojo.mojo.compare.Match;

public class XOrMore implements CompareOperator {

    public int x = 2;

    public boolean compare(List<Match> matches) {
        HashSet<Integer> uniqueset = new HashSet<Integer>();
        for (Match m : matches) {
            uniqueset.add(m.getPath1().getPath().getConceptNid());
            uniqueset.add(m.getPath2().getPath().getConceptNid());
        }

        return uniqueset.size() >= x;
    }

}
