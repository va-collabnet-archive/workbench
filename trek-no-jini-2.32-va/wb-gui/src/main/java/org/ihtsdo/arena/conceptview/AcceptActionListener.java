package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;

//~--- JDK imports ------------------------------------------------------------

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;


import javax.swing.SwingWorker;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.arena.contradiction.ContradictionEditorFrame;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

public class AcceptActionListener implements ActionListener {

    private static int adjudicationRecRefsetNid = Integer.MAX_VALUE;
    private static int readOnlyMaxSap = Integer.MAX_VALUE;
    private static int snorocketNid = Integer.MAX_VALUE;
    ConceptViewSettings settings;
    Boolean isCommitted = false;

    //~--- constructors --------------------------------------------------------
    public AcceptActionListener(ConceptViewSettings settings) {
        this.settings = settings;
    }

    //~--- methods -------------------------------------------------------------
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (settings != null) {
                if (settings.getConfig().getEditingPathSet().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Editing path set is empty.", "Error", JOptionPane.ERROR_MESSAGE);;
                } else {
                    CommitTask ct = new CommitTask();
                    ct.execute();
                }
            }
        } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    //~--- inner classes -------------------------------------------------------
    private class CommitTask extends SwingWorker<Boolean, Object> {

        @Override
        protected Boolean doInBackground() throws Exception {
            if (settings.isForAdjudication()) {
                I_GetConceptData c = settings.getConcept();
                Set<UUID> authorTimeHashSet = new HashSet<UUID>();
                if (adjudicationRecRefsetNid == Integer.MAX_VALUE) {
                    readOnlyMaxSap = Ts.get().getReadOnlyMaxStamp();
                    snorocketNid = Ts.get().getNidForUuids(ArchitectonicAuxiliary.Concept.SNOROCKET.getUids());
                    adjudicationRecRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.ADJUDICATION_RECORD.getUids());
                }
                //get ajudication path
                int[] editPathNids = settings.getConfig().getEditCoordinate().getEditPaths();
                for(int editPathNid : editPathNids){
                    PositionBI projectPosition = Ts.get().newPosition(Ts.get().getPath(editPathNid), System.currentTimeMillis()); //latest on path
                    for (Integer stamp : c.getAllStampNids()) {
                        PositionBI stampPosition = Ts.get().newPosition(Ts.get().getPath(Ts.get().getPathNidForStampNid(stamp)),
                                Ts.get().getTimeForStampNid(stamp));
                        if(stampPosition.isAntecedentOrEqualTo(projectPosition)){
                            if (stamp > readOnlyMaxSap) {
                                if (Ts.get().getAuthorNidForStampNid(stamp) != snorocketNid) {
                                    UUID authorUuid = Ts.get().getUuidPrimordialForNid(Ts.get().getAuthorNidForStampNid(stamp));
                                    long time = Ts.get().getTimeForStampNid(stamp);
                                    String stringToHash = authorUuid.toString() + Long.toString(time);
                                    UUID type5Uuid = Type5UuidFactory.get(Type5UuidFactory.AUTHOR_TIME_ID, stringToHash);
                                    authorTimeHashSet.add(type5Uuid);
                                }
                            }
                        }
                    }
                    if (!authorTimeHashSet.isEmpty()) {
                        byte[][] arrayOfAuthorTime = new byte[authorTimeHashSet.size()][];
                        UUID[] atUuidArray = authorTimeHashSet.toArray(new UUID[authorTimeHashSet.size()]);
                        for (int i = 0; i < arrayOfAuthorTime.length; i++) {
                            arrayOfAuthorTime[i] = Type5UuidFactory.getRawBytes(atUuidArray[i]);
                        }

                        RefexCAB annotBp = new RefexCAB(TK_REFEX_TYPE.ARRAY_BYTEARRAY,
                                c.getConceptNid(),
                                adjudicationRecRefsetNid);
                        annotBp.put(RefexProperty.ARRAY_BYTEARRAY, arrayOfAuthorTime);
                        annotBp.setMemberUuid(UUID.randomUUID());
                        I_ConfigAceFrame config = settings.getView().getConfig();
                        ViewCoordinate vc = config.getViewCoordinate();
                        EditCoordinate ec = config.getEditCoordinate();
                        TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(ec, vc);
                        RefexChronicleBI<?> newAdjudicationRecord = builder.constructIfNotCurrent(annotBp);

                        c.addAnnotation(newAdjudicationRecord);
                        Ts.get().addUncommitted(c);
                    }
            }
                
                isCommitted = c.commit(settings.getConfig().getDbConfig().getUserChangesChangeSetPolicy().convert(),
                        settings.getConfig().getDbConfig().getChangeSetWriterThreading().convert(),
                        settings.isForAdjudication());
                return isCommitted;
            }
            return false;
        }

        @Override
        protected void done() {
            try {
                get();
                I_GetConceptData c = settings.getConcept();
                if (isCommitted) {

                    if (settings.isForAdjudication()) {
                        I_ConfigAceFrame config = settings.getView().getConfig();
                        ViewCoordinate vc = config.getViewCoordinate();
                        EditCoordinate ec = config.getEditCoordinate();
                        TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(ec, vc);
                        int conflictRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.CONFLICT_RECORD.getPrimoridalUid());
                        ConceptChronicleBI conflictRefset = Ts.get().getConceptForNid(conflictRefsetNid);
                        RefexVersionBI member = conflictRefset.getRefsetMemberActiveForComponent(vc, c.getNid());
                        if (member != null) {
                            RefexCAB memberBp = member.makeBlueprint(vc);
                            memberBp.setRetired();
                            builder.constructIfNotCurrent(memberBp);
                            Ts.get().addUncommittedNoChecks(conflictRefset);
                            Ts.get().commit(conflictRefset);
                        }

                        ContradictionEditorFrame cef = (ContradictionEditorFrame) settings.getView().getRootPane().getParent();
                        TerminologyList list = cef.getBatchConceptList();
                        TerminologyListModel model = (TerminologyListModel) list.getModel();
                        List<Integer> nidsInList = model.getNidsInList();
                        int index = 0;
                        for (Integer nid : nidsInList) {
                            if (nid == c.getNid()) {
                                model.removeElement(index);
                                break;
                            }
                            index++;
                        }
                    }
                }
            } catch (InterruptedException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            } catch (ExecutionException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            } catch (TerminologyException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            } catch (IOException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            } catch (InvalidCAB ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            } catch (ContradictionException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }
}
