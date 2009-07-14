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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.dwfa.ace.I_ImplementActiveLabel;
import org.dwfa.ace.LabelForTuple;
import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Position;

import com.sleepycat.je.DatabaseException;

public class LogicalFormsPanel extends JPanel implements ActionListener {

	   public static class ConflictColors {
	      private List<Color> differenceColors = new ArrayList<Color>();

	      int currentColor = 0;

	      public ConflictColors() {
	         super();
	         // Link for colors
	         // http://www.w3schools.com/html/html_colornames.asp
	         differenceColors.add(new Color(0x5F9EA0));
	         differenceColors.add(new Color(0x7FFF00));
	         differenceColors.add(new Color(0xD2691E));
	         differenceColors.add(new Color(0x6495ED));
	         differenceColors.add(new Color(0xDC143C));
	         differenceColors.add(new Color(0xB8860B));
	         differenceColors.add(new Color(0xFF8C00));
	         differenceColors.add(new Color(0x8FBC8F));
	         differenceColors.add(new Color(0x483D8B));
	         differenceColors.add(new Color(0x1E90FF));
	         differenceColors.add(new Color(0xFFD700));
	         differenceColors.add(new Color(0xF0E68C));
	         differenceColors.add(new Color(0x90EE90));
	         differenceColors.add(new Color(0x8470FF));
	      }

	      public Color getColor() {
	         if (currentColor == differenceColors.size()) {
	            reset();
	         }
	         return differenceColors.get(currentColor++);
	      }

	      public void reset() {
	         currentColor = 0;
	      }
	   }

	   /**
	    * 
	    */
	   private static final long serialVersionUID = 1L;

	   private ConflictColors colors = new ConflictColors();

	   private JPanel commonPanel;

	   private JPanel differencePanel;

	   private JPanel formsPanel;

	   private Dimension maxPartPanelSize = new Dimension(TermLabelMaker.LABEL_WIDTH + 20, 4000);

	   private Dimension minPartPanelSize = new Dimension(TermLabelMaker.LABEL_WIDTH + 20, 100);

	   private JCheckBox showStatus = new JCheckBox("status");

	   private JCheckBox longForm = new JCheckBox("long form");

	   private ConceptBean cb;

	   private I_ConfigAceFrame config;

	   private List<I_ImplementActiveLabel> commonLabels;

	   private JPanel commonPartPanel;

	   public LogicalFormsPanel() {
	      super();
	      initWithGridBagLayout();
	      setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3), BorderFactory
	            .createLineBorder(Color.GRAY)));
	      showStatus.setSelected(true);
	      showStatus.addActionListener(this);
	      longForm.addActionListener(this);

	   }


	   private void setMinAndMax(JPanel panel) {
	      panel.setMinimumSize(minPartPanelSize);
	      panel.setMaximumSize(maxPartPanelSize);
	   }

	   private JPanel getMinAndMaxPanel() {
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
	      setMinAndMax(p);
	      return p;
	   }

	   private void initWithGridBagLayout() {
	      setLayout(new GridBagLayout());
	      GridBagConstraints c = new GridBagConstraints();
	      c.anchor = GridBagConstraints.NORTHWEST;
	      c.gridx = 0;
	      c.gridy = 0;
	      c.fill = GridBagConstraints.NONE;
	      c.weightx = 0.0;
	      c.weighty = 0.0;
	      c.gridwidth = 1;
	      c.gridy++;
	      JLabel label = new JLabel("Logical Forms:");
	      label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
	      add(label, c);
	      c.gridx++;
	      add(longForm, c);
	      c.gridx++;
	      add(showStatus, c);
	      c.gridy++;
	      c.gridx = 0;
	      c.gridwidth = 2;
	      c.fill = GridBagConstraints.BOTH;
	      c.weightx = 1.0;

	      commonPanel = getMinAndMaxPanel();
	      commonPanel.setLayout(new GridBagLayout());
	      commonPanel.setName("resolutionPanel");
	      commonPanel.setBorder(BorderFactory.createTitledBorder("Common: "));
	      add(commonPanel, c);
	      c.gridx = c.gridx + c.gridwidth;
	      differencePanel = getMinAndMaxPanel();
	      differencePanel.setLayout(new GridLayout(0, 1));
	      differencePanel.setName("difference panel");
	      differencePanel.setBorder(BorderFactory.createTitledBorder("Differences: "));
	      add(differencePanel, c);
	      c.gridy++;
	      c.gridx = 0;
	      c.gridwidth = 4;
	      formsPanel = new JPanel(new GridBagLayout());
	      formsPanel.setName("versionPanel");
	      formsPanel.setBorder(BorderFactory.createTitledBorder("Forms: "));
	      JScrollPane differenceScroller = new JScrollPane(formsPanel);
	      differenceScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	      add(differenceScroller, c);
	   }

	   public void setConcept(ConceptBean cb, I_ConfigAceFrame config) throws IOException {
	      this.cb = cb;
	      this.config = config;
	      commonPanel.removeAll();
	      differencePanel.removeAll();
	      formsPanel.removeAll();
	      if (cb != null) {
	         commonLabels = getCommonLabels(longForm.isSelected(), showStatus.isSelected(), config);
	          commonPartPanel = new JPanel();
	          setMinAndMax(commonPartPanel);
	          commonPartPanel.setLayout(new BoxLayout(commonPartPanel, BoxLayout.Y_AXIS));
	          for (I_ImplementActiveLabel l : commonLabels) {
	             commonPartPanel.add(l.getLabel());
	          }

	         GridBagConstraints resolutionConstraints = new GridBagConstraints();
	         resolutionConstraints.anchor = GridBagConstraints.SOUTHEAST;
	         resolutionConstraints.fill = GridBagConstraints.NONE;
	         resolutionConstraints.gridheight = 1;
	         resolutionConstraints.gridwidth = 1;
	         resolutionConstraints.gridx = 0;
	         resolutionConstraints.gridy = 1;
	         resolutionConstraints.weightx = 1;
	         resolutionConstraints.weighty = 1;

	         resolutionConstraints.anchor = GridBagConstraints.NORTHWEST;
	         resolutionConstraints.gridx = 0;
	         resolutionConstraints.gridy = 0;
	         resolutionConstraints.weighty = 0;
	         commonPanel.add(commonPartPanel, resolutionConstraints);

	         Map<I_ConceptAttributeTuple, Color> conAttrColorMap = new HashMap<I_ConceptAttributeTuple, Color>();
	         Map<I_DescriptionTuple, Color> desColorMap = new HashMap<I_DescriptionTuple, Color>();
	         Map<I_RelTuple, Color> relColorMap = new HashMap<I_RelTuple, Color>();
	         colors.reset();
	         Collection<I_ImplementActiveLabel> conflictingLabels = getConflictingLabels(longForm.isSelected(), showStatus
	               .isSelected(), config, colors, conAttrColorMap, desColorMap, relColorMap);
	         JPanel conflictPartPanel = new JPanel();
	         conflictPartPanel.setLayout(new BoxLayout(conflictPartPanel, BoxLayout.Y_AXIS));
	         for (I_ImplementActiveLabel l : conflictingLabels) {
	            conflictPartPanel.add(l.getLabel());
	         }
	         differencePanel.add(conflictPartPanel);

	         GridBagConstraints c = new GridBagConstraints();
	         c.fill = GridBagConstraints.VERTICAL;
	         c.anchor = GridBagConstraints.NORTHWEST;
	         c.weightx = 0;
	         c.weighty = 0;
	         c.gridx = 0;
	         c.gridy = 0;

	         I_Path path;
			try {
				path = ((VodbEnv) LocalVersionedTerminology.get()).getPath(config.getClassifierInputPath().getConceptId());
			} catch (DatabaseException e) {
				throw new ToIoException(e);
			}
	         I_Position p = new Position(Integer.MAX_VALUE, path);
	        	 
            JPanel statePanel = getVersionView("Stated form:", p, config, conAttrColorMap, desColorMap, relColorMap);
            setMinAndMax(statePanel);
            formsPanel.add(statePanel, c);
            c.gridx++;
            if (c.gridx == 2) {
               c.gridx = 0;
               c.gridy++;
            }
	            
	         try {
				path = ((VodbEnv) LocalVersionedTerminology.get()).getPath(config.getClassifierOutputPath().getConceptId());
			} catch (DatabaseException e) {
				throw new ToIoException(e);
			}
	         p = new Position(Integer.MAX_VALUE, path);
	        	 
            statePanel = getVersionView("Distribution normal form:", p, config, conAttrColorMap, desColorMap, relColorMap);
            setMinAndMax(statePanel);
            formsPanel.add(statePanel, c);
            c.gridx++;
            if (c.gridx == 2) {
               c.gridx = 0;
               c.gridy++;
            }
	         
	         c.weightx = 1.0;
	         c.fill = GridBagConstraints.HORIZONTAL;
	         formsPanel.add(new JPanel(), c);
	      }
	   }

	   public void actionPerformed(ActionEvent e) {
	      try {
	         setConcept(cb, config);
	         revalidate();
	      } catch (IOException e1) {
	         AceLog.getAppLog().alertAndLog(this, Level.SEVERE, "Database Exception: " + e1.getLocalizedMessage(), e1);
	      }
	   }

	   public List<I_ImplementActiveLabel> getCommonLabels(boolean showLongForm, boolean showStatus, I_ConfigAceFrame config)
	         throws IOException {
	      List<I_ImplementActiveLabel> labelList = new ArrayList<I_ImplementActiveLabel>();

	      // concept attributes
	      Set<I_ConceptAttributeTuple> commonConceptAttributes = this.cb.getCommonConceptAttributeTuples(config);
	      if (commonConceptAttributes != null) {
	         for (I_ConceptAttributeTuple t : commonConceptAttributes) {
	            I_ImplementActiveLabel conAttrLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
	            setBorder(conAttrLabel.getLabel(), null);
	            labelList.add(conAttrLabel);
	         }
	      }

	      // src relationships
	      Set<I_RelTuple> commonRelTuples = this.cb.getCommonRelTuples(config);
	      if (commonRelTuples != null) {
	         for (I_RelTuple t : commonRelTuples) {
	            I_ImplementActiveLabel relLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
	            setBorder(relLabel.getLabel(), null);
	            labelList.add(relLabel);
	         }
	      }

	      return labelList;
	   }

	   private void setBorder(JLabel tLabel, Color conflictColor) {
	      if (conflictColor == null) {
	         conflictColor = Color.white;
	      }
	      Dimension size = tLabel.getSize();
	      tLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory
	            .createCompoundBorder(BorderFactory.createMatteBorder(1, 5, 1, 5, conflictColor), BorderFactory
	                  .createEmptyBorder(1, 3, 1, 3))));
	      size.width = size.width + 18;
	      size.height = size.height + 6;
	      tLabel.setSize(size);
	      tLabel.setPreferredSize(size);
	      tLabel.setMaximumSize(size);
	      tLabel.setMinimumSize(size);
	   }

	   public Collection<I_ImplementActiveLabel> getConflictingLabels(boolean showLongForm, boolean showStatus,
	         I_ConfigAceFrame config, ConflictColors colors, Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
	         Map<I_DescriptionTuple, Color> descColorMap, Map<I_RelTuple, Color> relColorMap) throws IOException {

	      Set<I_ConceptAttributeTuple> allConAttrTuples = new HashSet<I_ConceptAttributeTuple>();
	      Set<I_RelTuple> allRelTuples = new HashSet<I_RelTuple>();

	      for (I_Position p : config.getViewPositionSet()) {
	         Set<I_Position> positionSet = new HashSet<I_Position>();
	         positionSet.add(p);

	         // concept attributes
	         List<I_ConceptAttributeTuple> conAttrTuplesForPosition = this.cb.getConceptAttributeTuples(config
	               .getAllowedStatus(), positionSet, false);
	         allConAttrTuples.addAll(conAttrTuplesForPosition);

	         // relationships
	         List<I_RelTuple> relTuplesForPosition = this.cb.getSourceRelTuples(config.getAllowedStatus(), null,
	               positionSet, false);
	         allRelTuples.addAll(relTuplesForPosition);
	      }

	      Set<I_ConceptAttributeTuple> commonConAttrTuples = this.cb.getCommonConceptAttributeTuples(config);
	      allConAttrTuples.removeAll(commonConAttrTuples);

	      Set<I_RelTuple> commonRelTuples = this.cb.getCommonRelTuples(config);
	      allRelTuples.removeAll(commonRelTuples);

	      Collection<I_ImplementActiveLabel> labelList = new ArrayList<I_ImplementActiveLabel>();

	      for (I_ConceptAttributeTuple t : allConAttrTuples) {
	         I_ImplementActiveLabel conAttrLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
	         Color conflictColor = colors.getColor();
	         conAttrColorMap.put(t, conflictColor);
	         setBorder(conAttrLabel.getLabel(), conflictColor);
	         labelList.add(conAttrLabel);
	      }
	      for (I_RelTuple t : allRelTuples) {
	         I_ImplementActiveLabel relLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
	         Color conflictColor = colors.getColor();
	         relColorMap.put(t, conflictColor);
	         setBorder(relLabel.getLabel(), conflictColor);
	         labelList.add(relLabel);
	      }

	      return labelList;
	   }

	   public JPanel getVersionView(String label, I_Position p, I_ConfigAceFrame config,
	         Map<I_ConceptAttributeTuple, Color> conAttrColorMap, Map<I_DescriptionTuple, Color> desColorMap,
	         Map<I_RelTuple, Color> relColorMap) throws IOException {
	      JPanel versionView = getMinAndMaxPanel();
	      versionView.setLayout(new GridBagLayout());
	      GridBagConstraints c = new GridBagConstraints();
	      c.fill = GridBagConstraints.NONE;
	      c.anchor = GridBagConstraints.NORTHWEST;
	      c.weightx = 0;
	      c.weighty = 0;
	      c.gridx = 0;
	      c.gridy = 0;
	      Set<I_Position> posSet = new HashSet<I_Position>(1);
	      posSet.add(p);

	      List<LabelForTuple> tuples = new ArrayList<LabelForTuple>();
	      c.gridx = 0;
	      c.gridwidth = 2;
	      c.anchor = GridBagConstraints.NORTHWEST;

	      // concept attributes
	      List<I_ConceptAttributeTuple> conAttrList = this.cb.getConceptAttributeTuples(config.getAllowedStatus(), posSet);
	      for (I_ConceptAttributeTuple t : conAttrList) {
	         I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t, false, false);
	         tuples.add((LabelForTuple) tLabel);
	         Color conflictColor = conAttrColorMap.get(t);
	         setBorder(tLabel.getLabel(), conflictColor);
	         versionView.add(tLabel.getLabel(), c);
	         c.gridy++;
	      }

	      // rels
	      List<I_RelTuple> relList = this.cb.getSourceRelTuples(config.getAllowedStatus(), null, posSet, false);
	      for (I_RelTuple t : relList) {
	         I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t, false, false);
	         tuples.add((LabelForTuple) tLabel);
	         Color conflictColor = relColorMap.get(t);
	         setBorder(tLabel.getLabel(), conflictColor);
	         versionView.add(tLabel.getLabel(), c);
	         c.gridy++;
	      }
	      c.weightx = 1.0;
	      c.weighty = 1.0;
	      c.gridwidth = 2;
	      versionView.add(new JPanel(), c);
	      versionView.setBorder(BorderFactory.createTitledBorder(label));
	      return versionView;
	   }

	}
