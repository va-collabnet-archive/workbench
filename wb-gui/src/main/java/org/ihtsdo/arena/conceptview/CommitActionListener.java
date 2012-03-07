package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;

//~--- JDK imports ------------------------------------------------------------

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

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

         return c.commit(settings.getConfig().getDbConfig().getUserChangesChangeSetPolicy().convert(),
                         settings.getConfig().getDbConfig().getChangeSetWriterThreading().convert(),
                         settings.isForAdjudication());
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
