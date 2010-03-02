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
package org.dwfa.ace.search;

import org.dwfa.ace.api.I_DescriptionVersioned;

public class LuceneMatch implements Comparable<LuceneMatch> {
	I_DescriptionVersioned desc;
    Float score;

    public LuceneMatch(I_DescriptionVersioned desc, Float score) {
        super();
        this.desc = desc;
        this.score = score;
    }

    public I_DescriptionVersioned getDesc() {
        return desc;
    }

    public Float getScore() {
        return score;
    }

    public int compareTo(LuceneMatch o) {
        if (this.score.equals(o.score) == false) {
            return Float.compare(this.score, o.score);
        }
        if (this.desc.getFirstTuple().getText().equals(o.desc.getFirstTuple().getText()) == false) {
            return this.desc.getFirstTuple().getText().compareTo(o.desc.getFirstTuple().getText());
        }
        return this.desc.toString().compareTo(o.desc.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (LuceneMatch.class.isAssignableFrom(obj.getClass())) {
            LuceneMatch another = (LuceneMatch) obj;
            return desc.getDescId() == another.desc.getDescId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return desc.getDescId();
    }

}
