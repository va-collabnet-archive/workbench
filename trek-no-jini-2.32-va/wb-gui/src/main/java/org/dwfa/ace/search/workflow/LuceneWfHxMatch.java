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
package org.dwfa.ace.search.workflow;

import org.dwfa.ace.search.LuceneMatch;
import org.ihtsdo.lucene.WorkflowLuceneSearchResult;

public class LuceneWfHxMatch extends LuceneMatch {
	WorkflowLuceneSearchResult val;

    public LuceneWfHxMatch(WorkflowLuceneSearchResult b, Float score) {
        super(score);
        this.val = b;
    }

    public WorkflowLuceneSearchResult getDisplayValues() {
        return val;
    }

	@Override
    public int compareTo(LuceneMatch o) {
        if (this.score.equals(o.score) == false) {
            return Float.compare(this.score, o.score);
        }
        if (this.val.equals(((LuceneWfHxMatch)o).val) == false) {
            return this.val.getFsn().compareTo(((LuceneWfHxMatch)o).val.getFsn());
        }
        return this.val.getFsn().compareTo(((LuceneWfHxMatch)o).val.getFsn());
    }

    @Override
    public boolean equals(Object obj) {
        if (LuceneWfHxMatch.class.isAssignableFrom(obj.getClass())) {
            LuceneWfHxMatch another = (LuceneWfHxMatch) obj;
            return val.equals(another.val);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return val.hashCode();
    }

}
