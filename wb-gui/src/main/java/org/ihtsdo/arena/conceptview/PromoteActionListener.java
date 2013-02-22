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
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

public class PromoteActionListener implements ActionListener {

    private static int adjudicationRecRefsetNid = Integer.MAX_VALUE;
    private static int readOnlyMaxSap = Integer.MAX_VALUE;
    private static int snorocketNid = Integer.MAX_VALUE;
    ConceptViewSettings settings;
    Boolean isCommitted = false;

    //~--- constructors --------------------------------------------------------
    public PromoteActionListener(ConceptViewSettings settings) {
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
            if (settings.isForPromotion()) {
                ViewCoordinate vc = settings.getConfig().getViewCoordinate();
                ConceptVersionBI c = settings.getConcept().getVersion(vc.getViewCoordinateWithAllStatusValues());
                int pathNid = settings.getConfig().getEditingPathSet().iterator().next().getConceptNid();
                TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(settings.getConfig().getEditCoordinate(), vc);
                ConceptVersionBI promoteRefex = Ts.get().getConceptVersion(vc, pathNid);
                boolean changed = false;
                if (promoteRefex.hasRefsetMemberActiveForComponent(c.getNid())) {
                    RefexNidVersionBI<?> member = (RefexNidVersionBI<?>) promoteRefex.getRefsetMemberActiveForComponent(vc, c.getNid());
                    RefexCAB memberBp = member.makeBlueprint(vc);
                    if (member.getNid1() == TermAux.UNREVIEWED.getLenient().getConceptNid()) {
                        memberBp.put(RefexProperty.CNID1, TermAux.PROMOTE.getLenient().getConceptNid());
                    } else if (member.getNid1() == TermAux.PROMOTE.getLenient().getConceptNid()) {
                        memberBp.put(RefexProperty.CNID1, TermAux.UNREVIEWED.getLenient().getConceptNid());
                    }
                    memberBp.setMemberUuid(member.getPrimUuid());
                    builder.construct(memberBp);
                    changed = true;
                }
                for (DescriptionVersionBI desc : c.getDescriptionsActive()) {
                    if (promoteRefex.hasRefsetMemberActiveForComponent(desc.getNid())) {
                        RefexNidVersionBI<?> member = (RefexNidVersionBI<?>) promoteRefex.getRefsetMemberActiveForComponent(vc, desc.getNid());
                        RefexCAB memberBp = member.makeBlueprint(vc);
                        if (member.getNid1() == TermAux.UNREVIEWED.getLenient().getConceptNid()) {
                            memberBp.put(RefexProperty.CNID1, TermAux.PROMOTE.getLenient().getConceptNid());
                        } else if (member.getNid1() == TermAux.PROMOTE.getLenient().getConceptNid()) {
                            memberBp.put(RefexProperty.CNID1, TermAux.UNREVIEWED.getLenient().getConceptNid());
                        }
                        memberBp.setMemberUuid(member.getPrimUuid());
                        builder.construct(memberBp);
                        changed = true;
                    }
                }
                for (RelationshipVersionBI rel : c.getRelationshipsOutgoingActive()) {
                    if (promoteRefex.hasRefsetMemberActiveForComponent(rel.getNid())) {
                        RefexNidVersionBI<?> member = (RefexNidVersionBI<?>) promoteRefex.getRefsetMemberActiveForComponent(vc, rel.getNid());
                        RefexCAB memberBp = member.makeBlueprint(vc);
                        if (member.getNid1() == TermAux.UNREVIEWED.getLenient().getConceptNid()) {
                            memberBp.put(RefexProperty.CNID1, TermAux.PROMOTE.getLenient().getConceptNid());
                        } else if (member.getNid1() == TermAux.PROMOTE.getLenient().getConceptNid()) {
                            memberBp.put(RefexProperty.CNID1, TermAux.UNREVIEWED.getLenient().getConceptNid());
                        }
                        memberBp.setMemberUuid(member.getPrimUuid());
                        builder.construct(memberBp);
                        changed = true;
                    }
                }
                if (changed) {
                    Ts.get().addUncommitted(Ts.get().getConceptForNid(pathNid));
                    Ts.get().commit(Ts.get().getConceptForNid(pathNid));
                }
            }
            return false;
        }

        @Override
        protected void done() {
            try {
                get();
//TODO: don't think there's anything to do here
            } catch (InterruptedException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            } catch (ExecutionException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }
}
