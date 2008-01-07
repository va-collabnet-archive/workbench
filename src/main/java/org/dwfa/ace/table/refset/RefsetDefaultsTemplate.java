package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.refset.I_RefsetDefaultsTemplate;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;

public class RefsetDefaultsTemplate extends RefsetDefaultsTemplateForRel implements I_RefsetDefaultsTemplate {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
    private I_GetConceptData attribute;
    private I_IntList attributePopupIds = new IntList();


    private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(attribute.getUids());
       IntList.writeIntList(out, attributePopupIds);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
           attribute = readConcept(in);
           attributePopupIds = IntList.readIntListIgnoreMapErrors(in);
       } else {
          throw new IOException("Can't handle dataversion: " + objDataVersion);
       }
    }
    public RefsetDefaultsTemplate() throws TerminologyException, IOException {
        super();
        attribute = ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_CODE_VALUE_TYPE.getUids());
        attributePopupIds.add(attribute.getConceptId());
        attributePopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_NUMBER_VALUE_TYPE.getUids()).getConceptId());
        attributePopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.TEMPLATE_DATE_VALUE_TYPE.getUids()).getConceptId());
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplate#getAttribute()
     */
    public I_GetConceptData getAttribute() {
        return attribute;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplate#setAttribute(org.dwfa.ace.api.I_GetConceptData)
     */
    public void setAttribute(I_GetConceptData attribute) {
        this.attribute = attribute;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplate#getAttributePopupIds()
     */
    public I_IntList getAttributePopupIds() {
        return attributePopupIds;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsTemplate#setAttributePopupIds(org.dwfa.ace.api.I_IntList)
     */
    public void setAttributePopupIds(I_IntList attributePopupIds) {
        this.attributePopupIds = attributePopupIds;
    }

 
}
