package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import org.dwfa.ace.I_ImplementActiveLabel;
import org.dwfa.ace.LabelForTuple;
import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinConTuple;
import org.dwfa.vodb.types.ThinRelTuple;

import com.sleepycat.je.DatabaseException;

/**
 * Stated & Inferred Panel view.
 * 
 */
public class LogicalFormsPanel extends JPanel implements ActionListener {

	/**
	 * <b>DeltaColors</b><br>
	 * Uses <code>AWT Color</code> object which use some of the following <a
	 * href=http://www.w3schools.com/html/html_colornames.asp>color names &
	 * values</a>. These colors are used to highlight differences.
	 * 
	 */
	public static class DeltaColors {

		private List<Color> colorList = new ArrayList<Color>(); // AWT: Color

		int currentColor = 0;

		public DeltaColors() {
			super();
			// Link for colors
			// http://www.w3schools.com/html/html_colornames.asp
			colorList.add(new Color(0x5F9EA0));
			colorList.add(new Color(0x7FFF00));
			colorList.add(new Color(0xD2691E));
			colorList.add(new Color(0x6495ED));
			colorList.add(new Color(0xDC143C));
			colorList.add(new Color(0xB8860B));
			colorList.add(new Color(0xFF8C00));
			colorList.add(new Color(0x8FBC8F));
			colorList.add(new Color(0x483D8B));
			colorList.add(new Color(0x1E90FF));
			colorList.add(new Color(0xFFD700));
			colorList.add(new Color(0xF0E68C));
			colorList.add(new Color(0x90EE90));
			colorList.add(new Color(0x8470FF)); // 14 colors
		}

		public Color getNextColor() {
			if (currentColor == colorList.size()) {
				reset();
			}
			return colorList.get(currentColor++);
		}

		public void reset() {
			currentColor = 0;
		}
	}

	private static final long serialVersionUID = 1L;

	// ** GRAPHICAL USER INTERFACE **
	private JPanel commonJPanel;
	private JPanel commonPartJPanel;
	private JPanel deltaJPanel;
	private JPanel deltaPartJPanel;
	private JPanel formsJPanel; // sub panels added using tmpJPanel
	//private JPanel statsJPanel; // for additional useful information

	private JCheckBox showStatusCB = new JCheckBox("show status");
	private JCheckBox showDetailCB = new JCheckBox("show detail");
	private JCheckBox showDistFormCB = new JCheckBox("Distribution Normal");
	private JCheckBox showAuthFormCB = new JCheckBox("Authoring Normal");
	private JCheckBox showLongFormCB = new JCheckBox("Long Canonical");
	private JCheckBox showShortFormCB = new JCheckBox("Short Canonical");

	// JLabel with ActionListener
	private List<I_ImplementActiveLabel> commonLabels;

	// AWT: Dimension(int Width, int Height) in pixels(???)
	private Dimension maxPartPanelSize = new Dimension(
			TermLabelMaker.LABEL_WIDTH + 20, 4000);
	private Dimension minPartPanelSize = new Dimension(
			TermLabelMaker.LABEL_WIDTH + 20, 100);

	private DeltaColors colors = new DeltaColors();

	// ** WORKBENCH PARTICULARS **
	private I_TermFactory tf;
	private I_ConfigAceFrame config;
	private ConceptBean theCBean;

	// ** CORE CONSTANTS **
	private static int isaNid;
	private static int isCURRENT = Integer.MIN_VALUE;

	// INPUT PATHS
	I_GetConceptData cEditPathObj = null;
	I_Path cEditIPath = null;
	List<I_Position> cEditPathPos = null; // Edit (Stated) Path I_Positions

	// OUTPUT PATHS
	I_GetConceptData cClassPathObj;
	I_Path cClassIPath;
	List<I_Position> cClassPathPos; // Classifier (Inferred) Path I_Positions

	I_Path inferredPath;
	Position inferredPos;
	I_Path statedPath;
	Position statedPos;

	// ** STATISTICS **
	// !!! :TODO: ??? need reset statistics routine
	private int countFindIsaProxDuplPart = 0;
	private int countFindRoleProxDuplPart = 0;
	private int countFindSelfDuplPart = 0;
	private int countIsCDefinedDuplPart = 0;
	private int countFindIsaProxDuplPartGE2 = 0;
	private int countFindRoleProxDuplPartGE2 = 0;
	private int countFindSelfDuplPartGE2 = 0;
	private int countIsCDefinedDuplPartGE2 = 0;

	// private int countIsCDefinedDuplPart = 0;

	public LogicalFormsPanel() {
		super();
		setLayout(new GridBagLayout()); // LogicalFormsPanel LayoutManager
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST; // Place LogicalFormsPanel

		// TOP ROW
		c.gridy = 0; // first row
		c.gridx = 0; // first in row
		c.weightx = 0.0; // no extra space
		c.weighty = 0.0; // no extra space
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;

		JLabel label = new JLabel("Normal Forms:");
		label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
		add(label, c);
		c.gridx++;
		add(showDistFormCB, c);
		c.gridx++;
		add(showAuthFormCB, c);
		c.gridx++;
		add(showLongFormCB, c);
		c.gridx++;
		add(showShortFormCB, c);

		// FORM SELECTION CHECKBOX ROW
		c.gridy++; // next row
		c.gridx = 0; // first cell in row
		c.gridwidth = 1;
		c.weightx = 0.0;
		c.fill = GridBagConstraints.NONE;

		label = new JLabel("Information:");
		label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
		add(label, c);

		c.gridx++;
		add(showDetailCB, c);
		c.gridx++;
		add(showStatusCB, c);

		// SETUP CHECKBOX VALUES & LISTENER
		showStatusCB.setSelected(false);
		showStatusCB.addActionListener(this);
		showDetailCB.setSelected(false);
		showDetailCB.addActionListener(this);
		showDistFormCB.setSelected(true);
		showDistFormCB.addActionListener(this);
		showAuthFormCB.setSelected(false);
		showAuthFormCB.addActionListener(this);
		showLongFormCB.setSelected(false);
		showLongFormCB.addActionListener(this);
		showShortFormCB.setSelected(false);
		showShortFormCB.addActionListener(this);

		// COMMON & DIFFERENT PANELS ROW
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		commonJPanel = newMinMaxJPanel();
		commonJPanel.setLayout(new GridBagLayout());
		commonJPanel.setName("Common Panel");
		commonJPanel.setBorder(BorderFactory.createTitledBorder("Common: "));
		add(commonJPanel, c);
		c.gridx = c.gridx + c.gridwidth;
		deltaJPanel = newMinMaxJPanel();
		deltaJPanel.setLayout(new GridLayout(0, 1));
		deltaJPanel.setName("Differences Panel");
		deltaJPanel.setBorder(BorderFactory.createTitledBorder("Different: "));
		add(deltaJPanel, c);

		// FORMS PANEL ROW
		c.gridy++;// next row
		c.gridx = 0; // reset at west side of row
		c.gridwidth = 4; // number of cells in row
		formsJPanel = new JPanel(new GridBagLayout());
		formsJPanel.setName("Forms Panel");
		formsJPanel.setBorder(BorderFactory.createTitledBorder("Forms: "));
		JScrollPane formJScrollPane = new JScrollPane(formsJPanel);
		formJScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		add(formJScrollPane, c);

		// STATS PANEL ROW
		if (false) {
			// c.gridy++;// next row
			// c.gridx = 0; // reset at west side of row
			// c.gridwidth = 1;
			//statsJPanel = new JPanel(new GridBagLayout());
			//statsJPanel.setName("Stats Panel");
			//statsJPanel.setBorder(BorderFactory.createTitledBorder("Stats: "));
			//add(statsJPanel, c);
		}

		// COMPONENT BORDER
		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEmptyBorder(1, 1, 1, 3), BorderFactory
				.createLineBorder(Color.GRAY)));

		tf = LocalVersionedTerminology.get();
		// SETUP CLASSIFIER PREFERENCE FIELDS
		try {
			config = tf.getActiveAceFrameConfig();
			isaNid = config.getClassifierIsaType().getConceptId();
			// :TODO: review as acceptable status set @@@
			isCURRENT = tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT
					.getUids()); // 0 CURRENT, 1 RETIRED

			// GET INPUT & OUTPUT PATHS FROM CLASSIFIER PREFERRENCES
			// :TODO: Does multiple edit paths matter ???
			// if (config.getEditingPathSet().size() != 1) {
			// throw new TaskFailedException(
			// "Profile must have only one edit path. Found: "
			// + tf.getActiveAceFrameConfig()
			// .getEditingPathSet());
			// }

			// GET ALL EDIT_PATH ORIGINS
			I_GetConceptData cEditPathObj = config.getClassifierInputPath();
			cEditIPath = tf.getPath(cEditPathObj.getUids());
			cEditPathPos = new ArrayList<I_Position>();
			cEditPathPos.add(new Position(Integer.MAX_VALUE, cEditIPath));
			addPathOrigins(cEditPathPos, cEditIPath);

			// GET ALL CLASSIFER_PATH ORIGINS
			I_GetConceptData cClassPathObj = config.getClassifierOutputPath();
			cClassIPath = tf.getPath(cClassPathObj.getUids());
			cClassPathPos = new ArrayList<I_Position>();
			cClassPathPos.add(new Position(Integer.MAX_VALUE, cClassIPath));
			addPathOrigins(cClassPathPos, cClassIPath);

		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void addPathOrigins(List<I_Position> origins, I_Path p) {
		origins.addAll(p.getOrigins());
		for (I_Position o : p.getOrigins()) {
			addPathOrigins(origins, o.getPath());
		}
	}

	private void setMinMaxSize(JPanel panel) {
		panel.setMinimumSize(minPartPanelSize);
		panel.setMaximumSize(maxPartPanelSize);
	}

	private JPanel newMinMaxJPanel() {
		JPanel p = new JPanel() {
			/**
	          * 
	          */
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.width = Math.max(d.width, minPartPanelSize.width);
				d.height = Math.max(d.height, minPartPanelSize.height);
				d.height = Math.min(d.height, maxPartPanelSize.height);
				d.height = Math.min(d.height, maxPartPanelSize.height);
				return d;
			}
		};
		setMinMaxSize(p);
		return p;
	}

	/**
	 * Called each time the concept selection changes.
	 * 
	 * @param theCBean
	 * @param config
	 * @throws IOException
	 */
	public void setConcept(ConceptBean conceptIn, I_ConfigAceFrame config)
			throws IOException {
		this.theCBean = conceptIn;
		this.config = config;
		commonJPanel.removeAll();
		deltaJPanel.removeAll();
		formsJPanel.removeAll(); // FORMS HAS TWO SUBPANELS: STATED & COMPUTED
		// statsJPanel.removeAll();

		if (conceptIn != null) {
			// COMMON PANEL
			commonLabels = getCommonLabels(showDetailCB.isSelected(),
					showStatusCB.isSelected(), config); // ####
			commonPartJPanel = new JPanel();
			setMinMaxSize(commonPartJPanel);
			commonPartJPanel.setLayout(new BoxLayout(commonPartJPanel,
					BoxLayout.Y_AXIS));
			for (I_ImplementActiveLabel l : commonLabels) {
				commonPartJPanel.add(l.getLabel());
			}

			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.SOUTHEAST;
			c.fill = GridBagConstraints.NONE;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 1;
			c.weightx = 1;
			c.weighty = 1;

			c.anchor = GridBagConstraints.NORTHWEST;
			c.gridx = 0;
			c.gridy = 0;
			c.weighty = 0;
			commonJPanel.add(commonPartJPanel, c);

			// DELTA (DIFFERENCES) PANEL
			Map<I_ConceptAttributeTuple, Color> conAttrColorMap = new HashMap<I_ConceptAttributeTuple, Color>();
			Map<I_DescriptionTuple, Color> desColorMap = new HashMap<I_DescriptionTuple, Color>();
			Map<I_RelTuple, Color> relColorMap = new HashMap<I_RelTuple, Color>();
			colors.reset();
			Collection<I_ImplementActiveLabel> deltaLabels = getDeltaLabels(
					showDetailCB.isSelected(), showStatusCB.isSelected(),
					config, colors, conAttrColorMap, desColorMap, relColorMap); // ####
			deltaPartJPanel = new JPanel();
			deltaPartJPanel.setLayout(new BoxLayout(deltaPartJPanel,
					BoxLayout.Y_AXIS));
			for (I_ImplementActiveLabel l : deltaLabels) {
				deltaPartJPanel.add(l.getLabel());
			}
			deltaJPanel.add(deltaPartJPanel);

			// FORM STATED PANEL
			c = new GridBagConstraints();
			c.fill = GridBagConstraints.VERTICAL;
			c.anchor = GridBagConstraints.NORTHWEST;
			c.weightx = 0; // horizontal free space distribution weight
			c.weighty = 0; // vertical free space distribution weight
			c.gridx = 0;
			c.gridy = 0;

			I_Path path;
			try {
				path = ((VodbEnv) LocalVersionedTerminology.get())
						.getPath(config.getClassifierInputPath().getConceptId());
			} catch (DatabaseException e) {
				throw new ToIoException(e);
			}
			I_Position p = new Position(Integer.MAX_VALUE, path);

			JPanel tmpJPanel;
			tmpJPanel = newFormStatedJPanel("Stated Form:", p, config,
					conAttrColorMap, desColorMap, relColorMap); // ####
			setMinMaxSize(tmpJPanel);
			formsJPanel.add(tmpJPanel, c);

			// FORM DISTRIBUTION NORMAL PANEL
			if (showDistFormCB.isSelected()) {
				c.gridx++;
				if (c.gridx == 2) {
					c.gridx = 0;
					c.gridy++;
				}
				tmpJPanel = newFormDistJPanel("Distribution Normal Form:",
						inferredPos, config, conAttrColorMap, desColorMap,
						relColorMap); // ####
				setMinMaxSize(tmpJPanel);
				formsJPanel.add(tmpJPanel, c);
			}

			// AUTHORING NORMAL FORM PANEL
			if (showAuthFormCB.isSelected()) {
				c.gridx++;
				if (c.gridx == 2) {
					c.gridx = 0;
					c.gridy++;
				}
				tmpJPanel = newFormAuthJPanel("Authoring Normal Form:",
						inferredPos, config, conAttrColorMap, desColorMap,
						relColorMap); // ####
				setMinMaxSize(tmpJPanel);
				formsJPanel.add(tmpJPanel, c);
			}

			// LONG CANONICAL FORM PANEL
			if (showLongFormCB.isSelected()) {
				c.gridx++;
				if (c.gridx == 2) {
					c.gridx = 0;
					c.gridy++;
				}
				tmpJPanel = newFormLongJPanel("Long Canonical Form:",
						inferredPos, config, conAttrColorMap, desColorMap,
						relColorMap); // ####
				setMinMaxSize(tmpJPanel);
				formsJPanel.add(tmpJPanel, c);
			}

			// FORM SHORT CANONICAL PANEL
			if (showShortFormCB.isSelected()) {
				c.gridx++;
				if (c.gridx == 2) {
					c.gridx = 0;
					c.gridy++;
				}
				tmpJPanel = newFormShortJPanel("Short Canonical Form:",
						inferredPos, config, conAttrColorMap, desColorMap,
						relColorMap); // ####
				setMinMaxSize(tmpJPanel);
				formsJPanel.add(tmpJPanel, c);
			}

			// STATISTICS PANEL
			if (false) {
				c = new GridBagConstraints();
				c.fill = GridBagConstraints.BOTH;
				c.anchor = GridBagConstraints.NORTHWEST;
				c.weighty = 0; // vertical free space distribution weight
				c.weightx = 0; // horizontal free space distribution weight
				c.gridy = 0; // reset to row one
				c.gridx = 0; // reset to column one

				String markup = statsToHtml();
				JEditorPane ep2 = new JEditorPane("text/html", markup);
				JScrollPane statsJScroll = new JScrollPane(ep2);
				statsJScroll.setBorder(new TitledBorder("Statistics"));
				// statsJPanel.add(statsJScroll);

				AceLog.getAppLog().log(Level.INFO, statsToString());
				statsReset();
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		try {
			setConcept(theCBean, config);
			revalidate();
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLog(this, Level.SEVERE,
					"Database Exception: " + e1.getLocalizedMessage(), e1);
		}
	}

	public List<I_ImplementActiveLabel> getCommonLabels(boolean showLongForm,
			boolean showStatus, I_ConfigAceFrame config) throws IOException {
		List<I_ImplementActiveLabel> labelList = new ArrayList<I_ImplementActiveLabel>();

		// GET CONCEPT ATTRIBUTES
		Set<I_ConceptAttributeTuple> commonConTuples = this.theCBean
				.getCommonConceptAttributeTuples(config); // #### COMMON CON
		// CREATE CONCEPT ATTRIBUTE LABELS
		if (commonConTuples != null) {
			for (I_ConceptAttributeTuple t : commonConTuples) {
				I_ImplementActiveLabel conAttrLabel = TermLabelMaker.newLabel(
						t, showLongForm, showStatus);
				setBorder(conAttrLabel.getLabel(), null);
				labelList.add(conAttrLabel);
			}
		}

		// GET SOURCE RELATIONSHIPS
		Set<I_RelTuple> commonRelTuples = this.theCBean
				.getCommonRelTuples(config); // #### COMMON REL
		// CREATE RELATIONSHIP LABELS
		if (commonRelTuples != null) {
			for (I_RelTuple t : commonRelTuples) {
				I_ImplementActiveLabel relLabel = TermLabelMaker.newLabel(t,
						showLongForm, showStatus);
				setBorder(relLabel.getLabel(), null);
				labelList.add(relLabel);
			}
		}

		return labelList;
	}

	private void setBorder(JLabel tLabel, Color deltaColor) {
		if (deltaColor == null) {
			deltaColor = Color.white;
		}
		Dimension size = tLabel.getSize();
		tLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createRaisedBevelBorder(), BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 5, 1, 5, deltaColor),
				BorderFactory.createEmptyBorder(1, 3, 1, 3))));
		size.width = size.width + 18;
		size.height = size.height + 6;
		tLabel.setSize(size);
		tLabel.setPreferredSize(size);
		tLabel.setMaximumSize(size);
		tLabel.setMinimumSize(size);
	}

	public Collection<I_ImplementActiveLabel> getDeltaLabels(
			boolean showLongForm, boolean showStatus, I_ConfigAceFrame config,
			DeltaColors colors,
			Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
			Map<I_DescriptionTuple, Color> descColorMap,
			Map<I_RelTuple, Color> relColorMap) throws IOException {

		Set<I_ConceptAttributeTuple> allConAttrTuples = new HashSet<I_ConceptAttributeTuple>();
		Set<I_RelTuple> allRelTuples = new HashSet<I_RelTuple>();

		// FIND ALL...
		for (I_Position p : config.getViewPositionSet()) {
			Set<I_Position> posSet = new HashSet<I_Position>();
			posSet.add(p);

			// concept attributes
			List<I_ConceptAttributeTuple> conTuplesForPosition = this.theCBean
					.getConceptAttributeTuples(config.getAllowedStatus(),
							posSet, false); // #### ALL COMMON CON
			allConAttrTuples.addAll(conTuplesForPosition);

			// relationships
			List<I_RelTuple> relTuplesForPosition = this.theCBean
					.getSourceRelTuples(config.getAllowedStatus(), null,
							posSet, false); // #### ALL REL
			allRelTuples.addAll(relTuplesForPosition);
		}

		// FIND & REMOVE COMMON...
		Set<I_ConceptAttributeTuple> commonConAttrTuples = this.theCBean
				.getCommonConceptAttributeTuples(config); // #### COMMON CON
		allConAttrTuples.removeAll(commonConAttrTuples);
		Set<I_RelTuple> commonRelTuples = this.theCBean
				.getCommonRelTuples(config); // #### COMMON REL
		allRelTuples.removeAll(commonRelTuples);

		Collection<I_ImplementActiveLabel> labelList = new ArrayList<I_ImplementActiveLabel>();
		// CREATE CONCEPT ATTRIBUTE LABELS
		for (I_ConceptAttributeTuple t : allConAttrTuples) {
			I_ImplementActiveLabel conAttrLabel = TermLabelMaker.newLabel(t,
					showLongForm, showStatus);
			Color deltaColor = colors.getNextColor();
			conAttrColorMap.put(t, deltaColor);
			setBorder(conAttrLabel.getLabel(), deltaColor);
			labelList.add(conAttrLabel);
		}
		// CREATE RELATIONSHIP LABELS
		for (I_RelTuple t : allRelTuples) {
			I_ImplementActiveLabel relLabel = TermLabelMaker.newLabel(t,
					showLongForm, showStatus);
			Color deltaColor = colors.getNextColor();
			relColorMap.put(t, deltaColor);
			setBorder(relLabel.getLabel(), deltaColor);
			labelList.add(relLabel);
		}

		return labelList;
	}

	private List<I_RelTuple> findIsaProximal(ConceptBean cBean,
			List<I_Position> posList) {
		List<I_RelTuple> returnRTuples = new ArrayList<I_RelTuple>();
		try {
			List<I_RelVersioned> relList = cBean.getSourceRels();
			for (I_RelVersioned rel : relList) { // FOR EACH [C1, C2] PAIR
				// FIND MOST_RECENT REL PART, ON HIGHEST_PRIORITY_PATH
				I_RelPart rp1 = null;
				for (I_Position pos : posList) { // FOR EACH PATH POSITION
					// FIND MOST CURRENT
					int tmpCountDupl = 0;
					for (I_RelPart rp : rel.getVersions()) {
						if (rp.getPathId() == pos.getPath().getConceptId()) {
							if (rp1 == null) {
								rp1 = rp; // ... KEEP FIRST_INSTANCE PART
							} else if (rp1.getVersion() < rp.getVersion()) {
								rp1 = rp; // ... KEEP MORE_RECENT PART
							} else if (rp1.getVersion() == rp.getVersion()) {
								// DUPLICATE PART SHOULD NEVER HAPPEN
								tmpCountDupl++;
							}
						}
					}
					if (rp1 != null) {
						if (rp1.getStatusId() == isCURRENT
								&& rp1.getTypeId() == isaNid) {
							returnRTuples.add(new ThinRelTuple(rel, rp1));
						}
						// VERIFICATION STATISTICS
						if (tmpCountDupl > 1) {
							countFindIsaProxDuplPart++;
							countFindIsaProxDuplPartGE2++;
						} else if (tmpCountDupl == 1) {
							countFindIsaProxDuplPart++;
						}
						break; // IF FOUND ON THIS PATH, STOP SEARCHING
					}
				} // FOR EACH PATH POSITION

			} // FOR EACH [C1, C2] PAIR
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return returnRTuples;
	}

	private List<I_RelTuple> findRoleProximal(ConceptBean cBean,
			List<I_Position> posList) {
		List<I_RelTuple> returnRTuples = new ArrayList<I_RelTuple>();
		try {
			List<I_RelVersioned> relList = cBean.getSourceRels();
			for (I_RelVersioned rel : relList) { // FOR EACH [C1, C2] PAIR
				// FIND MOST_RECENT REL PART, ON HIGHEST_PRIORITY_PATH
				I_RelPart rp1 = null;
				for (I_Position pos : posList) { // FOR EACH PATH POSITION
					// FIND MOST CURRENT
					int tmpCountDupl = 0;
					for (I_RelPart rp : rel.getVersions()) {
						if (rp.getPathId() == pos.getPath().getConceptId()) {
							if (rp1 == null) {
								rp1 = rp; // ... KEEP FIRST_INSTANCE PART
							} else if (rp1.getVersion() < rp.getVersion()) {
								rp1 = rp; // ... KEEP MORE_RECENT PART
							} else if (rp1.getVersion() == rp.getVersion()) {
								// DUPLICATE PART SHOULD NEVER HAPPEN
								tmpCountDupl++;
							}
						}
					}
					if (rp1 != null) {
						if (rp1.getStatusId() == isCURRENT
								&& rp1.getTypeId() != isaNid) {
							returnRTuples.add(new ThinRelTuple(rel, rp1));
						}
						// VERIFICATION STATISTICS
						if (tmpCountDupl > 1) {
							countFindRoleProxDuplPart++;
							countFindRoleProxDuplPartGE2++;
						} else if (tmpCountDupl == 1) {
							countFindRoleProxDuplPart++;
						}
						break; // IF FOUND ON THIS PATH, STOP SEARCHING
					}
				} // FOR EACH PATH POSITION

			} // FOR EACH [C1, C2] PAIR
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return returnRTuples;
	}

	private List<I_RelTuple> findIsaProximalPrim(ConceptBean cBean,
			List<I_Position> posList) {

		List<ConceptBean> isaCBNext = new ArrayList<ConceptBean>();
		List<I_RelTuple> isaRTFinal = new ArrayList<I_RelTuple>();

		List<I_RelTuple> isaRTProx = findIsaProximal(cBean, posList);
		while (isaRTProx.size() > 0) {
			// TEST LIST FOR PRIMITIVE OR NOT
			for (I_RelTuple isaRT : isaRTProx) {
				ConceptBean isaCB = ConceptBean.get(isaRT.getC2Id());
				if (isCDefined(isaCB, cEditPathPos)) { // i.e. not primitive
					isaCBNext.add(isaCB); // keep looking for primitive
				} else {
					int z = 0;
					while ((z < isaRTFinal.size())
							&& isaRTFinal.get(z).getC2Id() != isaRT.getC2Id()) {
						z++;
					}
					// IF NOT_REDUNDANT, THEN ADD
					if (z == isaRTFinal.size()) {
						isaRTFinal.add(isaRT); // add to return primitives list
					}
				}
			}

			// GET ALL NEXT LEVEL RELS FOR NON_PRIMITIVE CONCEPTS
			isaRTProx = new ArrayList<I_RelTuple>();
			for (ConceptBean cbNext : isaCBNext) {
				List<I_RelTuple> nextTuples = findIsaProximal(cbNext, posList);
				if (nextTuples.size() > 0)
					isaRTProx.addAll(nextTuples);
			}

			// RESET NEXT LEVEL SEARCH LIST
			isaCBNext = new ArrayList<ConceptBean>(); // clear find next list
		}

		return isaRTFinal;
	}

	private List<I_RelTuple> findRoleDiffFromRoot(ConceptBean cBean,
			List<I_Position> posList) {

		List<I_RelTuple> roleRTFinal = new ArrayList<I_RelTuple>();

		// GET IMMEDIATE PROXIMAL ROLES
		List<I_RelTuple> roleRTProx = findRoleProximal(cBean, posList);
		roleRTFinal.addAll(roleRTProx);

		// GET PROXIMAL ISAs, one next level up at a time
		List<I_RelTuple> isaRTNext = findIsaProximal(cBean, posList);
		List<I_RelTuple> isaRTNextB = new ArrayList<I_RelTuple>();
		while (isaRTNext.size() > 0) {

			// FOR EACH PROXIMAL CONCEPT...
			for (I_RelTuple isaRT : isaRTNext) {
				ConceptBean isaCB = ConceptBean.get(isaRT.getC2Id());

				// ... EVALUATE PROXIMAL ROLES
				roleRTProx = findRoleProximal(isaCB, posList);
				for (I_RelTuple roleRT : roleRTProx) {
					// CHECK FOR REDUNDANT TYPE ID
					int z = 0;
					while ((z < roleRTFinal.size())
							&& roleRTFinal.get(z).getTypeId() != roleRT
									.getTypeId()) {
						z++;
					}
					if (z == roleRTFinal.size()) {
						// THEN ADD PROXIMAL ROLE TO FINALROLES
						roleRTFinal.add(roleRT);
					}
				}

				// ... GET PROXIMAL ISAs
				isaRTNextB.addAll(findIsaProximal(isaCB, posList));
			}

			// SETUP NEXT LEVEL OF ISAs
			isaRTNext = isaRTNextB;
			isaRTNextB = new ArrayList<I_RelTuple>();
		}

		return roleRTFinal;
	}

	private List<I_RelTuple> findRoleDiffFromProx(ConceptBean cBean,
			List<I_RelTuple> isaList, List<I_Position> posList) {

		// FIND IMMEDIATE ROLES OF *THIS*CONCEPT*
		List<I_RelTuple> roleRTSetA = findRoleProximal(cBean, posList);

		// FIND NON-REDUNDANT ROLE SET OF PROXIMATE ISA
		List<I_RelTuple> roleRTSetB = new ArrayList<I_RelTuple>();
		for (I_RelTuple isaRT : isaList) {
			ConceptBean isaCB = ConceptBean.get(isaRT.getC2Id());
			List<I_RelTuple> tmpRoleRT = findRoleDiffFromRoot(isaCB, posList);

			// check if already in set b
			for (I_RelTuple roleRT : tmpRoleRT) {
				int z = 0;
				while ((z < roleRTSetB.size())
						&& roleRTSetB.get(z).getTypeId() != roleRT.getTypeId()) {
					z++;
				}
				if (z == roleRTSetB.size()) {
					roleRTSetB.add(roleRT);
				}
			}
		}

		// KEEP ONLY ROLES DIFFERENTIATED FROM MOST PROXIMATE
		List<I_RelTuple> roleRTFinal = new ArrayList<I_RelTuple>();
		// ... i.e. keep the roles in A which are NOT present in B
		for (I_RelTuple roleA : roleRTSetA) {
			int z = 0;
			while ((z < roleRTSetB.size())
					&& roleRTSetB.get(z).getTypeId() != roleA.getTypeId()) {
				z++;
			}
			if (z == roleRTSetB.size()) { // A not found in B
				roleRTFinal.add(roleA); // therefore keep
			}
		}

		return roleRTFinal;
	}

	private List<I_RelTuple> findRoleDiffFromProxPrim(ConceptBean cBean,
			List<I_RelTuple> isaList, List<I_Position> posList) {

		// FIND ALL NON-REDUNDANT INHERITED ROLES OF *THIS*CONCEPT*
		List<I_RelTuple> roleRTSetA = findRoleDiffFromRoot(cBean, posList);

		// FIND ROLE SET OF MOST PROXIMATE PRIMITIVE
		List<I_RelTuple> roleRTSetB = new ArrayList<I_RelTuple>();
		for (I_RelTuple isaRT : isaList) {
			ConceptBean isaCB = ConceptBean.get(isaRT.getC2Id());
			List<I_RelTuple> tmpRoleRT = findRoleDiffFromRoot(isaCB, posList);

			// check if already in set b
			for (I_RelTuple roleRT : tmpRoleRT) {
				int z = 0;
				while ((z < roleRTSetB.size())
						&& roleRTSetB.get(z).getTypeId() != roleRT.getTypeId()) {
					z++;
				}
				if (z == roleRTSetB.size()) {
					roleRTSetB.add(roleRT);
				}
			}
		}

		// KEEP ONLY ROLES DIFFERENTIATED FROM MOST PROXIMATE PRIMITIVE
		List<I_RelTuple> roleRTFinal = new ArrayList<I_RelTuple>();
		// ... i.e. keep the roles in A which are NOT present in B
		for (I_RelTuple roleA : roleRTSetA) {
			int z = 0;
			while ((z < roleRTSetB.size())
					&& roleRTSetB.get(z).getTypeId() != roleA.getTypeId()) {
				z++;
			}
			if (z == roleRTSetB.size()) { // A not found in B
				roleRTFinal.add(roleA); // therefore keep
			}
		}

		return roleRTFinal;
	}

	private I_ConceptAttributeTuple findSelf(ConceptBean cBean,
			List<I_Position> posList) {
		try {
			I_ConceptAttributeVersioned cv = cBean.getConceptAttributes();
			List<I_ConceptAttributePart> cvList = cv.getVersions();
			I_ConceptAttributePart cp1 = null;
			for (I_Position pos : posList) {
				int tmpCountDupl = 0;
				for (I_ConceptAttributePart cp : cvList) {
					// FIND MOST RECENT
					if (cp.getPathId() == pos.getPath().getConceptId()) {
						if (cp1 == null) {
							cp1 = cp; // ... KEEP FIRST_INSTANCE PART
						} else if (cp1.getVersion() < cp.getVersion()) {
							cp1 = cp; // ... KEEP MORE_RECENT PART
						} else if (cp1.getVersion() == cp.getVersion()) {
							// !!! THIS DUPLICATE SHOULD NEVER HAPPEN
							tmpCountDupl++;
						}
					}
				}
				// cp1.getStatusId() == isCURRENT
				if (cp1 != null) { // IF FOUND ON THIS PATH, STOP SEARCHING
					// VERIFICATION STATISTICS
					if (tmpCountDupl > 1) {
						countFindSelfDuplPart++;
						countFindSelfDuplPartGE2++;
					} else if (tmpCountDupl == 1) {
						countFindSelfDuplPart++;
					}
					return new ThinConTuple(cv, cp1);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return null;
	}

	private boolean isCDefined(ConceptBean cBean, List<I_Position> posList) {

		try {
			I_ConceptAttributeVersioned cv = cBean.getConceptAttributes();
			List<I_ConceptAttributePart> cvList = cv.getVersions();
			I_ConceptAttributePart cp1 = null;
			for (I_Position pos : posList) {
				int tmpCountDupl = 0;
				for (I_ConceptAttributePart cp : cvList) {
					// FIND MOST RECENT
					if (cp.getPathId() == pos.getPath().getConceptId()) {
						if (cp1 == null) {
							cp1 = cp; // ... KEEP FIRST_INSTANCE, CURRENT PART
						} else if (cp1.getVersion() < cp.getVersion()) {
							cp1 = cp; // ... KEEP MORE_RECENT, CURRENT PART
						} else if (cp1.getVersion() == cp.getVersion()) {
							// !!! THIS DUPLICATE SHOULD NEVER HAPPEN
							tmpCountDupl++;
						}
					}
				}
				if (cp1 != null) { // IF FOUND ON THIS PATH, STOP SEARCHING
					// VERIFICATION STATISTICS
					if (tmpCountDupl > 1) {
						countIsCDefinedDuplPart++;
						countIsCDefinedDuplPartGE2++;
					} else if (tmpCountDupl == 1) {
						countIsCDefinedDuplPart++;
					}
					return cp1.isDefined();
				}
			}
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// EXCEPTION --> cBean.getConceptAttributes() FAILED
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * <b>Authoring Normal Form</b><li>Most Proximate Supertypes (IS-A)</li>
	 */
	public JPanel newFormAuthJPanel(String label, I_Position p,
			I_ConfigAceFrame config,
			Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
			Map<I_DescriptionTuple, Color> desColorMap,
			Map<I_RelTuple, Color> relColorMap) throws IOException {
		JPanel formJPanel = newMinMaxJPanel();
		formJPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		Set<I_Position> posSet = new HashSet<I_Position>(1);
		posSet.add(p);

		List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
		c.gridx = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.NORTHWEST;

		// SHOW SELF CONCEPT
		I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
		I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabelForm(cTuple,
				showDetailCB.isSelected(), showStatusCB.isSelected());
		tLabelList.add((LabelForTuple) tmpTLabel);
		Color tmpDeltaColor = conAttrColorMap.get(cTuple);
		setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
		formJPanel.add(tmpTLabel.getLabel(), c);
		c.gridy++;

		// SHOW PROXIMAL ISAs -- as relationships
		List<I_RelTuple> isaList = findIsaProximal(theCBean, cClassPathPos);
		for (I_RelTuple t : isaList) {
			I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t,
					showDetailCB.isSelected(), showStatusCB.isSelected());
			tLabelList.add((LabelForTuple) tLabel);
			Color deltaColor = relColorMap.get(t);
			setBorder(tLabel.getLabel(), deltaColor);
			formJPanel.add(tLabel.getLabel(), c);
			c.gridy++;
		}

		// FIND NON-REDUNDANT ROLES, DIFFERENTIATED FROM PROXIMATE ISA
		List<I_RelTuple> roleList = findRoleDiffFromProx(theCBean, isaList,
				cClassPathPos);
		// SHOW ROLE SET
		for (I_RelTuple t : roleList) {
			I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t,
					showDetailCB.isSelected(), showStatusCB.isSelected());
			tLabelList.add((LabelForTuple) tLabel);
			Color deltaColor = relColorMap.get(t);
			setBorder(tLabel.getLabel(), deltaColor);
			formJPanel.add(tLabel.getLabel(), c);
			c.gridy++;
		}

		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = 2;
		formJPanel.add(new JPanel(), c);
		formJPanel.setBorder(BorderFactory.createTitledBorder(label));

		return formJPanel;
	}

	/**
	 * <b>Distribution Normal Form</b><li>Most Proximate Supertypes (IS-A)</li>
	 */
	public JPanel newFormDistJPanel(String label, I_Position p,
			I_ConfigAceFrame config,
			Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
			Map<I_DescriptionTuple, Color> desColorMap,
			Map<I_RelTuple, Color> relColorMap) throws IOException {
		JPanel formJPanel = newMinMaxJPanel();
		formJPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		Set<I_Position> posSet = new HashSet<I_Position>(1);
		posSet.add(p);

		List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
		c.gridx = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.NORTHWEST;

		// SHOW SELF CONCEPT
		I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
		I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabelForm(cTuple,
				showDetailCB.isSelected(), showStatusCB.isSelected());
		tLabelList.add((LabelForTuple) tmpTLabel);
		Color tmpDeltaColor = conAttrColorMap.get(cTuple);
		setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
		formJPanel.add(tmpTLabel.getLabel(), c);
		c.gridy++;

		// SHOW PROXIMAL ISAs -- as relationships
		List<I_RelTuple> relList = findIsaProximal(theCBean, cClassPathPos);
		for (I_RelTuple rTuple : relList) {
			tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB
					.isSelected(), showStatusCB.isSelected());
			tLabelList.add((LabelForTuple) tmpTLabel);
			tmpDeltaColor = relColorMap.get(rTuple);
			setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
			formJPanel.add(tmpTLabel.getLabel(), c);
			c.gridy++;
		}

		// SHOW ROLES, NON-REDUNDANT, DIFFERENTIATED FROM ROOT
		relList = findRoleDiffFromRoot(theCBean, cClassPathPos);
		for (I_RelTuple rTuple : relList) {
			tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB
					.isSelected(), showStatusCB.isSelected());
			tLabelList.add((LabelForTuple) tmpTLabel);
			tmpDeltaColor = relColorMap.get(rTuple);
			setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
			formJPanel.add(tmpTLabel.getLabel(), c);
			c.gridy++;
		}

		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = 2;
		formJPanel.add(new JPanel(), c);
		formJPanel.setBorder(BorderFactory.createTitledBorder(label));

		return formJPanel;
	}

	/**
	 * <b>Long Canonical Form</b><li>Most Proximate PRIMITIVE Supertypes (IS-A)</li>
	 * 
	 */
	public JPanel newFormLongJPanel(String label, I_Position p,
			I_ConfigAceFrame config,
			Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
			Map<I_DescriptionTuple, Color> desColorMap,
			Map<I_RelTuple, Color> relColorMap) throws IOException {
		JPanel formJPanel = newMinMaxJPanel();
		formJPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		Set<I_Position> posSet = new HashSet<I_Position>(1);
		posSet.add(p);

		List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
		c.gridx = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.NORTHWEST;

		// SHOW SELF CONCEPT
		I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
		I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabel(cTuple,
				showDetailCB.isSelected(), showStatusCB.isSelected());
		tLabelList.add((LabelForTuple) tmpTLabel);
		Color tmpDeltaColor = conAttrColorMap.get(cTuple);
		setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
		formJPanel.add(tmpTLabel.getLabel(), c);
		c.gridy++;

		// SHOW PROXIMAL ISAs -- as relationships
		List<I_RelTuple> relList = findIsaProximalPrim(theCBean, cClassPathPos);
		for (I_RelTuple rTuple : relList) {
			tmpTLabel = TermLabelMaker.newLabel(rTuple, showDetailCB
					.isSelected(), showStatusCB.isSelected());
			tLabelList.add((LabelForTuple) tmpTLabel);
			tmpDeltaColor = relColorMap.get(rTuple);
			setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
			formJPanel.add(tmpTLabel.getLabel(), c);
			c.gridy++;
		}

		// SHOW ROLES, NON-REDUNDANT, DIFFERENTIATED FROM ROOT
		relList = findRoleDiffFromRoot(theCBean, cClassPathPos);
		for (I_RelTuple rTuple : relList) {
			tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB
					.isSelected(), showStatusCB.isSelected());
			tLabelList.add((LabelForTuple) tmpTLabel);
			tmpDeltaColor = relColorMap.get(rTuple);
			setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
			formJPanel.add(tmpTLabel.getLabel(), c);
			c.gridy++;
		}

		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = 2;
		formJPanel.add(new JPanel(), c);
		formJPanel.setBorder(BorderFactory.createTitledBorder(label));

		return formJPanel;
	}

	/**
	 * <b>Short Canonical Form</b><li>Most Proximate PRIMITIVE Supertypes (IS-A)
	 * </li>
	 */
	public JPanel newFormShortJPanel(String label, I_Position p,
			I_ConfigAceFrame config,
			Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
			Map<I_DescriptionTuple, Color> desColorMap,
			Map<I_RelTuple, Color> relColorMap) throws IOException {
		JPanel formJPanel = newMinMaxJPanel();
		formJPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		Set<I_Position> posSet = new HashSet<I_Position>(1);
		posSet.add(p);

		List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
		c.gridx = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.NORTHWEST;

		// SHOW SELF CONCEPT
		I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
		I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabelForm(cTuple,
				showDetailCB.isSelected(), showStatusCB.isSelected());
		tLabelList.add((LabelForTuple) tmpTLabel);
		Color tmpDeltaColor = conAttrColorMap.get(cTuple);
		setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
		formJPanel.add(tmpTLabel.getLabel(), c);
		c.gridy++;

		// SHOW PROXIMAL PRIMITIVE ISAs -- as relationships
		List<I_RelTuple> isaList = findIsaProximalPrim(theCBean, cClassPathPos);
		for (I_RelTuple t : isaList) {
			I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t,
					showDetailCB.isSelected(), showStatusCB.isSelected());
			tLabelList.add((LabelForTuple) tLabel);
			Color deltaColor = relColorMap.get(t);
			setBorder(tLabel.getLabel(), deltaColor);
			formJPanel.add(tLabel.getLabel(), c);
			c.gridy++;
		}

		// SHOW ROLES
		List<I_RelTuple> roleList = findRoleDiffFromProxPrim(theCBean, isaList,
				cClassPathPos);
		for (I_RelTuple t : roleList) {
			I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t,
					showDetailCB.isSelected(), showStatusCB.isSelected());
			tLabelList.add((LabelForTuple) tLabel);
			Color deltaColor = relColorMap.get(t);
			setBorder(tLabel.getLabel(), deltaColor);
			formJPanel.add(tLabel.getLabel(), c);
			c.gridy++;
		}

		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = 2;
		formJPanel.add(new JPanel(), c);
		formJPanel.setBorder(BorderFactory.createTitledBorder(label));

		return formJPanel;
	}

	public JPanel newFormStatedJPanel(String label, I_Position p,
			I_ConfigAceFrame config,
			Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
			Map<I_DescriptionTuple, Color> desColorMap,
			Map<I_RelTuple, Color> relColorMap) throws IOException {
		JPanel formJPanel = newMinMaxJPanel();
		formJPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		Set<I_Position> posSet = new HashSet<I_Position>(1);
		posSet.add(p);

		List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
		c.gridx = 0;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.NORTHWEST;

		// concept attributes
		List<I_ConceptAttributeTuple> conTuples = this.theCBean
				.getConceptAttributeTuples(config.getAllowedStatus(), posSet);
		for (I_ConceptAttributeTuple t : conTuples) {
			I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t,
					showDetailCB.isSelected(), showStatusCB.isSelected());
			tLabelList.add((LabelForTuple) tLabel);
			Color deltaColor = conAttrColorMap.get(t);
			setBorder(tLabel.getLabel(), deltaColor);
			formJPanel.add(tLabel.getLabel(), c);
			c.gridy++;
		}

		// rels
		List<I_RelTuple> relList = this.theCBean.getSourceRelTuples(config
				.getAllowedStatus(), null, posSet, false);
		for (I_RelTuple t : relList) {
			I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t,
					showDetailCB.isSelected(), showStatusCB.isSelected());
			tLabelList.add((LabelForTuple) tLabel);
			Color deltaColor = relColorMap.get(t);
			setBorder(tLabel.getLabel(), deltaColor);
			formJPanel.add(tLabel.getLabel(), c);
			c.gridy++;
		}
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = 2;
		formJPanel.add(new JPanel(), c);
		formJPanel.setBorder(BorderFactory.createTitledBorder(label));
		return formJPanel;
	}

	public String statsToHtml() {
		StringBuilder markup = new StringBuilder(256);

		markup.append("<font face=\"monospace\">");
		markup.append("<br>");
		markup.append("<br>countFindIsaProxDuplPart=\t"
				+ countFindIsaProxDuplPart);
		markup.append("<br>countFindRoleProxDuplPart=\t"
				+ countFindRoleProxDuplPart);
		markup.append("<br>countFindSelfDuplPart=\t" + countFindSelfDuplPart);
		markup.append("<br>countIsCDefinedDuplPart=\t"
				+ countIsCDefinedDuplPart);
		markup.append("<br>countFindIsaProxDuplPartGE2=\t"
				+ countFindIsaProxDuplPartGE2);
		markup.append("<br>countFindRoleProxDuplPartGE2=\t"
				+ countFindRoleProxDuplPartGE2);
		markup.append("<br>countFindSelfDuplPartGE2=\t"
				+ countFindSelfDuplPartGE2);
		markup.append("<br>countIsCDefinedDuplPartGE2=\t"
				+ countIsCDefinedDuplPartGE2);
		return markup.toString();
	}

	public String statsToString() {
		StringBuilder markup = new StringBuilder(256);

		markup.append("\r\n::: [LogicalFormsPanel]");
		markup.append("\r\n:::");
		markup.append("\r\n:::countFindIsaProxDuplPart=\t"
				+ countFindIsaProxDuplPart);
		markup.append("\r\n:::countFindRoleProxDuplPart=\t"
				+ countFindRoleProxDuplPart);
		markup
				.append("\r\n:::countFindSelfDuplPart=\t"
						+ countFindSelfDuplPart);
		markup.append("\r\n:::countIsCDefinedDuplPart=\t"
				+ countIsCDefinedDuplPart);
		markup.append("\r\n:::countFindIsaProxDuplPartGE2=\t"
				+ countFindIsaProxDuplPartGE2);
		markup.append("\r\n:::countFindRoleProxDuplPartGE2=\t"
				+ countFindRoleProxDuplPartGE2);
		markup.append("\r\n:::countFindSelfDuplPartGE2=\t"
				+ countFindSelfDuplPartGE2);
		markup.append("\r\n:::countIsCDefinedDuplPartGE2=\t"
				+ countIsCDefinedDuplPartGE2);
		return markup.toString();
	}

	public void statsReset() {
		countFindIsaProxDuplPart = 0;
		countFindRoleProxDuplPart = 0;
		countFindSelfDuplPart = 0;
		countIsCDefinedDuplPart = 0;
		countFindIsaProxDuplPartGE2 = 0;
		countFindRoleProxDuplPartGE2 = 0;
		countFindSelfDuplPartGE2 = 0;
		countIsCDefinedDuplPartGE2 = 0;
	}

}
