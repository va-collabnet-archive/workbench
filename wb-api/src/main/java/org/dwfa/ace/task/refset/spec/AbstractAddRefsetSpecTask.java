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
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;

import org.ihtsdo.tk.api.PathBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.lang.reflect.InvocationTargetException;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public abstract class AbstractAddRefsetSpecTask extends AbstractTask {
   private static final int dataVersion = 4;

   /**
    *
    */
   private static final long serialVersionUID            = 1L;
   protected static Integer  trueNid                     = Integer.MIN_VALUE;
   protected static Integer  refsetOrGroupingNid         = Integer.MIN_VALUE;
   protected static Integer  refsetAndGroupingNid        = Integer.MIN_VALUE;
   protected static Integer  falseNid                    = Integer.MIN_VALUE;
   protected static Integer  conceptContainsRelGrouping  = Integer.MIN_VALUE;
   protected static Integer  conceptContainsDescGrouping = Integer.MIN_VALUE;

   //~--- fields --------------------------------------------------------------

   private String activeConceptPropName     = ProcessAttachmentKeys.ACTIVE_CONCEPT_UUID.getAttachmentKey();
   private String activeDescriptionPropName =
      ProcessAttachmentKeys.ACTIVE_DESCRIPTION_UUID.getAttachmentKey();
   private Boolean                  clauseIsTrue    = true;
   protected transient Exception    ex              = null;
   private transient Condition      returnCondition = Condition.CONTINUE;
   protected I_DescriptionVersioned c3Description;

   //~--- methods -------------------------------------------------------------

   public final void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

      // Nothing to do
   }

   protected void doRun(final I_EncodeBusinessProcess process, final I_Work worker) {
      try {

         // TODO pass in frame configuration
         I_ConfigAceFrame configFrame = Terms.get().getActiveAceFrameConfig();

         if (configFrame.getEditingPathSet().isEmpty()) {
            String msg = "Unable to add to spec. Editing path set is empty.";

            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), msg);

            throw new TaskFailedException(msg);
         }

         UUID descriptionUuid = (UUID) process.readAttachement(activeDescriptionPropName);

         if (descriptionUuid != null) {
            c3Description = Terms.get().getDescription(Terms.get().uuidToNative(descriptionUuid));
         }

         I_GetConceptData refsetSpec = configFrame.getRefsetSpecInSpecEditor();

         if (refsetSpec != null) {
            JTree                  specTree     = configFrame.getTreeInSpecEditor();
            int                    refsetId     = refsetSpec.getConceptNid();
            int                    componentId  = refsetId;
            TreePath               selection    = specTree.getSelectionPath();
            DefaultMutableTreeNode selectedNode = null;
            boolean                canAdd       = true;

            if (selection != null) {
               canAdd       = false;
               selectedNode = (DefaultMutableTreeNode) selection.getLastPathComponent();

               I_ExtendByRef selectedSpec = (I_ExtendByRef) selectedNode.getUserObject();

               componentId = selectedSpec.getMemberId();

               if (selectedSpec.getTypeNid()
                       == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid()) {
                  canAdd = true;
               }
            }

            if (canAdd) {
               I_TermFactory     tf           = Terms.get();
               I_HelpRefsets     refsetHelper = tf.getRefsetHelper(configFrame);
               RefsetPropertyMap propMap      = getRefsetPropertyMap(tf, configFrame);
               I_ExtendByRef     ext          = refsetHelper.getOrCreateRefsetExtension(refsetId,
                                                   componentId, propMap.getMemberType(), propMap,
                                                   UUID.randomUUID());
               List<? extends I_ExtendByRefVersion> tuples = ext.getTuples(configFrame.getAllowedStatus(),
                                                                configFrame.getViewPositionSetReadOnly(),
                                                                configFrame.getPrecedence(),
                                                                configFrame.getConflictResolutionStrategy());

               if (tuples.size() > 0) {
                  boolean added = false;

                  for (I_ExtendByRefVersion t : tuples) {
                     if (t.getTime() == Long.MAX_VALUE) {
                        tf.addUncommitted(ext);
                        added = true;
                     }
                  }

                  if (!added) {
                     String msg = "Unable to add to spec. Equivalent specification already exists.";

                     JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), msg);
                  }
               } else {
                  List<? extends I_ExtendByRefVersion> lastTuple =
                     ext.getTuples(null, configFrame.getViewPositionSetReadOnly(),
                                   configFrame.getPrecedence(), configFrame.getConflictResolutionStrategy());

                  for (I_ExtendByRefVersion t : lastTuple) {
                     for (PathBI p : configFrame.getEditingPathSet()) {
                        I_ExtendByRefPart analog =
                           (I_ExtendByRefPart) t.makeAnalog(
                               ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
                               Long.MAX_VALUE,
                               configFrame.getEditCoordinate().getAuthorNid(),
                               configFrame.getEditCoordinate().getModuleNid(),
                               p.getConceptNid());

                        ext.addVersion(analog);
                        tf.addUncommitted(ext);
                     }
                  }
               }

               RefsetSpec refsetSpecHelper = new RefsetSpec(refsetSpec, configFrame);

               refsetSpecHelper.setLastEditTime(System.currentTimeMillis());
               configFrame.fireRefsetSpecChanged(ext);
               configFrame.refreshRefsetTab();
            } else {
               String msg = "Unable to add to spec. Selected parent must be a branching spec.";

               JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), msg);

               throw new TaskFailedException(msg);
            }
         } else {
            String msg = "Unable to add to spec. No valid refset in the refset spec panel.";

            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), msg);
         }

         returnCondition = Condition.CONTINUE;
      } catch (Exception e) {
         ex = e;
      }
   }

   public final Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker)
           throws TaskFailedException {
      try {
         if ((trueNid == Integer.MIN_VALUE) || (falseNid == Integer.MIN_VALUE)
                 || (refsetOrGroupingNid == Integer.MIN_VALUE)
                 || (conceptContainsRelGrouping == Integer.MIN_VALUE)
                 || (conceptContainsDescGrouping == Integer.MIN_VALUE)
                 || (refsetAndGroupingNid == Integer.MIN_VALUE)) {
            trueNid                    =
               RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.localize().getNid();
            falseNid                   =
               RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_FALSE.localize().getNid();
            refsetOrGroupingNid        = RefsetAuxiliary.Concept.REFSET_OR_GROUPING.localize().getNid();
            refsetAndGroupingNid       = RefsetAuxiliary.Concept.REFSET_AND_GROUPING.localize().getNid();
            conceptContainsRelGrouping =
               RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING.localize().getNid();
            conceptContainsDescGrouping =
               RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING.localize().getNid();
         }

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
      } catch (IOException e) {
         throw new TaskFailedException(e);
      } catch (TerminologyException e) {
         throw new TaskFailedException(e);
      }

      if (ex != null) {
         throw new TaskFailedException(ex);
      }

      return returnCondition;
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion <= dataVersion) {
         if (objDataVersion < 2) {
            clauseIsTrue = true;
         } else {
            clauseIsTrue = in.readBoolean();
         }

         if (objDataVersion == 3) {
            activeConceptPropName = (String) in.readObject();
         } else {
            activeConceptPropName = ProcessAttachmentKeys.ACTIVE_CONCEPT_UUID.getAttachmentKey();
         }

         if (objDataVersion >= 4) {
            activeDescriptionPropName = (String) in.readObject();
         } else {
            activeDescriptionPropName = ProcessAttachmentKeys.ACTIVE_DESCRIPTION_UUID.getAttachmentKey();
         }
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeBoolean(clauseIsTrue);
      out.writeObject(activeConceptPropName);
      out.writeObject(activeDescriptionPropName);
   }

   //~--- get methods ---------------------------------------------------------

   public String getActiveConceptPropName() {
      return activeConceptPropName;
   }

   public String getActiveDescriptionPropName() {
      return activeDescriptionPropName;
   }

   public Boolean getClauseIsTrue() {
      if (clauseIsTrue == null) {
         clauseIsTrue = true;
      }

      return clauseIsTrue;
   }

   public Collection<Condition> getConditions() {
      return AbstractTask.CONTINUE_CONDITION;
   }

   public int[] getDataContainerIds() {
      return new int[] {};
   }

   protected abstract int getRefsetPartTypeId() throws IOException, TerminologyException;

   protected abstract RefsetPropertyMap getRefsetPropertyMap(I_TermFactory tf, I_ConfigAceFrame configFrame)
           throws IOException, TerminologyException;

   //~--- set methods ---------------------------------------------------------

   public void setActiveConceptPropName(String activeConceptPropName) {
      this.activeConceptPropName = activeConceptPropName;
   }

   public void setActiveDescriptionPropName(String activeDescriptionPropName) {
      this.activeDescriptionPropName = activeDescriptionPropName;
   }

   public void setClauseIsTrue(Boolean clauseIsTrue) {
      this.clauseIsTrue = clauseIsTrue;
   }
}
