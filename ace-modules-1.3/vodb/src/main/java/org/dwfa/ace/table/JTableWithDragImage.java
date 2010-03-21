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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.KeyEvent;
import java.awt.image.FilteredImageSource;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.table.TableModel;

import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguage;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartLanguageScoped;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartMeasurement;
import org.dwfa.ace.config.AceConfig;
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
import org.dwfa.ace.table.refset.StringWithExtTuple;
import org.dwfa.ace.table.refset.RefsetMemberTableModel.REFSET_FIELDS;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;

import com.sleepycat.je.DatabaseException;

public class JTableWithDragImage extends JTable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private class TermLabelDragSourceListener implements DragSourceListener {

        public void dragDropEnd(DragSourceDropEvent dsde) {
            // TODO Auto-generated method stub
        }

        public void dragEnter(DragSourceDragEvent dsde) {
            // TODO Auto-generated method stub
        }

        public void dragExit(DragSourceEvent dse) {
            // TODO Auto-generated method stub
        }

        public void dragOver(DragSourceDragEvent dsde) {
            // TODO Auto-generated method stub
        }

        public void dropActionChanged(DragSourceDragEvent dsde) {
            // TODO Auto-generated method stub
        }
    }

    private class DragGestureListenerWithImage implements DragGestureListener {

        DragSourceListener dsl;

        public DragGestureListenerWithImage(DragSourceListener dsl) {
            super();
            this.dsl = dsl;
        }

        public void dragGestureRecognized(DragGestureEvent dge) {
            int column = columnAtPoint(dge.getDragOrigin());
            int row = rowAtPoint(dge.getDragOrigin());
            if (row > -1) {
                Object obj = getValueAt(row, column);
                Image dragImage = getDragImage(obj);
                Point imageOffset = new Point(-10, -(dragImage.getHeight(JTableWithDragImage.this) + 1));
                try {
                    Transferable t = getTransferable(obj, column);
                    AceLog.getAppLog().info("Created transferable: " + t);
                    if (t != null) {
                        dge.startDrag(DragSource.DefaultCopyDrop, dragImage, imageOffset, t, dsl);
                    }
                } catch (InvalidDnDOperationException e) {
                    AceLog.getAppLog().info(e.toString());
                } catch (Exception ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        }

        private Transferable getTransferable(Object obj, int column) throws DatabaseException {
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
                }
            } else if (ImageWithImageTuple.class.isAssignableFrom(obj.getClass())) {
                return transferableFromIWImgT(obj);
            } else {
                return new StringSelection(obj.toString());
            }
            AceLog.getAppLog().info("Can't create transferable for: " + obj.getClass());
            return null;
        }

        private Transferable transferableFromSWExtT(Object obj, int column) {
            StringWithExtTuple swextt = (StringWithExtTuple) obj;
            Object columnIdentifier = getColumnModel().getColumn(column).getIdentifier();
            if (ReflexiveRefsetFieldData.class.isAssignableFrom(columnIdentifier.getClass())) {
                ReflexiveRefsetFieldData fieldData = (ReflexiveRefsetFieldData) columnIdentifier;
                switch (fieldData.getType()) {
                case COMPONENT_IDENTIFIER:
                    throw new UnsupportedOperationException();
                case CONCEPT_IDENTIFIER:
                    return new ConceptTransferable(ConceptBean.get(swextt.getId()));
                case STRING:
                    return new StringSelection(swextt.getCellText());
                case VERSION:
                    return new StringSelection(swextt.getCellText());
                default:
                    throw new UnsupportedOperationException("Can't handle fieldDataType: " + fieldData.getType());
                }

            } else if (REFSET_FIELDS.class.isAssignableFrom(columnIdentifier.getClass())) {
                REFSET_FIELDS field = (REFSET_FIELDS) columnIdentifier;
                switch (field) {
                // All extensions
                case REFSET_ID:
                    return new ConceptTransferable(ConceptBean.get(swextt.getTuple().getRefsetId()));
                case MEMBER_ID:
                    throw new UnsupportedOperationException();
                case COMPONENT_ID:
                    throw new UnsupportedOperationException();
                case STATUS:
                    return new ConceptTransferable(ConceptBean.get(swextt.getTuple().getStatusId()));
                case VERSION:
                    return new StringSelection(swextt.getCellText());
                case PATH:
                    return new ConceptTransferable(ConceptBean.get(swextt.getTuple().getPathId()));
                case BOOLEAN_VALUE:
                    return new StringSelection(swextt.getCellText());
                case CONCEPT_ID:
                    return new ConceptTransferable(ConceptBean.get(((I_ThinExtByRefPartConcept) swextt.getTuple()
                        .getPart()).getC1id()));
                case CONCEPT_2_ID:
                    return new ConceptTransferable(ConceptBean.get(((I_ThinExtByRefPartConceptConcept) swextt.getTuple()
                        .getPart()).getC2id()));
                case INTEGER_VALUE:
                    return new StringSelection(swextt.getCellText());
                case CONCEPT_CONCEPT_STRING_VALUE:
                    return new StringSelection(
                        ((I_ThinExtByRefPartConceptConceptString) swextt.getTuple().getPart()).getStringValue().replaceAll("\t", " "));
                case ACCEPTABILITY:
                    return new ConceptTransferable(ConceptBean.get(((I_ThinExtByRefPartLanguage) swextt.getTuple()
                        .getPart()).getAcceptabilityId()));
                case CORRECTNESS:
                    return new ConceptTransferable(ConceptBean.get(((I_ThinExtByRefPartLanguage) swextt.getTuple()
                        .getPart()).getCorrectnessId()));
                case DEGREE_OF_SYNONYMY:
                    return new ConceptTransferable(ConceptBean.get(((I_ThinExtByRefPartLanguage) swextt.getTuple()
                        .getPart()).getDegreeOfSynonymyId()));
                case TAG:
                    return new ConceptTransferable(
                        ConceptBean.get(((I_ThinExtByRefPartLanguageScoped) swextt.getTuple().getPart()).getTagId()));
                case SCOPE:
                    return new ConceptTransferable(
                        ConceptBean.get(((I_ThinExtByRefPartLanguageScoped) swextt.getTuple().getPart()).getScopeId()));
                case PRIORITY:
                    return new StringSelection(swextt.getCellText());
                case MEASUREMENT_VALUE:
                    return new StringSelection(swextt.getCellText());
                case MEASUREMENT_UNITS_ID:
                    return new ConceptTransferable(ConceptBean.get(((I_ThinExtByRefPartMeasurement) swextt.getTuple()
                        .getPart()).getUnitsOfMeasureId()));
                case STRING_VALUE:
                    return new StringSelection(swextt.getCellText());
                default:
                    throw new UnsupportedOperationException("Can't handle field: " + field);
                }
            }
            throw new UnsupportedOperationException("Can't handle columnIdentifier: " + columnIdentifier);

        }

        private Transferable TransferableFromSWIdT(Object obj, int column) {
            StringWithIdTuple seidt = (StringWithIdTuple) obj;

            ID_FIELD field = (ID_FIELD) getColumnModel().getColumn(column).getIdentifier();
            switch (field) {
            case LOCAL_ID:
                throw new UnsupportedOperationException();
            case EXT_ID:
                return new StringSelection(seidt.getTuple().getSourceId().toString());
            case STATUS:
                return new ConceptTransferable(ConceptBean.get(seidt.getTuple().getStatusId()));
            case VERSION:
                return new StringSelection(ThinVersionHelper.format(seidt.getTuple().getVersion()));
            case PATH:
                return new ConceptTransferable(ConceptBean.get(seidt.getTuple().getPathId()));
            default:
                throw new UnsupportedOperationException("Cana't handle field: " + field);
            }
        }

        private Transferable TransferableFromSWRT(Object obj, int column) {
            StringWithRelTuple swrt = (StringWithRelTuple) obj;

            REL_FIELD field = (REL_FIELD) getColumnModel().getColumn(column).getIdentifier();
            switch (field) {
            case REL_ID:
                throw new UnsupportedOperationException();
            case SOURCE_ID:
                return new ConceptTransferable(ConceptBean.get(swrt.getTuple().getC1Id()));
            case REL_TYPE:
                return new ConceptTransferable(ConceptBean.get(swrt.getTuple().getTypeId()));
            case DEST_ID:
                return new ConceptTransferable(ConceptBean.get(swrt.getTuple().getC2Id()));
            case GROUP:
                return new StringSelection(Integer.toString(swrt.tuple.getGroup()));
            case REFINABILITY:
                return new ConceptTransferable(ConceptBean.get(swrt.getTuple().getRefinabilityId()));
            case CHARACTERISTIC:
                return new ConceptTransferable(ConceptBean.get(swrt.getTuple().getCharacteristicId()));
            case STATUS:
                return new ConceptTransferable(ConceptBean.get(swrt.getTuple().getStatusId()));
            case VERSION:
                return new StringSelection(ThinVersionHelper.format(swrt.getTuple().getVersion()));
            case PATH:
                return new ConceptTransferable(ConceptBean.get(swrt.getTuple().getPathId()));
            default:
                throw new UnsupportedOperationException("Cana't handle field: " + field);
            }
        }

        private Transferable TransferableFromSWDT(Object obj, int column) {
            StringWithDescTuple swdt = (StringWithDescTuple) obj;

            DESC_FIELD field = (DESC_FIELD) getColumnModel().getColumn(column).getIdentifier();
            switch (field) {
            case DESC_ID:
                throw new UnsupportedOperationException();
            case CON_ID:
                return new ConceptTransferable(ConceptBean.get(swdt.getTuple().getConceptId()));
            case TEXT:
                return new DescriptionTransferable(swdt.tuple);
            case LANG:
                return new StringSelection(swdt.tuple.getLang());
            case CASE_FIXED:
                return new StringSelection(swdt.getCellText());
            case STATUS:
                return new ConceptTransferable(ConceptBean.get(swdt.getTuple().getStatusId()));
            case TYPE:
                return new ConceptTransferable(ConceptBean.get(swdt.getTuple().getTypeId()));
            case VERSION:
                return new StringSelection(ThinVersionHelper.format(swdt.getTuple().getVersion()));
            case PATH:
                return new ConceptTransferable(ConceptBean.get(swdt.getTuple().getPathId()));
            default:
                throw new UnsupportedOperationException("Cana't handle field: " + field);
            }
        }

        private Transferable TransferableFromSWCT(Object obj, int column) {
            StringWithConceptTuple swct = (StringWithConceptTuple) obj;
            CONCEPT_FIELD field = (CONCEPT_FIELD) getColumnModel().getColumn(column).getIdentifier();
            switch (field) {
            case CON_ID:
                return new ConceptTransferable(ConceptBean.get(swct.getTuple().getConId()));
            case STATUS:
                return new ConceptTransferable(ConceptBean.get(swct.getTuple().getConceptStatus()));
            case DEFINED:
                return new StringSelection(swct.getCellText());
            case VERSION:
                return new StringSelection(ThinVersionHelper.format(swct.getTuple().getVersion()));
            case PATH:
                return new ConceptTransferable(ConceptBean.get(swct.getTuple().getPathId()));
            default:
                throw new UnsupportedOperationException("Cana't handle field: " + field);
            }
        }

        private Transferable transferableFromIWImgT(Object obj) throws DatabaseException {
            ImageWithImageTuple iwit = (ImageWithImageTuple) obj;
            return new StringSelection("<img src='ace:" + AceConfig.getVodb().nativeToUuid(iwit.tuple.getImageId())
                + "$" + AceConfig.getVodb().nativeToUuid(iwit.tuple.getConceptId()) + "'>");
        }

        private Transferable transferableFromSWImgT(Object obj, int column) throws DatabaseException {
            StringWithImageTuple swit = (StringWithImageTuple) obj;
            IMAGE_FIELD field = (IMAGE_FIELD) getColumnModel().getColumn(column).getIdentifier();
            switch (field) {
            case IMAGE_ID:
                return new StringSelection("<img src='ace:"
                    + AceConfig.getVodb().nativeToUuid(swit.getTuple().getImageId()) + "$"
                    + AceConfig.getVodb().nativeToUuid(swit.getTuple().getConceptId()) + "'>");
            case CON_ID:
                return new ConceptTransferable(ConceptBean.get(swit.getTuple().getConceptId()));
            case DESC:
                return new StringSelection(swit.getTuple().getTextDescription());
            case IMAGE:
                return new StringSelection("<img src='ace:"
                    + AceConfig.getVodb().nativeToUuid(swit.getTuple().getImageId()) + "$"
                    + AceConfig.getVodb().nativeToUuid(swit.getTuple().getConceptId()) + "'>");
            case FORMAT:
                return new StringSelection(swit.getTuple().getFormat());
            case STATUS:
                return new ConceptTransferable(ConceptBean.get(swit.getTuple().getStatusId()));
            case TYPE:
                return new ConceptTransferable(ConceptBean.get(swit.getTuple().getTypeId()));
            case VERSION:
                return new StringSelection(ThinVersionHelper.format(swit.getTuple().getVersion()));
            case PATH:
                return new ConceptTransferable(ConceptBean.get(swit.getTuple().getPathId()));
            default:
                throw new UnsupportedOperationException("Can't handle field: " + field);
            }
        }

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
    }

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
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.CTRL_MASK), TransferHandler.getCutAction().getValue(
            Action.NAME));
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK), TransferHandler.getCopyAction().getValue(
            Action.NAME));
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Event.CTRL_MASK), TransferHandler.getPasteAction().getValue(
            Action.NAME));

        ActionMap map = this.getActionMap();
        map.put("cut", new AceTransferAction("cut"));
        map.put("copy", new AceTransferAction("copy"));
        map.put("paste", new AceTransferAction("paste"));
    }

}
