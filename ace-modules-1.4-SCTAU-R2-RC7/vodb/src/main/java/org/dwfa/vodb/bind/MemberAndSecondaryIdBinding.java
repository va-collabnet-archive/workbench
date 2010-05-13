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
/**
 * 
 */
package org.dwfa.vodb.bind;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class MemberAndSecondaryIdBinding extends TupleBinding {

    public MemberAndSecondaryId entryToObject(TupleInput ti) {
        return new MemberAndSecondaryId(ti.readInt(), ti.readInt());
    }

    public void objectToEntry(Object obj, TupleOutput to) {
        MemberAndSecondaryId id = (MemberAndSecondaryId) obj;
        to.writeInt(id.getSecondaryId());
        to.writeInt(id.getMemberId());
    }

}
