package org.ihtsdo.concept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.db.bdb.Bdb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IsLeafBinder extends TupleBinding<Boolean> {
    
    I_ConfigAceFrame aceConfig;

    
    public IsLeafBinder(I_ConfigAceFrame aceConfig) {
        super();
        this.aceConfig = aceConfig;
    }

    @Override
    public Boolean entryToObject(TupleInput input) {
        I_IntSet destRelTypes = aceConfig.getDestRelTypes();
        int size = input.readInt();
        for (int i = 0; i < size; i = i + 2) {
            int relNid = input.readInt();
            int typeId = input.readInt();
            if (destRelTypes.contains(typeId)) {
                try {
                    Relationship r = Bdb.getConceptForComponent(relNid).getSourceRel(relNid);
                    if (r != null) {
                        List<I_RelTuple> currentVersions = new ArrayList<I_RelTuple>();
                        r.addTuples(aceConfig.getAllowedStatus(), destRelTypes, aceConfig
                                .getViewPositionSetReadOnly(), currentVersions, 
                                aceConfig.getPrecedence(), aceConfig.getConflictResolutionStrategy());
                        if (currentVersions.size() > 0) {
                            return false;
                        }
                    }
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        }
        return true;
    }

    @Override
    public void objectToEntry(Boolean object, TupleOutput output) {
        throw new UnsupportedOperationException();
    }

}
