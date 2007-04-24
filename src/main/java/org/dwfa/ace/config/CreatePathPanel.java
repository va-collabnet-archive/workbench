package org.dwfa.ace.config;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dwfa.ace.ACE;
import org.dwfa.ace.AceLog;
import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescPart;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

public class CreatePathPanel extends JPanel  implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = -6900445108100412148L;
    JTextField desc;
    SelectPathAndPositionPanel sppp;
    TermComponentLabel parent;
    /**
     * @param config
     * @throws Exception 
     * @throws RemoteException
     * @throws QueryException
     */
    public CreatePathPanel(I_ConfigAceFrame aceConfig) throws Exception  {
      super(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = GridBagConstraints.HORIZONTAL;
      c.anchor = GridBagConstraints.NORTHWEST;
      c.gridwidth = 2;
      c.weightx = 1;
      c.weighty = 0;
      c.gridx = 0;
      c.gridy = 0;
      
      this.desc = new JTextField();
      this.desc.setBorder(BorderFactory.createTitledBorder("Description for new path:"));
      this.add(this.desc, c);
      
      c.gridy++;
      parent = new TermComponentLabel(aceConfig);
       JPanel parentHolder = new JPanel(new GridLayout(1,1));
      parentHolder.add(parent);
      parentHolder.setBorder(BorderFactory.createTitledBorder("Parent for path:"));
     this.add(parentHolder, c);
      
      
      JButton createButton = new JButton("create");
      createButton.addActionListener(this);
      c.fill = GridBagConstraints.NONE;
      c.anchor = GridBagConstraints.NORTHEAST;
      c.gridwidth = 1;
      c.weightx = 0;
      c.weighty = 0;
      c.gridx = 1;
      c.gridy++;
      this.add(createButton, c);
      /*
      PropertySetListenerGlue browsingPositionGlue = new PropertySetListenerGlue(null, null, 
              null, null, Position.class, config);
              */
      sppp = new SelectPathAndPositionPanel(true, "as origin", aceConfig, null);
      c.fill = GridBagConstraints.BOTH;
      c.anchor = GridBagConstraints.NORTHWEST;
      c.gridwidth = 2;
      c.weightx = 1;
      c.weighty = 1;
      c.gridx = 0;
      c.gridy++;
      this.add(sppp, c);
      
      
    }
    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
    	int n = JOptionPane.showConfirmDialog(
    		    this,
    		    "This operation will perform an immediate commit of all changes. \n\nDo you wish to proceed?",
    		    "Confirm commit...",
    		    JOptionPane.YES_NO_OPTION); 
    	if (n != JOptionPane.YES_OPTION) {
    		return;
    	}
        AceLog.getAppLog().info("Create new path: " + desc.getText());
        if (desc.getText() == null || desc.getText().length() == 0) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "Path description cannot be empty.");
            return;
        }
        List<I_Position> origins = this.sppp.getSelectedPositions();
        AceLog.getAppLog().info(origins.toString());
        if (origins.size() == 0) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "You must select at least one origin for path.");
            return;
        }
        ConceptBean selectedParent = (ConceptBean) parent.getTermComponent();
        if (selectedParent == null) {
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "You must designate one parent for path.");
            return;
        }
        try {
        	UUID newPathUid = UUID.randomUUID();
        	I_Path p = new Path(AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()), null);
        	Date now = new Date();
        	int thinDate = ThinVersionHelper.convert(now.getTime());
        	int idSource = AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
        	int nativePathId = AceConfig.vodb.uuidToNativeWithGeneration(newPathUid, idSource,
        			p, thinDate);
        	Path newPath = new Path(nativePathId, origins);
         	// path id and uuid == the corresponding concepts UUID...
        	
        	ConceptBean cb = ConceptBean.get(nativePathId);
           	cb.getUncommittedIds().add(nativePathId);
        	
        	//Needs a concept record...
        	I_ConceptAttributeVersioned con = new ThinConVersioned(nativePathId, 1);
        	ThinConPart part = new ThinConPart();
        	part.setPathId(AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()));
        	part.setVersion(Integer.MAX_VALUE);
        	part.setConceptStatus(AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
        	part.setDefined(false);
        	con.addVersion(part);
        	cb.setUncommittedConceptAttributes(con);
           	cb.getUncommittedIds().add(con.getConId());
         	
        	//Needs a description record...
        	int nativeDescId = AceConfig.vodb.uuidToNativeWithGeneration(UUID.randomUUID(), idSource,
        			p, thinDate);
        	ThinDescVersioned descV = new ThinDescVersioned(nativeDescId, nativePathId, 1);
        	ThinDescPart descPart = new ThinDescPart();
        	descPart.setPathId(AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()));
        	descPart.setVersion(Integer.MAX_VALUE);
        	descPart.setStatusId(AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
        	descPart.setInitialCaseSignificant(true);
        	descPart.setTypeId(AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
        	descPart.setLang("en");
        	descPart.setText(desc.getText());
        	descV.addVersion(descPart);
        	cb.getUncommittedDescriptions().add(descV);
           	cb.getUncommittedIds().add(descV.getDescId());
        	
        	//Needs a relationship record...
        	int nativeRelId = AceConfig.vodb.uuidToNativeWithGeneration(UUID.randomUUID(), idSource,
        			p, thinDate);
        	ThinRelVersioned relV = new ThinRelVersioned(nativeRelId, nativePathId, 
        			selectedParent.getConceptId(), 1);
        	ThinRelPart relPart = new ThinRelPart();
        	relPart.setPathId(AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()));
        	relPart.setVersion(Integer.MAX_VALUE);
        	relPart.setStatusId(AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
        	relPart.setRelTypeId(AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
        	relPart.setCharacteristicId(AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()));
        	relPart.setRefinabilityId(AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()));
        	relPart.setGroup(0);
        	relV.addVersion(relPart);
        	cb.getUncommittedSourceRels().add(relV);
          	cb.getUncommittedIds().add(relV.getRelId());
        	        	        	
        	ACE.addUncommitted(cb);
            ACE.commit();
           	ACE.addUncommitted(newPath);
           	
            AceLog.getAppLog().info("Created new path: " + desc.getText() + " " + origins);
            this.desc.setText("");
            this.parent.setTermComponent(null);
            ACE.commit();
            
        } catch (Exception ex) {
			AceLog.getAppLog().alertAndLogException(ex);
        }
    }
 
}