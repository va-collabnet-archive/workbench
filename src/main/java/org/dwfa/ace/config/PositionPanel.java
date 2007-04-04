package org.dwfa.ace.config;



import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.bpa.gui.GridBagPanel;
import org.dwfa.bpa.gui.glue.PropertySetListenerGlue;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;

import com.sleepycat.je.DatabaseException;

/**
 * @author kec
 * 
 */
public class PositionPanel extends GridBagPanel implements ChangeListener,
        ItemListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private JSlider coarseControl;

    private JSlider fineControl;

    private JCheckBox selectPositionCheckBox;

    private JCheckBox editOnPathCheckBox;

    private SimpleDateFormat dateFormatter;

    private List<Date> dates;

    private List<String> positionStrings;

    private int fineControlSize = 10;

    private I_Path path;

    private Position position;

    private boolean selectPositionOnly = false;

	private Font monoSpaceFont;

	private PropertySetListenerGlue editGlue;

	private PropertySetListenerGlue selectGlue;

	private I_ConfigAceFrame aceConfig;

    private class Setup implements Runnable {
        public void run() {
            try {
            	dates = new ArrayList<Date>();
            	for (TimePathId tp: AceConfig.vodb.getTimePathList()) {
            		if (tp.getPathId() == path.getConceptId()) {
            			dates.add(new Date(ThinVersionHelper.convert(tp.getTime())));
            		}
            		
            	}
                if (PositionPanel.this.dates.size() > 1) {
                    Collections.sort(PositionPanel.this.dates);
                }
                if (selectPositionOnly == false) {
                    PositionPanel.this.dates.add(null); // For the top "latest"
                    // position...
                }
                AceLog.info("Processing path: "
                        + ConceptBean.get(path.getConceptId()).getInitialText() +
                        " with " + dates.size()
                        + " coordinates");
                PositionPanel.this.positionStrings = new ArrayList<String>();
                for (Iterator<Date> sortedDateItr = PositionPanel.this.dates
                        .iterator(); sortedDateItr.hasNext();) {
                    Date date = sortedDateItr.next();
                    if (date != null) {
                        if (date.after(new Date(Long.MIN_VALUE))) {
                            PositionPanel.this.positionStrings
                                    .add(PositionPanel.this.dateFormatter
                                            .format(date));
                        } else {
                            PositionPanel.this.positionStrings
                                    .add("Beginning of time");
                        }

                    } else {
                        PositionPanel.this.positionStrings.add("latest");
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        try {
                            GridBagConstraints c = new GridBagConstraints();
                            c.fill = GridBagConstraints.HORIZONTAL;
                            c.anchor = GridBagConstraints.NORTHWEST;

                            JLabel pathLabel = new JLabel(Path
                                    .toHtmlString(path));
                            pathLabel.setBorder(BorderFactory
                                    .createMatteBorder(0, 0, 2, 0, Color.GRAY));
                            c.gridheight = 1;
                            c.gridwidth = 2;
                            c.gridx = 0;
                            c.gridy = 0;
                            PositionPanel.this.add(pathLabel, c);

                            c.fill = GridBagConstraints.NONE;
                            c.gridwidth = 1;
                            c.gridy++;
                            JComponent sliderPanel = setupSliderPanel();
                            c.weightx = 0;
                            // this.add(new JScrollPane(sliderPanel), c);
                            PositionPanel.this.add(sliderPanel, c);
                            c.weightx = 1;
                            c.gridx = 1;
                            c.fill = GridBagConstraints.BOTH;
                            addFiller(c);
                            c.gridy++;
                            c.gridx = 0;
                            c.weighty = 1;
                            addFiller(c);
                            // this.setBorder(BorderFactory.createTitledBorder("PositionPanel"));
                        } catch (Exception ex) {
                			AceLog.alertAndLogException(ex);
                        }
                    }
                });

            } catch (Exception ex) {
    			AceLog.alertAndLogException(ex);
            }
        }
    }

    /**
     * @param config
     * @throws QueryException
     * @throws RemoteException
     * 
     */
    public PositionPanel(I_Path path,
            boolean selectPositionOnly, String purpose,
            String name, I_ConfigAceFrame aceConfig, PropertySetListenerGlue selectGlue) throws DatabaseException {
        super(new GridBagLayout(), name, null);
        Font defaultFont = new JLabel().getFont();
       	monoSpaceFont = new Font("Monospaced", defaultFont.getStyle(), defaultFont.getSize());

        this.selectPositionOnly = selectPositionOnly;
        this.aceConfig = aceConfig;
        this.dateFormatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        this.path = path;
        this.selectPositionCheckBox = new JCheckBox("Use position "
                + purpose);
        this.selectPositionCheckBox.setSelected(false);
        this.editOnPathCheckBox = new JCheckBox("Edit on this path");
        this.editOnPathCheckBox.setSelected(aceConfig.getEditingPathSet().contains(path));
        this.selectGlue = selectGlue;
        this.editGlue = new PropertySetListenerGlue("removeEditingPath",
        	    "addEditingPath", "replaceEditingPath", "getEditingPathSet",
                I_Path.class, aceConfig);
        new Thread(new Setup(), "PositionPanel Setup").start();
        Dimension size = new Dimension(330, 310);
        setSize(size);
        setPreferredSize(size);
        setMinimumSize(size);
       
    }

    /**
     * @param c
     */
    private void addFiller(GridBagConstraints c) {
        JLabel fillerLabel = new JLabel();
        //fillerLabel.setBorder(BorderFactory.createLineBorder(Color.red));
        this.add(fillerLabel, c);
    }

    /**
     * @param c
     * @throws QueryException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws SecurityException
     */
    private JComponent setupSliderPanel() throws
            SecurityException, IllegalArgumentException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
    	int coarseLabelInset = 8;
        JPanel sliderPanel = new JPanel(new GridBagLayout());
        //sliderPanel.setBorder(BorderFactory.createLineBorder(Color.green));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.gridheight = 1;
        if (this.selectPositionOnly) {
            if (dates.size() > 0) {
                this.selectPositionCheckBox.addItemListener(this);
                sliderPanel.add(this.selectPositionCheckBox, c);
                c.gridy++;
            }
        } else {
            if (this.path != null) {
                this.editOnPathCheckBox.setSelected(aceConfig.getEditingPathSet().contains(path));
                this.editOnPathCheckBox.addItemListener(this);
                sliderPanel.add(this.editOnPathCheckBox, c);
                c.gridy++;
            }
            this.selectPositionCheckBox.addItemListener(this);
            c.gridy++;
            sliderPanel.add(this.selectPositionCheckBox, c);
        }
        c.gridy++;
        JPanel fill = new JPanel();
        fill.setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 1));
        sliderPanel.add(fill, c);
        // Create the slider
        if (dates.size() > 0) {
            this.coarseControl = new JSlider(JSlider.VERTICAL, 0,
                    dates.size() - 1, dates.size() - 1);
            //coarseControl.setBorder(BorderFactory.createLineBorder(Color.green));
            coarseControl.addChangeListener(this);
            coarseControl.setMajorTickSpacing(10);
            coarseControl.setPaintTicks(false);

            coarseControl.setValue(coarseControl.getMaximum());

            c.weightx = 0.0;
            c.gridheight = 3;
            c.gridwidth = 1;
            c.fill = GridBagConstraints.VERTICAL;
            c.gridx = 0;
            c.gridy++;
            sliderPanel.add(this.coarseControl, c);

            c.gridheight = 1;
            c.gridx++;
            int dateOffset = 2;
            if ((this.selectPositionOnly) || (dates.size() == 1)) {
                dateOffset = 1;
            }
            Date dateForLabel = dates.get(dates.size() - dateOffset);
            String dateForLabelStr = "None";
            if (dateForLabel != null) {
                if (dateForLabel.after(new Date(Long.MIN_VALUE))) {
                    dateForLabelStr = this.dateFormatter.format(dateForLabel);
                } else {
                    dateForLabelStr = "Beginning of time";
                }
            }
            JLabel lastChange = new JLabel("<html>Last change: " + dateForLabelStr);
            lastChange.setBorder(BorderFactory.createEmptyBorder(coarseLabelInset, 0, 0, 0));
            lastChange.setFont(monoSpaceFont);
            sliderPanel.add(lastChange, c);
            if (this.coarseControl.getMaximum()
                    - this.coarseControl.getMinimum() < 10) {
                this.fineControl = new JSlider(JSlider.VERTICAL, 0,
                        this.coarseControl.getMaximum(), this.coarseControl
                                .getValue());
                this.fineControlSize = this.coarseControl.getMaximum();
            } else {
                this.fineControl = new JSlider(JSlider.VERTICAL, 0, 9, 1);
            }
            this.fineControl.setMajorTickSpacing(1);
            this.fineControl.setPaintLabels(true);
            this.fineControl.setPaintTicks(true);
            this.fineControl.setSnapToTicks(true);
            this.fineControl.setValue(this.fineControl.getMaximum());
            this.updateFineControl();
            if (this.selectGlue != null) {
                Iterator positionItr = this.selectGlue.getSet().iterator();
                while (positionItr.hasNext()) {
                    Position position = (Position) positionItr.next();
                    if (position.getPath() != null && this.path != null) {
                        if (position.getPath().equals(this.path)) {
                            setupPathsEqual(position);
                        }
                    } else if (position.getPath() == null && this.path == null) {
                        setupPathsEqual(position);
                    }

                }
            }
            this.fineControl.addChangeListener(this);
            c.gridheight = 1;
            c.gridy++;
            sliderPanel.add(this.fineControl, c);

            c.gridy++;
            dateForLabel = dates.get(0);
            dateForLabelStr = "None";
            if (dateForLabel != null) {
                if (dateForLabel.after(new Date(Long.MIN_VALUE))) {
                    dateForLabelStr = this.dateFormatter.format(dateForLabel);
                } else {
                    dateForLabelStr = "Beginning of time";
                }
            }
            JLabel firstChange = new JLabel("<html>First change: " + dateForLabelStr);
            firstChange.setFont(monoSpaceFont);
            firstChange.setBorder(BorderFactory.createEmptyBorder(0, 0, coarseLabelInset, 0));
            sliderPanel.add(firstChange, c);
        }
        
        c.gridy++;
        return sliderPanel;
    }

    /**
     * @param position
     */
    private void setupPathsEqual(Position position) {
        this.position = position;
        this.selectPositionCheckBox.setSelected(true);
        Date coordinate = new Date(ThinVersionHelper.convert(position.getVersion()));
        for (int i = 0; i < this.dates.size(); i++) {
            Date date = this.dates.get(i);
            if (date == null) {
                if (position.getVersion() == Integer.MAX_VALUE) {
                    this.coarseControl.setValue(i);
                    this.fineControl.setValue(i);
                    break;
                }
            } else if (date.equals(coordinate)) {
                this.coarseControl.setValue(i);
                this.fineControl.setValue(i);
                break;
            }
        }
    }

    private void updateFineControl() {
        if (this.fineControl != null) {
            this.fineControl.setMaximum(getFineControlMax());
            this.fineControl.setMinimum(getFineControlMin());
            this.fineControl.setValue(this.coarseControl.getValue());
            updateFineControlLabelTable();
        }
    }

    /**
     * 
     */
    private void updateFineControlLabelTable() {
        Hashtable<Integer, JLabel> fineLabelTable = new Hashtable<Integer, JLabel>();
        for (int i = this.fineControl.getMinimum(); i <= this.fineControl
                .getMaximum(); i++) {
            if ((i != 0) && (i != this.coarseControl.getMaximum())) {
                if (i == this.fineControl.getMinimum()) {
                    fineLabelTable.put(new Integer(i), new JLabel(
                            "<html><font color='blue'>prev"));

                } else if (i == this.fineControl.getMaximum()) {
                    fineLabelTable.put(new Integer(i), new JLabel(
                            "<html><font color='blue'>next"));

                } else {
                    labelDateItem(fineLabelTable, i);
                }

            } else {
                labelDateItem(fineLabelTable, i);
            }
        }
        this.fineControl.setLabelTable(fineLabelTable);
    }

    /**
     * @param fineLabelTable
     * @param i
     */
    private void labelDateItem(Hashtable<Integer, JLabel> fineLabelTable, int i) {
        StringBuffer prefix = new StringBuffer();
        // prefix.append("<html>");
        if (i == this.fineControl.getValue()) {
            prefix.append("<html><font color='red'>");
        }
        JLabel positionLabel = new JLabel(prefix.toString()
                + this.positionStrings.get(i));
        positionLabel.setFont(monoSpaceFont);
        fineLabelTable.put(new Integer(i), positionLabel);

    }

    private int getFineControlMax() {
        if (this.coarseControl.getValue() > this.coarseControl.getMaximum()
                - fineControlSize / 2) {
            return this.coarseControl.getMaximum();
        }
        if (this.coarseControl.getValue() < this.coarseControl.getMinimum()
                + fineControlSize / 2) {
            return this.coarseControl.getMinimum() + fineControlSize;
        }
        return this.coarseControl.getValue() + fineControlSize / 2;
    }

    private int getFineControlMin() {
        if (this.coarseControl.getValue() > this.coarseControl.getMaximum()
                - fineControlSize / 2) {
            return this.coarseControl.getMaximum() - fineControlSize;
        }
        if (this.coarseControl.getValue() < this.coarseControl.getMinimum()
                + fineControlSize / 2) {
            return this.coarseControl.getMinimum();
        }
        return this.coarseControl.getValue() - fineControlSize / 2;
    }

    /**
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == this.coarseControl) {
            this.updateFineControl();
        }
        if (e.getSource() == this.fineControl) {
            this.fineControl.removeChangeListener(this);
            this.coarseControl.removeChangeListener(this);
            this.coarseControl.setValue(this.fineControl.getValue());
            this.coarseControl.addChangeListener(this);
            if ((this.fineControl.getValue() == this.fineControl.getMinimum())
                    && (this.fineControl.getValue() != 0)) {
                this.fineControl.setMinimum(Math.max(this.fineControl
                        .getMinimum() - 1, 0));
                this.fineControl.setMaximum(Math.min(this.fineControl
                        .getMaximum() - 1, this.dates.size() - 1));
            }
            if ((this.fineControl.getValue() == this.fineControl.getMaximum())
                    && (this.fineControl.getValue() != this.coarseControl
                            .getMaximum())) {
                this.fineControl.setMinimum(Math.max(this.fineControl
                        .getMinimum() + 1, 0));
                this.fineControl.setMaximum(Math.min(this.fineControl
                        .getMaximum() + 1, this.dates.size() - 1));
            }
            this.updateFineControlLabelTable();
            this.fineControl.addChangeListener(this);
        }
        if (this.position != null) {
            I_Position oldPosition = this.position;
        	Date d = this.dates.get(this.fineControl.getValue());
        	if (d == null) {
        		d = new Date(Long.MAX_VALUE);
        	}
            this.position = new Position(ThinVersionHelper.convert(d.getTime()), this.path);
            if (oldPosition.equals(this.position) == false) {
                try {
                    this.selectGlue.replaceObj(oldPosition,
                            this.position);
                } catch (Exception e1) {
        			AceLog.alertAndLogException(e1);
                }
            }
        }

    }

    /**
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent e) {
        try {
            if (e.getSource() == this.editOnPathCheckBox) {
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    this.editGlue.removeObj(this.path);
                } else if (e.getStateChange() == ItemEvent.SELECTED) {
                    this.editGlue.addObj(this.path);
                }
            } else if (e.getSource() == this.selectPositionCheckBox) {
                if (this.selectGlue != null) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        this.selectGlue.removeObj(position);
                        this.position = null;
                    } else if (e.getStateChange() == ItemEvent.SELECTED) {
                    	Date d = this.dates.get(this.fineControl.getValue());
                    	long time = Long.MAX_VALUE;
                    	if (d != null) {
                    		time = d.getTime();
                    	}
                        this.position = new Position(ThinVersionHelper.convert(time), this.path);
                        this.selectGlue.addObj(position);
                    }
                }
            }
        } catch (Exception ex) {
			AceLog.alertAndLogException(ex);
        }
    }

    /**
     * @return
     */
    public boolean isPositionSelected() {
        return this.selectPositionCheckBox.isSelected();
    }

    /**
     * @return
     */
    public Position getPosition() {
       	Date d = this.dates.get(this.fineControl.getValue());
       	if (d == null) {
       		d = new Date(Long.MAX_VALUE);
       	}
        return new Position(ThinVersionHelper.convert(d.getTime()), this.path);
    }
}