package org.dwfa.ace.task.prop;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.lucene.index.TermFreqVector;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/prop", type = BeanType.TASK_BEAN) })
public class SetPropertyToTermEntry extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private String termPropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();
    private TermEntry termEntry = new TermEntry(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids());

    private boolean convertToConcept = true;
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(termPropName);
        out.writeObject(termEntry);
        out.writeObject(convertToConcept);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion > dataVersion) {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
        termPropName = (String) in.readObject();
        termEntry = (TermEntry) in.readObject();
        if (objDataVersion >= 2) {
            convertToConcept = (Boolean) in.readObject();
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            if (convertToConcept) {
                I_TermFactory tf = LocalVersionedTerminology.get();
                I_GetConceptData concept = tf.getConcept(termEntry.getIds());
                process.setProperty(termPropName, concept);
            } else {
                process.setProperty(termPropName, termEntry);
            }
            
            return Condition.CONTINUE;
            
        } catch (Exception e) {
            throw new TaskFailedException(e);
        } 
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public String getTermPropName() {
        return termPropName;
    }

    public void setTermPropName(String profilePropName) {
        this.termPropName = profilePropName;
    }

    public TermEntry getTermEntry() {
        return termEntry;
    }

    public void setTermEntry(TermEntry rootEntry) {
        this.termEntry = rootEntry;
    }

    public boolean isConvertToConcept() {
        return convertToConcept;
    }

    public void setConvertToConcept(boolean convertToConcept) {
        this.convertToConcept = convertToConcept;
    }
}
