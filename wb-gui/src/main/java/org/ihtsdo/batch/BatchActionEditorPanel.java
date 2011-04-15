package org.ihtsdo.batch;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_boolean.RefexBooleanVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_int.RefexIntVersionBI;
import org.ihtsdo.tk.api.refex.type_long.RefexLongVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.util.swing.GuiUtil;

/**
 *
 * @author marc
 */
public final class BatchActionEditorPanel extends javax.swing.JPanel {

    private final ACE ace;
    private final TerminologyStoreDI ts;
    private final TerminologyList batchConceptList;
    private List<BatchActionTaskBase> batchActionEditTaskList;
    private JPanel batchActionTaskViewPanel;
    private JTextPane resultsTextArea;
    private List<ComponentVersionBI> existingParents;
    private List<ComponentVersionBI> existingRefsets;
    private List<ComponentVersionBI> existingRoles;
    private Map<Integer, Class> existingRefsetTypes;

    public List<ComponentVersionBI> getExistingParents() {
        return existingParents;
    }

    public List<ComponentVersionBI> getExistingRefsets() {
        return existingRefsets;
    }

    public Class getExistingRefsetType(int nid) {
        return existingRefsetTypes.get(nid);
    }

    public List<ComponentVersionBI> getExistingRoles() {
        return existingRoles;
    }

    public void updateExistingLists(ListModel termList) {
        existingParents = new ArrayList<ComponentVersionBI>();
        existingRefsets = new ArrayList<ComponentVersionBI>();
        existingRoles = new ArrayList<ComponentVersionBI>();
        existingRefsetTypes = new HashMap<Integer, Class>();

        LinkedHashSet<Integer> setParents = new LinkedHashSet<Integer>();
        LinkedHashSet<Integer> setRefsets = new LinkedHashSet<Integer>();
        LinkedHashSet<Integer> setRoles = new LinkedHashSet<Integer>();
        ViewCoordinate vc = ace.getAceFrameConfig().getViewCoordinate();

        try {
            for (int i = 0; i < termList.getSize(); i++) {
                I_GetConceptData cb = (I_GetConceptData) termList.getElementAt(i);

                // EXISTING PARENTS
                for (RelationshipChronicleBI rel : cb.getRelsOutgoing()) {
                    setParents.add(rel.getDestinationNid());
                }

                // EXISTING ROLES
                for (RelationshipChronicleBI rel : cb.getRelsIncoming()) {
                    RelationshipVersionBI rv = rel.getVersion(vc);
                    if (rv != null) {
                        setRoles.add(rv.getTypeNid());
                    }
                }

                // :!!!: EXISTING REFSETS
                Collection<? extends RefexVersionBI<?>> cr = cb.getCurrentRefexes(vc);
                for (RefexVersionBI<?> rvbi : cr) {
                    int refexNid = rvbi.getCollectionNid();
                    existingRefsetTypes.put(refexNid, RefexStrVersionBI.class);
                    System.out.println("!!! updateExistingLists CURRENT_REFEX UserString // " + rvbi.toUserString());
                    if (RefexStrVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                        RefexStrVersionBI r = (RefexStrVersionBI) rvbi;
                        System.out.println("!!! updateExistingLists CURRENT_REFEX getStr1: " + r.getStr1());
                    } else if (RefexBooleanVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                        RefexBooleanVersionBI r = (RefexBooleanVersionBI) rvbi;
                        System.out.println("!!! updateExistingLists CURRENT_REFEX getBoolean1: " + r.getBoolean1());

                        // rvbi.getRefexEditSpec() throws exception on internal index = -1
                        // RefexBooleanAnalogBI ra = (RefexBooleanAnalogBI) r.makeAnalog(r.getStatusNid(), r.getAuthorNid(), r.getPathNid(), Long.MAX_VALUE);
                        // ra.setBoolean1(true);
                    } else if (RefexCnidVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                        RefexCnidVersionBI r = (RefexCnidVersionBI) rvbi;
                        System.out.println("!!! updateExistingLists CURRENT_REFEX getCnid1: " + r.getCnid1());
                    } else if (RefexIntVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                        RefexIntVersionBI r = (RefexIntVersionBI) rvbi;
                        System.out.println("!!! updateExistingLists CURRENT_REFEX getInt1: " + r.getInt1());
                    } else if (RefexLongVersionBI.class.isAssignableFrom(rvbi.getClass())) {
                        RefexLongVersionBI r = (RefexLongVersionBI) rvbi;
                        System.out.println("!!! updateExistingLists CURRENT_REFEX getLong1: " + r.getLong1());
                    }
                    setRefsets.add(refexNid);
                }
            }
            // CONVERT SET TO LIST
            for (Integer nid : setParents) {
                ComponentVersionBI cvbi = ts.getComponentVersion(vc, nid);
                if (cvbi != null) {
                    existingParents.add(cvbi);
                }
            }

            for (Integer nid : setRefsets) {
                ComponentVersionBI cvbi = ts.getComponentVersion(vc, nid);
                if (cvbi != null) {
                    existingRefsets.add(cvbi);
                }
            }

            for (Integer nid : setRoles) {
                ComponentVersionBI cvbi = ts.getComponentVersion(vc, nid);
                if (cvbi != null) {
                    existingRoles.add(cvbi);
                }
            }

            // UPDATE TASK DETAILS
            for (BatchActionTaskBase baet : batchActionEditTaskList) {
                baet.updateExisting(existingParents, existingRefsets, existingRoles);
            }

        } catch (ContraditionException ex) {
            Logger.getLogger(BatchActionEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BatchActionEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    class ExistingListDataListener implements ListDataListener {

        BatchActionEditorPanel baep;

        public ExistingListDataListener(BatchActionEditorPanel baep) {
            this.baep = baep;
        }

        @Override
        public void intervalAdded(ListDataEvent lde) {
            baep.updateExistingLists((TerminologyListModel) lde.getSource());
        }

        @Override
        public void intervalRemoved(ListDataEvent lde) {
            baep.updateExistingLists((TerminologyListModel) lde.getSource());
        }

        @Override
        public void contentsChanged(ListDataEvent lde) {
            baep.updateExistingLists((TerminologyListModel) lde.getSource());
        }
    }

    /** Creates new form BatchActionEditorPanel */
    public BatchActionEditorPanel(ACE ace, TerminologyList list, JTextPane results) {
        this.ace = ace;
        this.ts = Ts.get();
        this.batchConceptList = list;
        this.batchConceptList.getModel().addListDataListener(new ExistingListDataListener(this));
        this.batchActionEditTaskList = new ArrayList<BatchActionTaskBase>();
        this.batchActionTaskViewPanel = new JPanel();
        this.resultsTextArea = results;

        // initComponents is autogenerated
        initComponents();

        existingParents = new ArrayList<ComponentVersionBI>();
        existingRefsets = new ArrayList<ComponentVersionBI>();
        existingRoles = new ArrayList<ComponentVersionBI>();
        updateExistingLists(batchConceptList.getModel());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnExecuteAllTasks = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        btnClearAll = new javax.swing.JButton();
        jScrollBatchActionTasks = new javax.swing.JScrollPane();

        btnExecuteAllTasks.setText("Execute");
        btnExecuteAllTasks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionExecuteAllTasks(evt);
            }
        });

        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add2.png"))); // NOI18N
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionAddAnotherTask(evt);
            }
        });

        btnClearAll.setText("Clear All");
        btnClearAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionClearAll(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(btnExecuteAllTasks)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 340, Short.MAX_VALUE)
                .addComponent(btnAdd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnClearAll)
                .addContainerGap())
            .addComponent(jScrollBatchActionTasks, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnExecuteAllTasks)
                    .addComponent(btnClearAll)
                    .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollBatchActionTasks, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // actionExecuteAllTask ... dispatch engine approach
    private void actionExecuteAllTasks(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionExecuteAllTasks
        try {
            // SETUP CONCEPT LIST
            List<ConceptChronicleBI> concepts = new ArrayList<ConceptChronicleBI>();
            ListModel termList = batchConceptList.getModel();
            for (int i = 0; i < termList.getSize(); i++) {
                I_GetConceptData cb = (I_GetConceptData) termList.getElementAt(i);
                concepts.add(ts.getConcept(cb.getConceptNid()));
            }

            // SETUP TASK LIST
            ViewCoordinate vc = ace.aceFrameConfig.getViewCoordinate();
            EditCoordinate ec = ace.aceFrameConfig.getEditCoordinate();

            BatchActionTask.setup(ec, vc);
            BatchActionEventReporter.reset();

            List<BatchActionTask> tasks = new ArrayList<BatchActionTask>();
            for (BatchActionTaskBase taskBase : batchActionEditTaskList) {
                BatchActionTask tmpTask = taskBase.getTask(ec, vc);
                if (tmpTask != null) {
                    tasks.add(tmpTask);
                }
            }

            BatchActionProcessor bap = new BatchActionProcessor(concepts, tasks, ec, vc);
            ts.iterateConceptDataInParallel(bap);

            System.out.println("\r\n!!! BATCH ACTION TASK REPORT\r\n" + BatchActionEventReporter.createReportTSV());

            // resultsTextArea.setText(BatchActionEventReporter.createReportTSV());
            resultsTextArea.setText(BatchActionEventReporter.createReportHTML());
        } catch (Exception ex) {
            Logger.getLogger(BatchActionEditorPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_actionExecuteAllTasks

    private void actionAddAnotherTask(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionAddAnotherTask
        actionAddAnotherTask();
    }//GEN-LAST:event_actionAddAnotherTask

    public void actionAddAnotherTask() {
        // CREATE NEW TASK
        BatchActionTaskBase batb = new BatchActionTaskBase(this);
        batb.setTaskParentUI(this);
        batb.updateExisting(existingParents, existingRefsets, existingRoles);
        batb.setEnabled(true);
        batb.setVisible(true);
        batb.invalidate();
        batchActionEditTaskList.add(batb);

        // FILL SCROLL PANE
        batchActionTaskViewPanel.removeAll();
        batchActionTaskViewPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTH;
        gc.fill = GridBagConstraints.BOTH;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 0.5;
        gc.weighty = 0;
        gc.ipady = 0;
        for (BatchActionTaskBase b : batchActionEditTaskList) {
            batchActionTaskViewPanel.add(b, gc);
            gc.gridy++;
        }
        gc.weighty = 0.5;
        batchActionTaskViewPanel.add(new JLabel(""), gc);
        jScrollBatchActionTasks.setViewportView(batchActionTaskViewPanel);

        jScrollBatchActionTasks.invalidate();
        jScrollBatchActionTasks.validate();
        jScrollBatchActionTasks.doLayout();
        this.invalidate();
        this.validate();
        this.doLayout();
    }

    public void actionDeleteTask(BatchActionTaskBase task) {
        if (batchActionEditTaskList.remove(task)) {
            // REFILL SCROLL PANE
            batchActionTaskViewPanel.removeAll();
            batchActionTaskViewPanel.setLayout(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.NORTHWEST;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.gridx = 0;
            gc.weightx = 0.5;
            gc.gridy = 0;
            for (BatchActionTaskBase b : batchActionEditTaskList) {
                batchActionTaskViewPanel.add(b, gc);
                gc.gridy++;
            }
            jScrollBatchActionTasks.setViewportView(batchActionTaskViewPanel);

            jScrollBatchActionTasks.invalidate();
            jScrollBatchActionTasks.validate();
            jScrollBatchActionTasks.doLayout();
            this.invalidate();
            this.validate();
            this.doLayout();
        }
    }

    private void actionClearAll(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionClearAll
        batchActionEditTaskList.clear();
        batchActionTaskViewPanel.removeAll();
        batchActionTaskViewPanel.invalidate();
        jScrollBatchActionTasks.invalidate();
        jScrollBatchActionTasks.doLayout();
        this.invalidate();
        this.validate();
        GuiUtil.tickle(this);
    }//GEN-LAST:event_actionClearAll
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnClearAll;
    private javax.swing.JButton btnExecuteAllTasks;
    private javax.swing.JScrollPane jScrollBatchActionTasks;
    // End of variables declaration//GEN-END:variables
}
