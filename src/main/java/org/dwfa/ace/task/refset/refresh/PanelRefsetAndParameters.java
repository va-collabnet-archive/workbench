package org.dwfa.ace.task.refset.refresh;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.commit.TestForEditRefsetPermission;
import org.dwfa.ace.task.util.DatePicker;
import org.dwfa.bpa.data.ArrayListModel;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * The Refresh Refset panel that allows user to input:
 * 1) refset name (from pulldown menu)
 * 2) editor (from pulldown menu)
 * 3) comments (text field)
 * 4) deadline (date picker)
 * 5) priority (from pulldown menu)
 * 6) request attachments (file chooser)
 * 
 * @author Perry Reid
 * @version 1.0, November 2009 
 * 
 */
public class PanelRefsetAndParameters extends JPanel {

    /* -----------------------
     * Properties 
     * -----------------------
     */
	// Serialization Properties 
    private static final long serialVersionUID = 1L;

    // components
    private JLabel refsetSpecLabel;
    private JLabel editorLabel;
    private JLabel commentsLabel;
    private JLabel deadlineLabel;
    private JLabel priorityLabel;
    private JButton openFileChooserButton;
    private JComboBox refsetSpecComboBox;
    private JComboBox editorComboBox;
	private JComboBox priorityComboBox;
 	private JTextArea commentsTextField;
    private DatePicker deadlinePicker;
    private JScrollPane commentsScrollPane;
    
    private JList attachmentList;
    private HashSet<File> attachmentSet = new HashSet<File>();
    private ArrayListModel<File> attachmentListModel;

    private Set<I_GetConceptData> refsets;
    private Set<I_GetConceptData> editors;

    
    /**
     * 
     * @param refsets
     */
    public PanelRefsetAndParameters(Set<I_GetConceptData> refsets) {
        super(new GridBagLayout());
        this.refsets = refsets;
        
        /* -------------------------------------------------
         *  Set Default / initial values for all the fields 
         * -------------------------------------------------
         */
        // labels
        refsetSpecLabel     = new JLabel("Refset Spec (required):");
        editorLabel         = new JLabel("Editor (required):");
        deadlineLabel       = new JLabel("Deadline (required):");
        priorityLabel       = new JLabel("Priority (required):");
        commentsLabel       = new JLabel("Comments (optional):");


        // buttons and boxes
        openFileChooserButton 	= new JButton("Attach a file...");
        refsetSpecComboBox 		= new JComboBox(refsets.toArray());
        editorComboBox 			= new JComboBox();
        priorityComboBox 		= new JComboBox(new String[] { "Highest", "High", "Normal", "Low", "Lowest" });

        // date picker
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        deadlinePicker = new DatePicker(Calendar.getInstance(), null, dateFormat);

        // text fields
        commentsTextField = new JTextArea();
        commentsTextField.setLineWrap(true);
        commentsTextField.setWrapStyleWord(true);
        commentsScrollPane = new JScrollPane(commentsTextField);
        commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add Listeners 
        openFileChooserButton.addActionListener(new AddAttachmentActionLister());
        refsetSpecComboBox.addActionListener(new RefsetListener());

        /* -------------------------------------------------
         *  Layout the components  
         * -------------------------------------------------
         */
        layoutComponents();
    }


    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        
        
        // refset name label & box
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 10, 5, 5); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(refsetSpecLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        if (refsets.size() == 0) {
            this.add(new JLabel("No available Refset Specs"), gridBagConstraints);
        } else {
            this.add(refsetSpecComboBox, gridBagConstraints);
        }

        
        // editor
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(5, 10, 5, 5); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(editorLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        
        
        editors = null; 
        try {
        	editors = getValidEditors();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    if (editors == null || editors.size() == 0 ) {
	    	this.add(new JLabel("No available editors."), gridBagConstraints);
	    } else {
	    	// Populate the editorComboBox with the list of valid editors 
	    	editorComboBox = new JComboBox(editors.toArray());
	    	this.add(editorComboBox, gridBagConstraints);
	    }


        // deadline
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(5, 10, 5, 5); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(deadlineLabel, gridBagConstraints);
	
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5); // padding
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;
        this.add(deadlinePicker, gridBagConstraints);
	
        
        // priority
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(5, 10, 5, 5); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(priorityLabel, gridBagConstraints);
	
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(priorityComboBox, gridBagConstraints);


		// comments
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.insets = new Insets(5, 10, 5, 5); // padding
		gridBagConstraints.weighty = 0.0;
		gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
		this.add(commentsLabel, gridBagConstraints);
		
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 4;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.insets = new Insets(5, 5, 5, 5); // padding
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.anchor = GridBagConstraints.LINE_START;
		this.add(commentsScrollPane, gridBagConstraints);

		
        // file attachments
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.insets = new Insets(5, 5, 0, 5); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        this.add(openFileChooserButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3; 
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1;
        
        attachmentListModel = new ArrayListModel<File>();
        attachmentList = new JList(attachmentListModel);
        attachmentList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTask");
        attachmentList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "deleteTask");
        attachmentList.getActionMap().put("deleteTask", new DeleteAction());

		JScrollPane attachmentScroller = new JScrollPane(attachmentList);
		attachmentScroller.setMinimumSize(new Dimension(100,100));
		attachmentScroller.setMaximumSize(new Dimension(500,300));
		attachmentScroller.setPreferredSize(new Dimension(150,150));
		attachmentScroller.setBorder(BorderFactory.createTitledBorder("Attachments (optional):"));
		add(attachmentScroller, gridBagConstraints);

		// Tell the panel to o lay out its subcomponents again. It should be invoked 
		// when this container's subcomponents are modified after the container has been displayed.
		this.validate();


    }

    
    private Set<I_GetConceptData> getAllUsers() throws IOException, TerminologyException {
        I_GetConceptData userParent = LocalVersionedTerminology.get().getConcept(
        		ArchitectonicAuxiliary.Concept.USER.getUids());
        I_IntSet allowedTypes = LocalVersionedTerminology.get().newIntSet();
        allowedTypes.add(LocalVersionedTerminology.get().getConcept(
        		ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());
        return userParent.getDestRelOrigins(allowedTypes, true, true);
    }

    
    private Set<I_GetConceptData> getValidEditors() throws Exception {
        I_GetConceptData selectedRefset = getRefset();
        Set<I_GetConceptData> editors = new HashSet<I_GetConceptData>();
        if (selectedRefset != null) {
            for (I_GetConceptData user : getAllUsers()) {
                if (hasPermission(user, selectedRefset)) {
                    editors.add(user);
                }
            }
        }
        return editors;
    }

    
    private boolean hasPermission(I_GetConceptData user, I_GetConceptData selectedRefset) throws Exception {
        TestForEditRefsetPermission permissionTest = new TestForEditRefsetPermission();
        Set<I_GetConceptData> parents = new HashSet<I_GetConceptData>();
        parents.addAll(permissionTest.getValidRefsetsFromIndividualUserPermissions(user));
        parents.addAll(permissionTest.getValidRefsetsFromRolePermissions(user));

        for (I_GetConceptData parent : parents) {
            if (parent.isParentOfOrEqualTo(selectedRefset, true)) {
                return true;
            }
        }
        return false;
    }

    
    class RefsetListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	
            layoutComponents();
        }
    }

	private class AddAttachmentActionLister implements ActionListener {
		public AddAttachmentActionLister() {
		}
		public void actionPerformed(ActionEvent e) {
			try {
				if (e.getActionCommand().equals(openFileChooserButton.getText())) {
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.setDialogTitle("Attach a File");
					int returnValue = fileChooser.showDialog(new Frame(), "Attach file");
					if (returnValue == JFileChooser.APPROVE_OPTION) {
						File selectedFile = fileChooser.getSelectedFile(); 
						if (attachmentSet.contains(selectedFile)) {
							// Warn the user that the file is already attached 							
							JOptionPane.showMessageDialog(null,
									"The file '" + selectedFile.getName() + "' " +  
									" is already an attachment. \nPlease select a different file. ",
									"Attachment Already Exists Warning",
									JOptionPane.WARNING_MESSAGE);
						} else {
							// Add the attachment
							attachmentSet.add(selectedFile);
							attachmentListModel.add(selectedFile);		
						}
					}
				}
			} catch (Exception ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
		}
		
	}

	public class DeleteAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			File selectedFile = (File) attachmentList.getSelectedValue();
			attachmentListModel.remove(selectedFile);
			attachmentSet.remove(selectedFile);
		}
	}
    
    
	//-----------------------
	// Refset 
	//-----------------------
    public I_GetConceptData getRefset() {
        if (refsets.size() == 0) {
            return null;
        } else {
            return (I_GetConceptData) refsetSpecComboBox.getSelectedItem();
        }
    }
    
	public void setRefset(I_GetConceptData newRefset) {
		this.refsetSpecComboBox.setSelectedItem(newRefset);
	}

   
	//-----------------------
	// Editor 
	//-----------------------
     public I_GetConceptData getEditor() {
    	 I_GetConceptData selectedEditor = (I_GetConceptData) editorComboBox.getSelectedItem();
         return selectedEditor;
     }

	public void setEditor(I_GetConceptData newEditor) {
		this.editorComboBox.setSelectedItem(newEditor);
	}

	//-----------------------
	// Deadline
	//-----------------------
    public Calendar getDeadline() {
        return (Calendar) deadlinePicker.getSelectedDate(); 
    } 
    public void setDeadline(Calendar newDeadline) {
    	deadlinePicker.setSelectedDate(newDeadline);
    }
    

    //-----------------------
	// Priority
	//-----------------------
    public String getPriority() {
        return (String) priorityComboBox.getSelectedItem();
    }
    
    public void setPriority(String newPriority) {
        priorityComboBox.setSelectedItem(newPriority);
    }
    
    
	//-----------------------
	// Comments
	//-----------------------
    public String getComments() {
        String result = commentsTextField.getText();
        if (result == null) {
            return null;
        } else if (result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }

    public void setComments(String newComments) {
        commentsTextField.setText(newComments);
    }

    
	//-----------------------
	// Attachments
	//-----------------------
    public HashSet<File> getAttachments() {
        return attachmentSet;
    }

    public void setAttachments(HashSet<File> files) {
		attachmentSet.clear(); 
		attachmentSet.addAll(files); 
		attachmentListModel.clear();		
		attachmentListModel.addAll(files); 		
    }

    

}