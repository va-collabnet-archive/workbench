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
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ImageTableModel extends AbstractTableModel implements PropertyChangeListener {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public static class TextFieldEditor extends DefaultCellEditor {

        private static final long serialVersionUID = 1L;

        public TextFieldEditor() {
            super(new JTextField());
            final JTextField textField = new JTextField();
            editorComponent = textField;

            delegate = new EditorDelegate() {
                private static final long serialVersionUID = 1L;

                public void setValue(Object value) {
                    if (StringWithImageTuple.class.isAssignableFrom(value.getClass())) {
                        StringWithImageTuple swdt = (StringWithImageTuple) value;
                        textField.setText((value != null) ? swdt.tuple.getTextDescription() : "");
                    } else {
                        textField.setText((value != null) ? value.toString() : "");
                    }
                }

                public Object getCellEditorValue() {
                    return textField.getText();
                }
            };
            textField.addActionListener(delegate);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            ((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            if (evt instanceof MouseEvent) {
                int clickCount;
                // For double-click activation
                clickCount = 2;
                return ((MouseEvent) evt).getClickCount() >= clickCount;
            }
            return true;
        }
    }

    public static class TypeFieldEditor extends AbstractPopupFieldEditor {

        private static final long serialVersionUID = 1L;

        public TypeFieldEditor(I_ConfigAceFrame config) throws TerminologyException, IOException {
            super(config);
        }

        @Override
        public int[] getPopupValues() {
            return config.getEditImageTypePopup().getListArray();
        }

        @Override
        public I_GetConceptData getSelectedItem(Object value) throws TerminologyException, IOException {
            StringWithImageTuple swdt = (StringWithImageTuple) value;
            return Terms.get().getConcept(swdt.getTuple().getTypeId());
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            if (evt instanceof MouseEvent) {
                int clickCount;
                // For double-click activation
                clickCount = 2;
                return ((MouseEvent) evt).getClickCount() >= clickCount;
            }
            return true;
        }
    }

    public static class StatusFieldEditor extends AbstractPopupFieldEditor {

        private static final long serialVersionUID = 1L;

        public StatusFieldEditor(I_ConfigAceFrame config) throws TerminologyException, IOException {
            super(config);
        }

        @Override
        public int[] getPopupValues() {
            return config.getEditStatusTypePopup().getListArray();
        }

        @Override
        public I_GetConceptData getSelectedItem(Object value) throws TerminologyException, IOException {
            StringWithImageTuple swdt = (StringWithImageTuple) value;
            return Terms.get().getConcept(swdt.getTuple().getStatusId());
        }

        @Override
        public boolean isCellEditable(EventObject evt) {
            if (evt instanceof MouseEvent) {
                int clickCount;
                // For double-click activation
                clickCount = 2;
                return ((MouseEvent) evt).getClickCount() >= clickCount;
            }
            return true;
        }
    }

    public enum IMAGE_FIELD {
        IMAGE_ID("iid", 5, 100, 100),
        CON_ID("cid", 5, 100, 100),
        DESC("Description", 5, 200, 1000),
        IMAGE("image", 5, 200, 1000),
        STATUS("status", 5, 50, 250),
        FORMAT("format", 5, 30, 30),
        TYPE("type", 5, 85, 85),
        VERSION("time", 5, 140, 140),
        PATH("path", 5, 90, 150);

        private String columnName;
        private int min;
        private int pref;
        private int max;

        private IMAGE_FIELD(String columnName, int min, int pref, int max) {
            this.columnName = columnName;
            this.min = min;
            this.pref = pref;
            this.max = max;
        }

        public String getColumnName() {
            return columnName;
        }

        public int getMax() {
            return max;
        }

        public int getMin() {
            return min;
        }

        public int getPref() {
            return pref;
        }

    }

    public class ReferencedConceptsSwingWorker extends SwingWorker<Boolean> {
        private boolean stopWork = false;
        Map<Integer, I_GetConceptData> concepts;

        @Override
        protected Boolean construct() throws Exception {
            getProgress().setActive(true);
            concepts = new HashMap<Integer, I_GetConceptData>();
            Set<Integer> fetchSet = null;
            synchronized (conceptsToFetch) {
                fetchSet = new HashSet<Integer>(conceptsToFetch);
            }
            for (Integer id : fetchSet) {
                if (stopWork) {
                    return false;
                }
                I_GetConceptData b = Terms.get().getConcept(id);
                b.getDescriptions();
                concepts.put(id, b);

            }
            return true;
        }

        @Override
        protected void finished() {
            super.finished();
            try {
                if (get()) {
                    if (stopWork) {
                        return;
                    }
                    if (getProgress() != null) {
                        getProgress().getProgressBar().setIndeterminate(false);
                        if (conceptsToFetch.size() == 0) {
                            getProgress().getProgressBar().setValue(1);
                        } else {
                            getProgress().getProgressBar().setValue(conceptsToFetch.size());
                        }
                    }
                    referencedConcepts = concepts;
                    fireTableDataChanged();
                    if (getProgress() != null) {
                        getProgress().setProgressInfo("   " + getRowCount() + "   ");
                        getProgress().setActive(false);
                    }
                }
            } catch (InterruptedException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            } catch (ExecutionException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }

        }

        public void stop() {
            stopWork = true;
        }

    }

    public class TableChangedSwingWorker extends SwingWorker<Integer> {
    	I_GetConceptData cb;

        private boolean stopWork = false;

        public TableChangedSwingWorker(I_GetConceptData cb) {
            super();
            this.cb = cb;
        }

        @Override
        protected Integer construct() throws Exception {
            if (refConWorker != null) {
                refConWorker.stop();
            }
            if (cb == null) {
                return 0;
            }
            List<I_ImageVersioned> images = new ArrayList<I_ImageVersioned>();
            images.addAll(cb.getImages());
            for (I_ImageVersioned i : images) {
                if (stopWork) {
                    return -1;
                }
                for (I_ImagePart part : i.getMutableParts()) {
                    conceptsToFetch.add(part.getTypeId());
                    conceptsToFetch.add(part.getStatusId());
                    conceptsToFetch.add(part.getPathId());
                }

            }

            refConWorker = new ReferencedConceptsSwingWorker();
            refConWorker.start();
            return images.size();
        }

        @Override
        protected void finished() {
            super.finished();
            if (getProgress() != null) {
                getProgress().getProgressBar().setIndeterminate(false);
                if (conceptsToFetch.size() == 0) {
                    getProgress().getProgressBar().setValue(1);
                    getProgress().getProgressBar().setMaximum(1);
                } else {
                    getProgress().getProgressBar().setValue(1);
                    getProgress().getProgressBar().setMaximum(conceptsToFetch.size());
                }
            }
            if (stopWork) {
                return;
            }
            try {
                get();
            } catch (InterruptedException e) {
                ;
            } catch (ExecutionException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
            tableConcept = cb;
            fireTableDataChanged();

        }

        public void stop() {
            stopWork = true;
        }

    }

    public static class ImageWithImageTuple {
        ImageIcon image;
        I_ImageTuple tuple;

        public ImageWithImageTuple(ImageIcon image, I_ImageTuple tuple) {
            super();
            this.image = image;
            this.tuple = tuple;
        }

        public ImageIcon getImage() {
            return image;
        }

        public I_ImageTuple getTuple() {
            return tuple;
        }

    }

    public static class StringWithImageTuple extends StringWithTuple<StringWithImageTuple>  {

        I_ImageTuple tuple;

        public StringWithImageTuple(String cellText, I_ImageTuple tuple, boolean isInConflict) {
            super(cellText, isInConflict);
            this.tuple = tuple;
        }

        public I_ImageTuple getTuple() {
            return tuple;
        }
    }

    private IMAGE_FIELD[] columns;
    private SmallProgressPanel progress = new SmallProgressPanel();
    I_HostConceptPlugins host;
    List<I_ImageTuple> allImageTuples;
    ArrayList<I_ImageVersioned> allImages;

    private boolean showHistory;
    Map<Integer, I_GetConceptData> referencedConcepts = new HashMap<Integer, I_GetConceptData>();
    private Set<Integer> conceptsToFetch = new HashSet<Integer>();
    private TableChangedSwingWorker tableChangeWorker;
    private ReferencedConceptsSwingWorker refConWorker;
    private I_GetConceptData tableConcept;

    public ImageTableModel(I_HostConceptPlugins host, IMAGE_FIELD[] columns, boolean showHistory) {
        super();
        this.columns = columns;
        this.host = host;
        this.host.addPropertyChangeListener(I_ContainTermComponent.TERM_COMPONENT, this);
        this.showHistory = showHistory;
    }

    public int getColumnCount() {
        return columns.length;
    }

    public String getColumnName(int col) {
        return columns[col].getColumnName();
    }

    public Class<?> getColumnClass(int c) {
        switch (columns[c]) {
        case IMAGE:
            return ImageWithImageTuple.class;

        default:
            return String.class;
        }
    }

    protected I_ImageTuple getImage(int rowIndex) throws IOException {
        if (tableConcept == null) {
            return null;
        }
        if (showHistory) {
            return allImageTuples.get(rowIndex);
        } else {
            return getAllImages().get(rowIndex).getLastTuple();
        }
    }

    public Map<Integer, I_GetConceptData> getReferencedConcepts() {
        return referencedConcepts;

    }

    public List<I_ImageVersioned> getAllImages() throws IOException {
        if (allImages == null) {
            allImages = new ArrayList<I_ImageVersioned>();
            allImages.addAll(tableConcept.getImages());
        }
        return allImages;
    }

    public int getRowCount() {
        if (tableConcept == null) {
            return 0;
        }
        try {
            if (showHistory) {
                if (allImageTuples == null) {
                    allImageTuples = new ArrayList<I_ImageTuple>();
                    for (I_ImageVersioned i : getAllImages()) {
                        allImageTuples.addAll(i.getTuples());
                    }
                }
                return allImageTuples.size();
            } else {
                return getAllImages().size();
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return 0;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            if (rowIndex >= getRowCount()) {
                return null;
            }
            if (rowIndex == -1) {
                return null;
            }
            I_ImageTuple image = getImage(rowIndex);
            if (image == null) {
                return null;
            }

            I_ConfigAceFrame config = host.getConfig();
            boolean inConflict =
                    config.getHighlightConflictsInComponentPanel()
                        && config.getConflictResolutionStrategy().isInConflict((I_ImageVersioned) image.getFixedPart());

            switch (columns[columnIndex]) {
            case IMAGE_ID:
                return new StringWithImageTuple(Integer.toString(image.getImageId()), image, inConflict);
            case CON_ID:
                return new StringWithImageTuple(Integer.toString(image.getConceptId()), image, inConflict);
            case DESC:
                if (BasicHTML.isHTMLString(image.getTextDescription())) {
                    return new StringWithImageTuple(image.getTextDescription(), image, inConflict);
                } else {
                    return new StringWithImageTuple("<html>" + image.getTextDescription(), image, inConflict);
                }
            case IMAGE:
                return new ImageWithImageTuple(new ImageIcon(image.getImage()), image);
            case FORMAT:
                return new StringWithImageTuple(image.getFormat(), image, inConflict);
            case STATUS:
                if (getReferencedConcepts().containsKey(image.getStatusId())) {
                    return new StringWithImageTuple(getPrefText(image.getStatusId()), image, inConflict);
                }
                return new StringWithImageTuple(Integer.toString(image.getStatusId()), image, inConflict);
            case TYPE:
                if (getReferencedConcepts().containsKey(image.getTypeId())) {
                    return new StringWithImageTuple(getPrefText(image.getTypeId()), image, inConflict);
                }
                return new StringWithImageTuple(Integer.toString(image.getTypeId()), image, inConflict);
            case VERSION:
                if (image.getVersion() == Integer.MAX_VALUE) {
                    return new StringWithImageTuple(ThinVersionHelper.uncommittedHtml(), image, inConflict);
                }
                return new StringWithImageTuple(ThinVersionHelper.format(image.getVersion()), image, inConflict);
            case PATH:
                if (getReferencedConcepts().containsKey(image.getPathId())) {
                    return new StringWithImageTuple(getPrefText(image.getPathId()), image, inConflict);
                }
                return new StringWithImageTuple(Integer.toString(image.getPathId()), image, inConflict);
            }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
    }

    private String getPrefText(int id) throws IOException {
        I_GetConceptData cb = getReferencedConcepts().get(id);
        I_DescriptionTuple desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
        if (desc != null) {
            return desc.getText();
        }
        return cb.getInitialText();
    }

    public SmallProgressPanel getProgress() {
        return progress;
    }

    public void setProgress(SmallProgressPanel progress) {
        this.progress = progress;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        allImageTuples = null;
        allImages = null;
        if (getProgress() != null) {
            getProgress().setVisible(true);
            getProgress().getProgressBar().setValue(0);
            getProgress().getProgressBar().setIndeterminate(true);
        }
        tableConcept = null;
        if (tableChangeWorker != null) {
            tableChangeWorker.stop();
        }
        conceptsToFetch.clear();
        referencedConcepts.clear();
        tableChangeWorker = new TableChangedSwingWorker((I_GetConceptData) evt.getNewValue());
        tableChangeWorker.start();
        fireTableDataChanged();
    }

    public IMAGE_FIELD[] getColumnEnums() {
        return columns;
    }

    public void setColumns(IMAGE_FIELD[] columns) {
        if (this.columns.length != columns.length) {
            this.columns = columns;
            fireTableStructureChanged();
            return;
        }
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(this.columns[i]) == false) {
                this.columns = columns;
                fireTableStructureChanged();
                return;
            }
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (ACE.editMode == false) {
            return false;
        }
        try {
            I_ImageTuple image = getImage(rowIndex);
            if (image.getVersion() == Integer.MAX_VALUE) {
                switch (columns[columnIndex]) {
                case IMAGE_ID:
                    return false;
                case CON_ID:
                    return false;
                case DESC:
                    return true;
                case IMAGE:
                    return false;
                case FORMAT:
                    return false;
                case STATUS:
                    return true;
                case TYPE:
                    return true;
                case VERSION:
                    return false;
                case PATH:
                    return false;
                }
            }
            return false;
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return false;
    }

    public ImagePopupListener makePopupListener(JTable table, I_ConfigAceFrame config) {
        return new ImagePopupListener(table, config, this);
    }

    public void setValueAt(Object value, int row, int col) {
        try {
            I_ImageTuple image = getImage(row);
            if (image.getVersion() == Integer.MAX_VALUE) {
                switch (columns[col]) {
                case IMAGE_ID:
                    break;
                case CON_ID:
                    break;
                case DESC:
                    image.getMutablePart().setTextDescription(value.toString());
                    Terms.get().addUncommitted(Terms.get().getConcept(image.getConceptId()));
                    break;
                case STATUS:
                    Integer statusId = (Integer) value;
                    image.getMutablePart().setStatusId(statusId);
                    getReferencedConcepts().put(statusId, Terms.get().getConcept(statusId));
                    Terms.get().addUncommitted(Terms.get().getConcept(image.getConceptId()));
                    break;
                case TYPE:
                    Integer typeId = (Integer) value;
                    image.getMutablePart().setTypeId(typeId);
                    getReferencedConcepts().put(typeId, Terms.get().getConcept(typeId));
                    Terms.get().addUncommitted(Terms.get().getConcept(image.getConceptId()));
                    break;
                case VERSION:
                    break;
                case PATH:
                    break;
                }
                fireTableDataChanged();
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
		}
    }

}
