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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.arena.contradiction.ContradictionEditorFrame;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;

public class AcceptActionListener implements ActionListener {

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
                CommitTask ct = new CommitTask();
                ct.execute();
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
                int adjudicationRecRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.ADJUDICATION_RECORD.getUids());
                Set<UUID> authorTimeHashSet = new HashSet<UUID>();
                adjudicationRecRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.ADJUDICATION_RECORD.getUids());
                for (Integer sap : c.getAllSapNids()) {
                    if (sap > Ts.get().getReadOnlyMaxSap()) {
                        ConceptChronicleBI authorConcept = Ts.get().getConceptForNid(Ts.get().getAuthorNidForSapNid(sap));
                        long time = Ts.get().getTimeForSapNid(sap);
                        String stringToHash = authorConcept.getPrimUuid().toString()
                                + Long.toString(time);
                        UUID type5Uuid = Type5UuidFactory.get(Type5UuidFactory.AUTHOR_TIME_ID,
                                stringToHash);
                        authorTimeHashSet.add(type5Uuid);
                    }
                }
                if (!authorTimeHashSet.isEmpty()) {
                    byte[][] arrayOfAuthorTime = new byte[authorTimeHashSet.size()][];
                    UUID[] atUuidArray = authorTimeHashSet.toArray(new UUID[authorTimeHashSet.size()]);
                    for (int i = 0; i < arrayOfAuthorTime.length; i++) {
                        arrayOfAuthorTime[i] = Type5UuidFactory.getRawBytes(atUuidArray[i]);
                    }

                    RefexCAB annotBp = new RefexCAB(TK_REFSET_TYPE.ARRAY_BYTEARRAY,
                            c.getConceptNid(),
                            adjudicationRecRefsetNid);
                    annotBp.put(RefexProperty.ARRAY_BYTEARRAY, arrayOfAuthorTime);

                    I_ConfigAceFrame config = settings.getView().getConfig();
                    ViewCoordinate vc = config.getViewCoordinate();
                    EditCoordinate ec = config.getEditCoordinate();
                    TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(ec, vc);
                    RefexChronicleBI<?> newAdjudicationRecord = builder.constructIfNotCurrent(annotBp);

                    c.addAnnotation(newAdjudicationRecord);
                    Ts.get().addUncommitted(c);
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
                I_GetConceptData c = settings.getConcept();
                if (isCommitted) {

                    if (settings.isForAdjudication()) {
                        I_ConfigAceFrame config = settings.getView().getConfig();
                        ViewCoordinate vc = config.getViewCoordinate();
                        EditCoordinate ec = config.getEditCoordinate();
                        TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(ec, vc);
                        int conflictRefsetNid = Ts.get().getNidForUuids(UUID.fromString("767e3525-ebd4-43f9-9640-952e31589e47"));
                        ConceptChronicleBI conflictRefset = Ts.get().getConceptForNid(conflictRefsetNid);
                        RefexVersionBI member = conflictRefset.getCurrentRefsetMemberForComponent(vc, c.getNid());
                        RefexCAB memberBp = member.makeBlueprint(vc);
                        memberBp.setRetired();
                        builder.constructIfNotCurrent(memberBp);
                        Ts.get().addUncommitted(conflictRefset);
                        Ts.get().commit(conflictRefset);

                        ContradictionEditorFrame cef = (ContradictionEditorFrame) settings.getView().getRootPane().getParent();
                        TerminologyList list = cef.getBatchConceptList();
                        TerminologyListModel model = (TerminologyListModel) list.getModel();
                        List<Integer> nidsInList = model.getNidsInList();
                        for (int i = 0; i < nidsInList.size(); i++) {
                            int nid = nidsInList.get(i);
                            if (nid == c.getNid()) {
                                model.removeElement(i);
                                break;
                            }
                        }
                    }
                } 
            } catch (IOException ex) {
                Logger.getLogger(AcceptActionListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidCAB ex) {
                Logger.getLogger(AcceptActionListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ContradictionException ex) {
                Logger.getLogger(AcceptActionListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
