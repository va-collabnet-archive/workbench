package org.dwfa.ace.task.refset.refresh;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.commit.TestForCreateNewRefsetPermission;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Shows the Refresh Refset Spec panel in the WF Details panel.
 * 
 * @author Perry Reid
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/refset/spec/wf", type = BeanType.TASK_BEAN) })
public class SetWFDSheetToRefreshRefsetSpecParamsPanelTask extends AbstractTask {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private transient Exception ex = null;
    private I_TermFactory termFactory;
    private I_ConfigAceFrame config;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields...
            } else {
                // Set version 1 default values...
            }
            // Initialize transient properties...
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }  
    
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do
    }

    public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker) throws TaskFailedException {

        try {
            ex = null;
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
        }
        if (ex != null) {
            throw new TaskFailedException(ex);
        }
        return Condition.CONTINUE;
    }

    private void doRun(final I_EncodeBusinessProcess process, final I_Work worker) {

        try {
            termFactory = LocalVersionedTerminology.get();
            config = (I_ConfigAceFrame) termFactory.getActiveAceFrameConfig();

            Set<I_GetConceptData> refsets = getValidRefsets();

            JPanel wfSheet = config.getWorkflowDetailsSheet();
            Component[] components = wfSheet.getComponents();
            for (int i = 0; i < components.length; i++) {
                wfSheet.remove(components[i]);
            }

	        int width = 475;
	        int height = 625;
	        wfSheet.setSize(width, height);
	        wfSheet.setLayout(new BorderLayout());
            wfSheet.add(new PanelRefreshRefsetSpecParams(refsets, wfSheet), BorderLayout.NORTH);

            wfSheet.repaint();
        } catch (Exception e) {
            ex = e;
        }
    }

    private Set<I_GetConceptData> getValidRefsets() throws Exception {
        Set<I_GetConceptData> refsets = new HashSet<I_GetConceptData>();

        I_GetConceptData owner = config.getDbConfig().getUserConcept();
        TestForCreateNewRefsetPermission permissionTest = new TestForCreateNewRefsetPermission();
        Set<I_GetConceptData> permissibleRefsetParents = new HashSet<I_GetConceptData>();
        permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromIndividualUserPermissions(owner));
        permissibleRefsetParents.addAll(permissionTest.getValidRefsetsFromRolePermissions(owner));

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

        for (I_GetConceptData parent : permissibleRefsetParents) {
            Set<I_GetConceptData> children = parent.getDestRelOrigins(null, allowedTypes, null, true, true);
            for (I_GetConceptData child : children) {
                if (isRefset(child)) {
                    refsets.add(child);
                }
            }
        }

        return refsets;
    }

    private boolean isRefset(I_GetConceptData child) throws TerminologyException, IOException {
        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(RefsetAuxiliary.Concept.SPECIFIES_REFSET.localize().getNid());

        List<I_RelTuple> relationships = child.getDestRelTuples(null, allowedTypes, null, true, true);
        if (relationships.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

}
