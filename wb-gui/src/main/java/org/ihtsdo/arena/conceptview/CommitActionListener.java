package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;

//~--- JDK imports ------------------------------------------------------------

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;

import javax.swing.SwingWorker;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.arena.contradiction.ContradictionEditorFrame;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

public class CommitActionListener implements ActionListener {
   ConceptViewSettings settings;

   //~--- constructors --------------------------------------------------------

   public CommitActionListener(ConceptViewSettings settings) {
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
         I_GetConceptData c = settings.getConcept();
         boolean okToCommit = false;
         int response = 100; //some value that is not "yes" or "no" value
         if (RulesLibrary.rulesDisabled) {
              response = JOptionPane.showConfirmDialog(null, "QA is disabled. Are you sure you want to commit?",
                      "QA Disabled!", JOptionPane.YES_NO_OPTION);
          }
          if(!RulesLibrary.rulesDisabled || response == JOptionPane.YES_OPTION){
              okToCommit = true;
          }
         
         if(settings.isForAdjudication() && okToCommit){
             I_ConfigAceFrame config = settings.getView().getConfig();
             ViewCoordinate vc = config.getViewCoordinate();
             EditCoordinate ec = config.getEditCoordinate();
                TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(ec, vc);
             int conflictRefsetNid = Ts.get().getNidForUuids(RefsetAuxiliary.Concept.CONFLICT_RECORD.getPrimoridalUid());
             ConceptChronicleBI conflictRefset = Ts.get().getConceptForNid(conflictRefsetNid);
             RefexVersionBI member = conflictRefset.getRefsetMemberActiveForComponent(vc, c.getConceptNid());
             if(member != null){
                RefexCAB memberBp = member.makeBlueprint(vc);
                memberBp.setRetired();
                builder.construct(memberBp);
                Ts.get().addUncommittedNoChecks(conflictRefset);
                Ts.get().commit(conflictRefset);
             }
             
             ContradictionEditorFrame cef = (ContradictionEditorFrame) settings.getView().getRootPane().getParent();
             TerminologyList list = cef.getBatchConceptList();
             TerminologyListModel model = (TerminologyListModel)list.getModel();
             List<Integer> nidsInList = model.getNidsInList();
             for(int i = 0; i < nidsInList.size(); i++){
                 int nid = nidsInList.get(i);
                 if(nid == c.getNid()){
                     model.removeElement(i);
                     break;
                 }
             }
         }else if (okToCommit){
             ACE.suspendDatacheckDisplay();
             boolean commit = c.commit(settings.getConfig().getDbConfig().getUserChangesChangeSetPolicy().convert(),
                     settings.getConfig().getDbConfig().getChangeSetWriterThreading().convert(),
                     settings.isForAdjudication());
             Ts.get().waitTillDatachecksFinished();
             ACE.resumeDatacheckDisplay();
             return commit;
         }
         
         return false;
      }

      @Override
      protected void done() {
         try {
            if (get()) {
               settings.getView().getCvRenderer().updateCancelAndCommit();
            }
         } catch (InterruptedException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         } catch (ExecutionException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }
   }
}
