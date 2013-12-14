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



package org.dwfa.ace.table;

//~--- non-JDK imports --------------------------------------------------------

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.KeyEvent;
import java.awt.image.FilteredImageSource;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.table.TableModel;
import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.classifier.SnoRelReport;
import org.dwfa.ace.dnd.AceTransferAction;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.dnd.DescriptionTransferable;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.ConceptAttributeTableModel.CONCEPT_FIELD;
import org.dwfa.ace.table.ConceptAttributeTableModel.StringWithConceptTuple;
import org.dwfa.ace.table.DescriptionTableModel.DESC_FIELD;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.ace.table.IdTableModel.ID_FIELD;
import org.dwfa.ace.table.IdTableModel.StringWithIdTuple;
import org.dwfa.ace.table.ImageTableModel.IMAGE_FIELD;
import org.dwfa.ace.table.ImageTableModel.ImageWithImageTuple;
import org.dwfa.ace.table.ImageTableModel.StringWithImageTuple;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData;
import org.dwfa.ace.table.refset.RefsetMemberTableModel.REFSET_FIELDS;
import org.dwfa.ace.table.refset.StringWithExtTuple;
import org.dwfa.ace.task.classify.SnoRel;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.ace.table.WorkflowHistoryTableModel.WORKFLOW_FIELD;
import org.ihtsdo.ace.table.WorkflowHistoryTableModel.WorkflowStringWithConceptTuple;
import sun.awt.dnd.SunDragSourceContextPeer;

public class JTableWithDragImage extends JTable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- constructors --------------------------------------------------------

   public JTableWithDragImage(TableModel dm) {
      super(dm);
      DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY,
              new DragGestureListenerWithImage(new TermLabelDragSourceListener()));

      InputMap imap = this.getInputMap();

      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               TransferHandler.getCutAction().getValue(Action.NAME));
      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               TransferHandler.getCopyAction().getValue(Action.NAME));
      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
               TransferHandler.getPasteAction().getValue(Action.NAME));
      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK),
               TransferHandler.getCutAction().getValue(Action.NAME));
      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK),
               TransferHandler.getCopyAction().getValue(Action.NAME));
      imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK),
               TransferHandler.getPasteAction().getValue(Action.NAME));

      ActionMap map = this.getActionMap();

      map.put("cut", new AceTransferAction("cut"));
      map.put("copy", new AceTransferAction("copy"));
      map.put("paste", new AceTransferAction("paste"));
   }

   //~--- inner classes -------------------------------------------------------

   private class DragGestureListenerWithImage implements DragGestureListener {
      DragSourceListener dsl;

      //~--- constructors -----------------------------------------------------

      public DragGestureListenerWithImage(DragSourceListener dsl) {
         super();
         this.dsl = dsl;
      }

      //~--- methods ----------------------------------------------------------

      private Transferable TransferableFromSWCT(Object obj, int column)
              throws TerminologyException, IOException {
         StringWithConceptTuple swct  = (StringWithConceptTuple) obj;
         CONCEPT_FIELD          field = (CONCEPT_FIELD) getColumnModel().getColumn(column).getIdentifier();

         switch (field) {
         case CON_ID :
            return new ConceptTransferable(Terms.get().getConcept(swct.getTuple().getNid()));

         case STATUS :
            return new ConceptTransferable(Terms.get().getConcept(swct.getTuple().getStatusNid()));

         case DEFINED :
            return new StringSelection(swct.getCellText());

         case VERSION :
            return new StringSelection(ThinVersionHelper.format(swct.getTuple().getVersion()));

         case PATH :
            return new ConceptTransferable(Terms.get().getConcept(swct.getTuple().getPathNid()));
             
         case AUTHOR:
            return new ConceptTransferable(Terms.get().getConcept(swct.getTuple().getAuthorNid()));

         default :
            throw new UnsupportedOperationException("Can not transfer field: " + field);
         }
      }

      private Transferable TransferableFromSWDT(Object obj, int column)
              throws TerminologyException, IOException {
         StringWithDescTuple swdt  = (StringWithDescTuple) obj;
         DESC_FIELD          field = (DESC_FIELD) getColumnModel().getColumn(column).getIdentifier();

         switch (field) {
         case DESC_NID :
            throw new UnsupportedOperationException();

         case CON_NID :
            return new ConceptTransferable(Terms.get().getConcept(swdt.getTuple().getConceptNid()));

         case TEXT :
            return new DescriptionTransferable(swdt.tuple);

         case LANG :
            return new StringSelection(swdt.tuple.getLang());

         case CASE_FIXED :
            return new StringSelection(swdt.getCellText());

         case STATUS :
            return new ConceptTransferable(Terms.get().getConcept(swdt.getTuple().getStatusId()));

         case TYPE :
            return new ConceptTransferable(Terms.get().getConcept(swdt.getTuple().getTypeId()));

         case VERSION :
            return new StringSelection(ThinVersionHelper.format(swdt.getTuple().getVersion()));

         case PATH :
            return new ConceptTransferable(Terms.get().getConcept(swdt.getTuple().getPathId()));

         case AUTHOR :
            return new ConceptTransferable(Terms.get().getConcept(swdt.getTuple().getAuthorNid()));

         default :
            throw new UnsupportedOperationException("Can not transfer field: " + field);
         }
      }

      private Transferable TransferableFromSWIdT(Object obj, int column)
              throws TerminologyException, IOException {
         StringWithIdTuple seidt = (StringWithIdTuple) obj;
         ID_FIELD          field = (ID_FIELD) getColumnModel().getColumn(column).getIdentifier();

         switch (field) {
         case LOCAL_ID :
            throw new UnsupportedOperationException();

         case EXT_ID :
            return new StringSelection(seidt.getTuple().getDenotation().toString());

         case STATUS :
            return new ConceptTransferable(Terms.get().getConcept(seidt.getTuple().getStatusId()));

         case VERSION :
            return new StringSelection(ThinVersionHelper.format(seidt.getTuple().getVersion()));

         case PATH :
            return new ConceptTransferable(Terms.get().getConcept(seidt.getTuple().getPathId()));
            
         case AUTHOR:
            return new ConceptTransferable(Terms.get().getConcept(seidt.getTuple().getAuthorNid()));

         default :
            throw new UnsupportedOperationException("Can not transfer field: " + field);
         }
      }

      private Transferable TransferableFromSWRT(Object obj, int column)
              throws TerminologyException, IOException {
         StringWithRelTuple swrt  = (StringWithRelTuple) obj;
         REL_FIELD          field = (REL_FIELD) getColumnModel().getColumn(column).getIdentifier();

         switch (field) {
         case REL_ID :
            throw new UnsupportedOperationException();

         case SOURCE_ID :
            return new ConceptTransferable(Terms.get().getConcept(swrt.getTuple().getC1Id()));

         case REL_TYPE :
            return new ConceptTransferable(Terms.get().getConcept(swrt.getTuple().getTypeId()));

         case DEST_ID :
            return new ConceptTransferable(Terms.get().getConcept(swrt.getTuple().getC2Id()));

         case GROUP :
            return new StringSelection(Integer.toString(swrt.tuple.getGroup()));

         case REFINABILITY :
            return new ConceptTransferable(Terms.get().getConcept(swrt.getTuple().getRefinabilityId()));

         case CHARACTERISTIC :
            return new ConceptTransferable(Terms.get().getConcept(swrt.getTuple().getCharacteristicId()));

         case STATUS :
            return new ConceptTransferable(Terms.get().getConcept(swrt.getTuple().getStatusNid()));

         case VERSION :
            return new StringSelection(ThinVersionHelper.format(swrt.getTuple().getVersion()));

         case PATH :
            return new ConceptTransferable(Terms.get().getConcept(swrt.getTuple().getPathNid()));

         case AUTHOR:
            return new ConceptTransferable(Terms.get().getConcept(swrt.getTuple().getAuthorNid()));
            
         default :
            throw new UnsupportedOperationException("Can not transfer field: " + field);
         }
      }

      @Override
      public void dragGestureRecognized(DragGestureEvent dge) {
         int column = columnAtPoint(dge.getDragOrigin());
         int row    = rowAtPoint(dge.getDragOrigin());

         if (row > -1) {
            Object obj         = getValueAt(row, column);
            Image  dragImage   = getDragImage(obj);
            Point  imageOffset = new Point(-10, -(dragImage.getHeight(JTableWithDragImage.this) + 1));

            try {
               Transferable t = getTransferable(obj, column);

               AceLog.getAppLog().info("Created transferable: " + t);

               if (t != null) {
                  dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, t, dsl);
               }
            } catch (InvalidDnDOperationException e) {
               AceLog.getAppLog().log(Level.WARNING, e.getMessage(), e);
               AceLog.getAppLog().log(Level.INFO, "Resetting SunDragSourceContextPeer [3]");
               SunDragSourceContextPeer.setDragDropInProgress(false);
            } catch (Exception ex) {
               AceLog.getAppLog().alertAndLogException(ex);
            }
         }
      }

      private Transferable transferableFromIWImgT(Object obj) throws IOException {
         ImageWithImageTuple iwit = (ImageWithImageTuple) obj;

         return new StringSelection("<img src='ace:" + Terms.get().nativeToUuid(iwit.tuple.getNid()) + "$"
                                    + Terms.get().nativeToUuid(iwit.tuple.getConceptNid()) + "'>");
      }

      private Transferable transferableFromSWExtT(Object obj, int column)
              throws TerminologyException, IOException {
         StringWithExtTuple swextt           = (StringWithExtTuple) obj;
         Object             columnIdentifier = getColumnModel().getColumn(column).getIdentifier();

         if (ReflexiveRefsetFieldData.class.isAssignableFrom(columnIdentifier.getClass())) {
            ReflexiveRefsetFieldData fieldData = (ReflexiveRefsetFieldData) columnIdentifier;

            switch (fieldData.getType()) {
            case COMPONENT_IDENTIFIER :
            case CONCEPT_IDENTIFIER :
               Object component = Terms.get().getComponent(swextt.getId());

               if (component == null) {
                  return new StringSelection("null component for: " + obj + " version: " + swextt.getTuple());
               }

               if (I_GetConceptData.class.isAssignableFrom(component.getClass())) {
                  return new ConceptTransferable(Terms.get().getConcept(swextt.getId()));
               }

               if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
                  return new DescriptionTransferable(Terms.get().getDescription(swextt.getId()));
               }

               throw new UnsupportedOperationException("Component: " + component);

            case STRING :
               return new StringSelection(swextt.getCellText());

            case TIME :
               return new StringSelection(swextt.getCellText());

            default :
               throw new UnsupportedOperationException("Can't handle fieldDataType: " + fieldData.getType());
            }
         } else if (REFSET_FIELDS.class.isAssignableFrom(columnIdentifier.getClass())) {
            REFSET_FIELDS field = (REFSET_FIELDS) columnIdentifier;

            switch (field) {

            // All extensions
            case REFSET_ID :
               return new ConceptTransferable(Terms.get().getConcept(swextt.getTuple().getRefsetId()));

            case MEMBER_ID :
               throw new UnsupportedOperationException();

            case COMPONENT_ID :
               throw new UnsupportedOperationException();

            case STATUS :
               return new ConceptTransferable(Terms.get().getConcept(swextt.getTuple().getStatusId()));

            case VERSION :
               return new StringSelection(swextt.getCellText());

            case PATH :
               return new ConceptTransferable(Terms.get().getConcept(swextt.getTuple().getPathId()));

            case BOOLEAN_VALUE :
               return new StringSelection(swextt.getCellText());

            case CONCEPT_ID :
               return new ConceptTransferable(
                   Terms.get().getConcept(
                      ((I_ExtendByRefPartCid) swextt.getTuple().getMutablePart()).getC1id()));

            case INTEGER_VALUE :
               return new StringSelection(swextt.getCellText());

            case STRING_VALUE :
               return new StringSelection(swextt.getCellText());
                

            default :
               throw new UnsupportedOperationException("Can't handle field: " + field);
            }
         }

         throw new UnsupportedOperationException("Can't handle columnIdentifier: " + columnIdentifier
                 + " of class: " + columnIdentifier.getClass());
      }

      private Transferable transferableFromSWImgT(Object obj, int column)
              throws IOException, TerminologyException {
         StringWithImageTuple swit  = (StringWithImageTuple) obj;
         IMAGE_FIELD          field = (IMAGE_FIELD) getColumnModel().getColumn(column).getIdentifier();

         switch (field) {
         case IMAGE_ID :
            return new StringSelection("<img src='ace:" + Terms.get().nativeToUuid(swit.getTuple().getNid())
                                       + "$" + Terms.get().nativeToUuid(swit.getTuple().getConceptNid())
                                       + "'>");

         case CON_ID :
            return new ConceptTransferable(Terms.get().getConcept(swit.getTuple().getConceptNid()));

         case DESC :
            return new StringSelection(swit.getTuple().getTextDescription());

         case IMAGE :
            return new StringSelection("<img src='ace:" + Terms.get().nativeToUuid(swit.getTuple().getNid())
                                       + "$" + Terms.get().nativeToUuid(swit.getTuple().getConceptNid())
                                       + "'>");

         case FORMAT :
            return new StringSelection(swit.getTuple().getFormat());

         case STATUS :
            return new ConceptTransferable(Terms.get().getConcept(swit.getTuple().getStatusId()));

         case TYPE :
            return new ConceptTransferable(Terms.get().getConcept(swit.getTuple().getTypeId()));

         case VERSION :
            return new StringSelection(ThinVersionHelper.format(swit.getTuple().getVersion()));

         case PATH :
            return new ConceptTransferable(Terms.get().getConcept(swit.getTuple().getPathId()));

         default :
            throw new UnsupportedOperationException("Can not transfer field: " + field);
         }
      }

      private Transferable transferableFromWFExtT(Object obj, int column)
              throws TerminologyException, IOException {
         WorkflowStringWithConceptTuple wfct  = (WorkflowStringWithConceptTuple) obj;
         WORKFLOW_FIELD                 field =
            (WORKFLOW_FIELD) getColumnModel().getColumn(column).getIdentifier();

         switch (field) {
         case FSN :
            return new ConceptTransferable(Terms.get().getConcept(wfct.getTuple().getConceptNid()));

//       case ACTION:
//           return new StringSelection(wfct.getCellText());
         case STATE :
            return new ConceptTransferable(Terms.get().getConcept(wfct.getTuple().getConceptNid()));

         case EDITOR :
            return new ConceptTransferable(Terms.get().getConcept(wfct.getTuple().getConceptNid()));

//       case PATH:
//           return new ConceptTransferable(Terms.get().getConcept(wfct.getTuple().getConceptNid()));
         case TIMESTAMP :
            return new ConceptTransferable(Terms.get().getConcept(wfct.getTuple().getConceptNid()));

         default :
            throw new UnsupportedOperationException("Can not transfer field: " + field);
         }
      }

      //~--- get methods ------------------------------------------------------

      @SuppressWarnings("unchecked")
      public Image getDragImage(Object obj) {
         JLabel dragLabel;

         if (I_CellTextWithTuple.class.isAssignableFrom(obj.getClass())) {
            I_CellTextWithTuple ctwt = (I_CellTextWithTuple) obj;

            dragLabel = TermLabelMaker.makeLabel(ctwt.getCellText());
         } else if (ImageWithImageTuple.class.isAssignableFrom(obj.getClass())) {
            ImageWithImageTuple iwit = (ImageWithImageTuple) obj;

            dragLabel = new JLabel(iwit.getImage());

            Dimension size = new Dimension(iwit.getImage().getIconWidth(), iwit.getImage().getIconHeight());

            dragLabel.setPreferredSize(size);
            dragLabel.setMaximumSize(size);
            dragLabel.setMinimumSize(size);
            dragLabel.setSize(size);
         } else {
            dragLabel = TermLabelMaker.makeLabel(obj.toString());
         }

         dragLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

         Image dragImage = createImage(dragLabel.getWidth(), dragLabel.getHeight());

         dragLabel.setVisible(true);

         Graphics og = dragImage.getGraphics();

         og.setClip(dragLabel.getBounds());
         dragLabel.paint(og);
         og.dispose();

         FilteredImageSource fis = new FilteredImageSource(dragImage.getSource(),
                                      TermLabelMaker.getTransparentFilter());

         dragImage = Toolkit.getDefaultToolkit().createImage(fis);

         return dragImage;
      }

      private Transferable getTransferable(Object obj, int column) throws IOException, TerminologyException {
         if (I_CellTextWithTuple.class.isAssignableFrom(obj.getClass())) {
            if (StringWithConceptTuple.class.isAssignableFrom(obj.getClass())) {
               return TransferableFromSWCT(obj, column);
            } else if (StringWithDescTuple.class.isAssignableFrom(obj.getClass())) {
               return TransferableFromSWDT(obj, column);
            } else if (StringWithRelTuple.class.isAssignableFrom(obj.getClass())) {
               return TransferableFromSWRT(obj, column);
            } else if (StringWithIdTuple.class.isAssignableFrom(obj.getClass())) {
               return TransferableFromSWIdT(obj, column);
            } else if (StringWithImageTuple.class.isAssignableFrom(obj.getClass())) {
               return transferableFromSWImgT(obj, column);
            } else if (StringWithExtTuple.class.isAssignableFrom(obj.getClass())) {
               return transferableFromSWExtT(obj, column);
            } else if (WorkflowStringWithConceptTuple.class.isAssignableFrom(obj.getClass())) {
               return transferableFromWFExtT(obj, column);
            }
         } else if (ImageWithImageTuple.class.isAssignableFrom(obj.getClass())) {
            return transferableFromIWImgT(obj);
         } else if (obj instanceof I_GetConceptData) {
            I_GetConceptData cc = (I_GetConceptData) obj;

            return new ConceptTransferable(cc);
         } else if (obj instanceof SnoRel) {
             int cnid = ((SnoRel) obj).c1Id;
             I_GetConceptData cc = Terms.get().getConcept(cnid);
             return new ConceptTransferable(cc);
         } else if (obj instanceof SnoRelReport) {
             SnoRelReport srr = (SnoRelReport) obj;
             int cnid = srr.snoRel.c1Id;
             if (column == 2) {
                cnid = srr.snoRel.typeId;
             } else if (column == 3) {
                cnid = srr.snoRel.c2Id;
             }
             I_GetConceptData cc = Terms.get().getConcept(cnid);
             return new ConceptTransferable(cc);
         } else {
            return new StringSelection(obj.toString());
         }

         AceLog.getAppLog().info("Can't create transferable for: " + obj.getClass());

         return null;
      }
   }


   private class TermLabelDragSourceListener implements DragSourceListener {
      @Override
      public void dragDropEnd(DragSourceDropEvent dsde) {

         // TODO Auto-generated method stub
      }

      @Override
      public void dragEnter(DragSourceDragEvent dsde) {

         // TODO Auto-generated method stub
      }

      @Override
      public void dragExit(DragSourceEvent dse) {

         // TODO Auto-generated method stub
      }

      @Override
      public void dragOver(DragSourceDragEvent dsde) {

         // TODO Auto-generated method stub
      }

      @Override
      public void dropActionChanged(DragSourceDragEvent dsde) {

         // TODO Auto-generated method stub
      }
   }
}
