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