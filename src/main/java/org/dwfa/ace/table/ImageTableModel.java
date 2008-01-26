package org.dwfa.ace.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.AbstractTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.SmallProgressPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.swing.SwingWorker;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;

public class ImageTableModel extends AbstractTableModel implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public enum IMAGE_FIELD {
		IMAGE_ID("iid", 5, 100, 100), CON_ID("cid", 5, 100, 100), 
		DESC("Description", 5, 200, 1000), IMAGE("image", 5, 200, 1000), 
		STATUS("status", 5, 50, 250), FORMAT("format", 5, 30, 30),
		TYPE("type", 5, 85, 85), VERSION("version", 5, 140, 140), 
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

	public class ReferencedConceptsSwingWorker extends
			SwingWorker<Map<Integer, ConceptBean>> {
		private boolean stopWork = false;

		@Override
		protected Map<Integer, ConceptBean> construct() throws Exception {
			getProgress().setActive(true);
			Map<Integer, ConceptBean> concepts = new HashMap<Integer, ConceptBean>();
			for (Integer id : new HashSet<Integer>(conceptsToFetch)) {
				if (stopWork) {
					break;
				}
				ConceptBean b = ConceptBean.get(id);
				b.getDescriptions();
				concepts.put(id, b);

			}
			return concepts;
		}

		@Override
		protected void finished() {
			super.finished();
			if (getProgress() != null) {
				getProgress().getProgressBar().setIndeterminate(false);
				if (conceptsToFetch.size() == 0) {
					getProgress().getProgressBar().setValue(1);
				} else {
					getProgress().getProgressBar().setValue(conceptsToFetch.size());
				}
			}
			if (stopWork) {
				return;
			}
			try {
				referencedConcepts = get();
			} catch (InterruptedException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			} catch (ExecutionException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
			fireTableDataChanged();
			if (getProgress() != null) {
				getProgress().setProgressInfo("   " + getRowCount() + "   ");
				getProgress().setActive(false);
			}

		}

		public void stop() {
			stopWork = true;
		}

	}

	public class TableChangedSwingWorker extends SwingWorker<Integer> {
		ConceptBean cb;

		private boolean stopWork = false;

		public TableChangedSwingWorker(ConceptBean cb) {
			super();
			this.cb = cb;
		}

		@Override
		protected Integer construct() throws Exception {
			if (refConWorker != null) {
				refConWorker.stop();
			}
			conceptsToFetch.clear();
			referencedConcepts.clear();
			if (cb == null) {
				return 0;
			}
			List<I_ImageVersioned> images = new ArrayList<I_ImageVersioned>();
			images.addAll(cb.getImages());
			images.addAll(cb.getUncommittedImages());
			for (I_ImageVersioned i : images) {
				if (stopWork) {
					return -1;
				}
				for (I_ImagePart part : i.getVersions()) {
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
	
	public static class ImageWithImageTuple  {
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
	public static class StringWithImageTuple implements Comparable<StringWithImageTuple>, I_CellTextWithTuple {
		String cellText;
		I_ImageTuple tuple;
		public StringWithImageTuple(String cellText, I_ImageTuple tuple) {
			super();
			this.cellText = cellText;
			this.tuple = tuple;
		}
		public String getCellText() {
			return cellText;
		}
		public I_ImageTuple getTuple() {
			return tuple;
		}
		
		public String toString() {
			return cellText;
		}
		public int compareTo(StringWithImageTuple another) {
			return cellText.compareTo(another.cellText);
		}
	}
	private IMAGE_FIELD[] columns;
	private SmallProgressPanel progress = new SmallProgressPanel();
	I_HostConceptPlugins host;
	List<I_ImageTuple> allImageTuples;
	ArrayList<I_ImageVersioned> allImages;

	private boolean showHistory;
	Map<Integer, ConceptBean> referencedConcepts = new HashMap<Integer, ConceptBean>();
	private Set<Integer> conceptsToFetch = new HashSet<Integer>();
	private TableChangedSwingWorker tableChangeWorker;
	private ReferencedConceptsSwingWorker refConWorker;
	private ConceptBean tableConcept;

	public ImageTableModel(
			I_HostConceptPlugins host, 
			IMAGE_FIELD[] columns, boolean showHistory) {
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
	protected I_ImageTuple getImage(int rowIndex)
	throws IOException {
		if (tableConcept == null) {
			return null;
		}
		if (showHistory) {
			return allImageTuples.get(rowIndex);
		} else {
			return getAllImages().get(rowIndex).getLastTuple();
		}
	}
	public Map<Integer, ConceptBean> getReferencedConcepts() {
		return referencedConcepts;

	}
	public List<I_ImageVersioned> getAllImages() throws IOException {
		if (allImages == null) {
			allImages = new ArrayList<I_ImageVersioned>();
			allImages.addAll(tableConcept.getUncommittedImages());
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
					for (I_ImageVersioned i: getAllImages()) {
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
			I_ImageTuple image = getImage(rowIndex);
			if (image == null) {
				return null;
			}

			switch (columns[columnIndex]) {
			case IMAGE_ID:
				return new StringWithImageTuple(Integer.toString(image.getImageId()), image);
			case CON_ID:
				return new StringWithImageTuple(Integer.toString(image.getConceptId()), image);
			case DESC:
				if (BasicHTML.isHTMLString(image.getTextDescription())) {
					return new StringWithImageTuple(image.getTextDescription(), image);
				} else {
					return new StringWithImageTuple("<html>" + image.getTextDescription(), image);
				}
			case IMAGE:
				return new ImageWithImageTuple(new ImageIcon(image.getImage()), image);
			case FORMAT:
				return new StringWithImageTuple(image.getFormat(), image);
			case STATUS:
				if (getReferencedConcepts().containsKey(image.getStatusId())) {
					return new StringWithImageTuple(getPrefText(image.getStatusId()), image);
				}
				return new StringWithImageTuple(Integer.toString(image.getStatusId()), image);
			case TYPE:
				if (getReferencedConcepts().containsKey(image.getTypeId())) {
					return new StringWithImageTuple(getPrefText(image.getTypeId()), image);
				}
				return new StringWithImageTuple(Integer.toString(image.getTypeId()), image);
			case VERSION:
				if (image.getVersion() == Integer.MAX_VALUE) {
					return new StringWithImageTuple(ThinVersionHelper.uncommittedHtml(), image);
				}
				return new StringWithImageTuple(ThinVersionHelper.format(image.getVersion()), image);
			case PATH:
				if (getReferencedConcepts().containsKey(image.getPathId())) {
					return new StringWithImageTuple(getPrefText(image.getPathId()), image);
				}
				return new StringWithImageTuple(Integer.toString(image.getPathId()), image);
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return null;
	}

	private String getPrefText(int id) throws IOException {
		ConceptBean cb = getReferencedConcepts().get(id);
		I_DescriptionTuple desc = cb.getDescTuple(host.getConfig().getTableDescPreferenceList(), host.getConfig());
		if (desc != null) {
			return desc.getText();
		}
		return "null pref desc: " + cb.getInitialText();
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
		fireTableDataChanged();
		if (tableChangeWorker != null) {
			tableChangeWorker.stop();
		}
		tableChangeWorker = new TableChangedSwingWorker((ConceptBean) evt
				.getNewValue());
		tableChangeWorker.start();
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

}
