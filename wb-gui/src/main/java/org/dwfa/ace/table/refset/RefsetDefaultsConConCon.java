/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.I_RefsetsDefaultsConConCon;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntList;

/**
 *
 * @author kec
 */
public class RefsetDefaultsConConCon extends RefsetDefaults implements I_RefsetsDefaultsConConCon {

    private I_IntList conceptPopupIds = new IntList();

    private I_GetConceptData defaultForConceptRefset;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(defaultForConceptRefset.getUids());
        IntList.writeIntList(out, conceptPopupIds);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            defaultForConceptRefset = readConcept(in);
            conceptPopupIds = IntList.readIntListIgnoreMapErrors(in);
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public RefsetDefaultsConConCon() throws TerminologyException, IOException {
        super();
        defaultForConceptRefset = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids());
        conceptPopupIds.add(defaultForConceptRefset.getConceptNid());
    }

    public I_GetConceptData getDefaultForConceptRefset() {
        return defaultForConceptRefset;
    }

    public void setDefaultForConceptRefset(I_GetConceptData defaultForConceptRefset) {
        Object oldValue = this.defaultForConceptRefset;
        this.defaultForConceptRefset = defaultForConceptRefset;
        pcs.firePropertyChange("defaultForConceptRefset", oldValue, defaultForConceptRefset);
    }

    public I_IntList getConceptPopupIds() {
        return conceptPopupIds;
    }

    @Override
    public I_GetConceptData getDefaultForCnid1() {
        return defaultForConceptRefset;
    }

    @Override
    public I_GetConceptData getDefaultForCnid2() {
        return defaultForConceptRefset;
    }

    @Override
    public I_GetConceptData getDefaultForCnid3() {
        return defaultForConceptRefset;
    }

}
