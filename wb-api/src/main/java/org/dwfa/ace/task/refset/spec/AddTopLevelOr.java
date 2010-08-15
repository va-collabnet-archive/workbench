package org.dwfa.ace.task.refset.spec;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

@BeanList(specs = { @Spec(directory = "tasks/refset/spec", type = BeanType.TASK_BEAN) })
public class AddTopLevelOr extends AbstractTask {

    private String refsetPropName = ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey();
    public String getRefsetPropName() {
        return refsetPropName;
    }

    public void setRefsetPropName(String refsetPropName) {
        this.refsetPropName = refsetPropName;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 5;

    private Boolean clauseIsTrue = true;
    private transient Exception ex = null;
    private transient Condition returnCondition = Condition.CONTINUE;
    protected I_DescriptionVersioned c3Description;
    
    private transient I_ConfigAceFrame configFrame;
    private transient I_GetConceptData refsetConcept;
    private transient I_GetConceptData refsetInEditor;

    protected static Integer trueNid = Integer.MIN_VALUE;
    protected static Integer falseNid = Integer.MIN_VALUE;
    protected static Integer refsetOrGroupingNid = Integer.MIN_VALUE;
    protected static Integer conceptContainsRelGrouping = Integer.MIN_VALUE;
    protected static Integer conceptContainsDescGrouping = Integer.MIN_VALUE;
    protected static Integer refsetAndGroupingNid = Integer.MIN_VALUE;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(refsetPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion < 5) {
                if (objDataVersion >= 2) {
                    in.readBoolean();
                } 
                if (objDataVersion == 3) {
                    in.readObject();
                } 
                if (objDataVersion >= 4) {
                    in.readObject();
                } 
            }
            if (objDataVersion < 5) {
                refsetPropName = ProcessAttachmentKeys.REFSET_UUID.getAttachmentKey();
            } else {
                refsetPropName = (String) in.readObject();
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public final void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public final Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker)
            throws TaskFailedException {

        try {
            if (trueNid == Integer.MIN_VALUE || falseNid == Integer.MIN_VALUE
                || refsetOrGroupingNid == Integer.MIN_VALUE || conceptContainsRelGrouping == Integer.MIN_VALUE
                || conceptContainsDescGrouping == Integer.MIN_VALUE || refsetAndGroupingNid == Integer.MIN_VALUE) {
                trueNid = RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.localize().getNid();
                falseNid = RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_FALSE.localize().getNid();
                refsetOrGroupingNid = RefsetAuxiliary.Concept.REFSET_OR_GROUPING.localize().getNid();
                refsetAndGroupingNid = RefsetAuxiliary.Concept.REFSET_AND_GROUPING.localize().getNid();
                conceptContainsRelGrouping = RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING.localize().getNid();
                conceptContainsDescGrouping =
                        RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING.localize().getNid();
            }

            ex = null;

            //TODO pass in frame configuration
            configFrame = Terms.get().getActiveAceFrameConfig();
             if (configFrame.getEditingPathSet().size() == 0) {
                 String msg = "Unable to add spec. Editing path set is empty.";
                 JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), msg);
                 throw new TaskFailedException(msg);
             }

             UUID refsetUuid = (UUID) process.getProperty(refsetPropName);

             refsetConcept = Terms.get().getConcept(refsetUuid);
             configFrame.setRefsetInSpecEditor(refsetConcept);
             Thread.sleep(200);
             
             refsetInEditor = configFrame.getRefsetInSpecEditor();
             
             while (refsetInEditor != refsetConcept) {
                 Thread.sleep(500);
                 SwingUtilities.invokeAndWait(new Runnable() {
                     public void run() {
                         try {
                            refsetInEditor = configFrame.getRefsetSpecInSpecEditor();
                        } catch (IOException e) {
                            ex = e;
                        } catch (TerminologyException e) {
                            ex = e;
                        }
                     }
                 });
                 if (ex != null) {
                     throw new TaskFailedException(ex);
                 }
             }

            
            if (SwingUtilities.isEventDispatchThread()) {
                doRun(process, worker);
            } else {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        doRun(process, worker);
                    }
                });
            }
        } catch (InterruptedException e) {
            throw new TaskFailedException(e);
        } catch (InvocationTargetException e) {
            throw new TaskFailedException(e);
        } catch (IllegalArgumentException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IntrospectionException e) {
            throw new TaskFailedException(e);
        } catch (IllegalAccessException e) {
            throw new TaskFailedException(e);
        }
        if (ex != null) {
            throw new TaskFailedException(ex);
        }
        return returnCondition;
    }

    private void doRun(final I_EncodeBusinessProcess process, final I_Work worker) {
        try {
            JTree specTree = configFrame.getTreeInSpecEditor();
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) specTree.getModel().getRoot();
            if (rootNode.getChildCount() == 0) {
                ;
                int specRefsetId = Terms.get().getRefsetHelper(configFrame).getSpecificationRefsetForRefset(refsetConcept, configFrame).iterator().next().getConceptNid();
                int componentId = specRefsetId;
                I_TermFactory tf = Terms.get();
                I_HelpRefsets refsetHelper = tf.getRefsetHelper(configFrame);
                RefsetPropertyMap propMap = getRefsetPropertyMap(tf, configFrame);
                I_ExtendByRef ext = refsetHelper.getOrCreateRefsetExtension(specRefsetId, componentId,
                    propMap.getMemberType(), propMap, UUID.randomUUID());
                tf.addUncommitted(ext);
                configFrame.fireRefsetSpecChanged(ext);
            }
            returnCondition = Condition.CONTINUE;
        } catch (Exception e) {
            ex = e;
        }
    }

    protected int getRefsetPartTypeId() throws IOException, TerminologyException {
        int typeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid();
        return typeId;
    }

    protected RefsetPropertyMap getRefsetPropertyMap(I_TermFactory tf, I_ConfigAceFrame configFrame) throws IOException, TerminologyException {
        RefsetPropertyMap refsetMap = new RefsetPropertyMap(REFSET_TYPES.CID_CID);
        if (getClauseIsTrue()) {
            refsetMap.put(REFSET_PROPERTY.CID_ONE, trueNid);
        } else {
            refsetMap.put(REFSET_PROPERTY.CID_ONE, falseNid);
        }
        refsetMap.put(REFSET_PROPERTY.CID_TWO, refsetOrGroupingNid);
        refsetMap.put(REFSET_PROPERTY.STATUS, configFrame.getDefaultStatus().getNid());
        return refsetMap;
    }
    
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

    public Boolean getClauseIsTrue() {
        if (clauseIsTrue == null) {
            clauseIsTrue = true;
        }
        return clauseIsTrue;
    }

    public void setClauseIsTrue(Boolean clauseIsTrue) {
        this.clauseIsTrue = clauseIsTrue;
    }
}
