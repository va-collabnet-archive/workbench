/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.dwfa.ace.task.refset.spec;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.refset.refresh.PanelRefsetVersion;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.PositionBI;

//~--- JDK imports ------------------------------------------------------------

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class DiffVnIsRefsetSpec extends AbstractAddRefsetSpecTask {
   private static final int dataVersion = 1;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   PositionBI position;

   //~--- methods -------------------------------------------------------------

   @Override
   protected void doRun(I_EncodeBusinessProcess process, I_Work worker) {
      try {
         new VersionDialog();
      } catch (Exception e) {
         ex = e;
         AceLog.getAppLog().alertAndLogException(ex);

         return;
      }

      super.doRun(process, worker);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion == dataVersion) {

         //
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   protected int getRefsetPartTypeId() throws IOException, TerminologyException {
      return RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION.localize().getNid();
   }

   @Override
   protected RefsetPropertyMap getRefsetPropertyMap(I_TermFactory tf, I_ConfigAceFrame configFrame)
           throws IOException, TerminologyException {
      RefsetPropertyMap refsetMap = new RefsetPropertyMap(REFSET_TYPES.CID_CID_STR);

      if (getClauseIsTrue()) {
         refsetMap.put(REFSET_PROPERTY.CID_ONE, trueNid);
      } else {
         refsetMap.put(REFSET_PROPERTY.CID_ONE, falseNid);
      }

      refsetMap.put(REFSET_PROPERTY.CID_TWO, getStructuralQueryTokenId());
      refsetMap.put(REFSET_PROPERTY.STATUS, configFrame.getDefaultStatus().getNid());
      refsetMap.put(REFSET_PROPERTY.STRING_VALUE,
                    position.toString() + "(" + position.getPath().getUUIDs().get(0) + " "
                    + position.getVersion() + ")");

      return refsetMap;
   }

   protected abstract int getStructuralQueryTokenId() throws IOException, TerminologyException;

   //~--- inner classes -------------------------------------------------------

   private class VersionDialog extends JDialog implements ActionListener {
      PanelRefsetVersion p;

      //~--- constructors -----------------------------------------------------

      public VersionDialog() throws Exception {
         super(new JFrame(), "Select versions", true);

         I_ConfigAceFrame configFrame = Terms.get().getActiveAceFrameConfig();

         p = new PanelRefsetVersion(configFrame);
         p.setSelectedRefsetSpecLabel("");
         this.getContentPane().add(p);

         JPanel  buttonPane = new JPanel();
         JButton button     = new JButton("OK");

         buttonPane.add(button);
         button.addActionListener(this);
         getContentPane().add(buttonPane, BorderLayout.SOUTH);
         this.pack();
         this.setVisible(true);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void actionPerformed(ActionEvent e) {
         position = p.getPositionSet().iterator().next();
         setVisible(false);
         dispose();
      }
   }
}
