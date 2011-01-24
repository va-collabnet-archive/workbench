/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.tests;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.issue.manager.IssuesPanel;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

/**
 * The Class testIssuesPanel.
 */
public class testIssuesPanel extends javax.swing.JFrame
{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The vodb directory. */
	static File vodbDirectory;
	
	/** The read only. */
	static boolean readOnly = false;
	
	/** The cache size. */
	static Long cacheSize = Long.getLong("600000000");
	
	/** The db setup config. */
	private static DatabaseSetupConfig dbSetupConfig; 
	
	/** The term factory. */
	private I_TermFactory termFactory;

	 /** The sdf. */
 	private static SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yyyy hh:mm am");
	
	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * 
	 * @throws Exception the exception
	 */
	public static void main( String[] args ) throws Exception
	{

		SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				try {
					new testIssuesPanel().setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Instantiates a new test issues panel.
	 * 
	 * @throws Exception the exception
	 */
	public testIssuesPanel() throws Exception{
		System.out.println( "Attempting connection..." );
		vodbDirectory = new File("C:\\dev\\sct-wb-ide-sa-bundle.dir4\\berkeley-db");
//				"//C:\\dev\\MrcmWS2\\miniSct-ide-sa2\\target\\sct-wb-ide-sa-bundle.dir\\berkeley-db");
		if (Terms.get() != null) {
			return;
		}

		if (dbSetupConfig == null) {
			dbSetupConfig = new DatabaseSetupConfig();
		}
		try {
			Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);

			termFactory = Terms.get();

			// set concept core view position
			I_ConfigAceFrame profile = termFactory.newAceFrameConfig();
			List<PathBI>ipa=termFactory.getPaths();
			Collection <UUID> uids;
			for (PathBI ip:ipa){
				uids=termFactory.getUids( ip.getConceptNid());
				
				System.out.println(ip.toString() + " UUID=" + uids.toArray()[0].toString() + " Positions:" );
				Collection<? extends PositionBI> pos=ip.getOrigins();
				for (PositionBI po :pos)
					System.out.println("             Position:" + po.toString() );
					
			}
		//	I_GetConceptData concept=termFactory.getConcept(new UUID[]{UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790")});
//			try {
//				FileInputStream fin = new FileInputStream(
//				"C:\\dev\\MrcmWS2\\project-manager\\src\\main\\java\\org\\dwfa\\termmed\\projectmanager\\tests\\config.dat");
//				ObjectInputStream ois = new ObjectInputStream(fin);
//				profile = (I_ConfigAceFrame) ois.readObject();
//				ois.close();
//			}
//			catch (Exception e) { e.printStackTrace(); }

			PathBI path = termFactory.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")});
			PositionBI position = termFactory.newPosition(path, Integer.MAX_VALUE);
			profile.addViewPosition(position);

			path = termFactory.getPath(new UUID[] {UUID.fromString("ff49bb30-90eb-11de-8a39-0800200c9a66")});
			position = termFactory.newPosition(path, Integer.MAX_VALUE-1);
			profile.addViewPosition(position);

			profile.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			profile.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid());
			profile.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			profile.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			profile.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			termFactory.setActiveAceFrameConfig(profile);
//			
			iniComponents();
			
//			for (PositionBI loopPosition : termFactory.getActiveAceFrameConfig().getViewPositionSet()) {
//				System.out.println("** Position on first pass: " + loopPosition.getPath().getConceptId() + " " + loopPosition.getVersion());
//			}
//
//			UUID[] uids = new UUID[1];
//			uids[0] = UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790");
//			I_GetConceptData concept = termFactory.getConcept(uids);
//
//			int lastVersion = Integer.MIN_VALUE;
//			String descriptionText = "";
//			for (I_DescriptionVersioned description : concept.getDescriptions()) {
//				for (I_DescriptionPart part : description.getVersions()) {
//					//System.out.println("Commited: " + part.getText());
//					if (part.getVersion() > lastVersion) {
//						descriptionText = part.getText();
//					}
//
//				}
//				System.out.println("Description" + descriptionText);
//
//			}
//
//			for (I_DescriptionVersioned description : concept.getUncommittedDescriptions()) {
//				for (I_DescriptionPart part : description.getVersions()) {
//					System.out.println("Uncommited: " + part.getText());
//				}
//			}
//			
//			
//			List<I_DescriptionTuple> descriptionTuples = concept.getDescriptionTuples(profile.getAllowedStatus(), 
//					profile.getDescTypes(), profile.getViewPositionSet());
//			
//			for (I_DescriptionTuple descriptionTuple : descriptionTuples) {
//				System.out.println("Tuple: " + descriptionTuple.getDescId() + " text= " + descriptionTuple.getText() + " path int=" + descriptionTuple.getPathId());
//			}
//			
//			System.out.println("Adding standalone view position....");
//
//			// set standalone view position
//			profile = termFactory.getActiveAceFrameConfig();
//
////			path = Terms.get().getPath(new UUID[] {UUID.fromString("7fe45a5a-99bb-4837-ab4f-3436191d7cbd")});
//	/*		List<PathBI> lpath=Terms.get().getPaths();
//			for (PathBI lpathi:lpath){
//				System.out.println("Path:" + lpathi.getUniversal().toString() );
//				List <UUID> pathId=lpathi.getUniversal()..getPathId();
//				for (UUID ui:pathId){
//					System.out.println("UUId:" + ui.toString());
//				}
//				System.out.println("PathId:" + lpathi.getUniversal().getPathId().toString() );
//				
//			}*/
////			f6adcbdd-bb44-4598-b891-f2a2665fdc54
//			path = Terms.get().getPath(new UUID[] {UUID.fromString("c35aaad7-17d2-4a01-83b0-f11e9b1e9205")});
//			position = Terms.get().newPosition( path, Integer.MAX_VALUE);
//			profile.addViewPosition(position);	
//			termFactory.setActiveAceFrameConfig(profile);
//			for (PositionBI loopPosition : termFactory.getActiveAceFrameConfig().getViewPositionSet()) {
//				System.out.println("** Position on second pass: " + loopPosition.getPath().getConceptId() + " " + loopPosition.getVersion());
//			}
//
//			uids = new UUID[1];
//			uids[0] = UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790");
//			concept = termFactory.getConcept(uids);
//
//			lastVersion = Integer.MIN_VALUE;
//			descriptionText = "";
//			int ipath=0;
//			for (I_DescriptionVersioned description : concept.getDescriptions()) {
//				for (I_DescriptionPart part : description.getVersions()) {
//					//System.out.println("Commited: " + part.getText());
//					/*if (part.getTypeId() == fully_specified_description_type_aux.getConceptId() &&
//							part.getVersion() > lastVersion) {
//						fsn = part.getText();
//					}*/
//					if (part.getVersion() > lastVersion) {
//						descriptionText = part.getText();
//						ipath=part.getPathId();
//					}
//
//				}
//				System.out.println("Description" + descriptionText + " Path int=" + ipath );
//
//			}
//
//			for (I_DescriptionVersioned description : concept.getUncommittedDescriptions()) {
//				for (I_DescriptionPart part : description.getVersions()) {
//					System.out.println("Uncommited: " + part.getText());
//				}
//			}
//			descriptionTuples = concept.getDescriptionTuples(profile.getAllowedStatus(), 
//					profile.getDescTypes(), profile.getViewPositionSet());
//			SimpleDateFormat formatter ;
//			 formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//		//	 Timestamp ts;
//			long thickVer;
//			for (I_DescriptionTuple descriptionTuple : descriptionTuples) {
//				System.out.println("Tuple: " + descriptionTuple.getDescId() +  " ver= " + descriptionTuple.getVersion() + " text= " + descriptionTuple.getText() + " path int=" + descriptionTuple.getPathId());
//				Collection<UUID> uuids=termFactory.getUids(descriptionTuple.getDescId());
//				for (UUID uid:uuids){
//					System.out.println("UID: " + uid.toString());
//				}
//				thickVer=Terms.get().convertToThickVersion(descriptionTuple.getVersion());
//				 System.out.println("date: " + formatter.format(thickVer));
//
//			}
//			System.out.println( "Closing..." );
//			I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) Terms.get();
//			termFactoryImpl.close();
//			System.out.println( "Connection closed" );
			this.setSize(400, 250);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}

//		System.exit(0);

	}
	
	/**
	 * Ini components.
	 * 
	 * @throws Exception the exception
	 */
	private void iniComponents() throws Exception{
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		pm=new IssuesPanel(termFactory);
		JTabbedPane tabs=new JTabbedPane();
//		GridBagConstraints c=new GridBagConstraints();
//		c.fill = GridBagConstraints.BOTH;
//		c.gridx = 0;
//		c.gridy = 0;
//		c.gridheight = 1;
//		c.weightx = 1.0;
//		c.weighty = 1.0;
//		c.anchor = GridBagConstraints.NORTHWEST;
		
		this.getContentPane().add(tabs);
		tabs.addTab("Issue Component", pm);
		this.addWindowListener(new AreYouSure());
		Container conta=this.getContentPane();
		Component[] comps=tabs.getComponents();
		Component issueComp=null;
		for (int i=0 ;i<comps.length;i++){
			System.out.println(comps[i].getClass().toString());
			if (comps[i].getClass().toString().equals("class org.ihtsdo.project.issue.manager.IssuesPanel")){
				issueComp=comps[i];
			}
		}
		
	}
//	private Component getComponent(String nameComp, Component[] comps){
//
//		Component issueComp=null;
//		for (int i=0 ;i<comps.length;i++){
//			if (comps[i].getName().equals("IssuesPanel")){
//				issueComp=comps[i];
//			}
//		}
//		if (issueComp==null){
//			for (int i=0 ;i<comps.length;i++){
//			return getComponent(nameComp,comps[i].getParent())
//		}
//	}
	
	/** The pm. */
private IssuesPanel pm;

    /**
     * The Class AreYouSure.
     */
    private class AreYouSure extends WindowAdapter {   
        
        /* (non-Javadoc)
         * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
         */
        public void windowClosing( WindowEvent e ) {   
            int option = JOptionPane.showOptionDialog(   
            		testIssuesPanel.this,   
                    "Are you sure you want to quit?",   
                    "Exit Dialog", JOptionPane.YES_NO_OPTION,   
                    JOptionPane.WARNING_MESSAGE, null, null,   
                    null );   
            if( option == JOptionPane.YES_OPTION ) {   
    			System.out.println( "Closing..." );
    			I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) Terms.get();
    			try {
					termFactoryImpl.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
    			System.out.println( "Connection closed" );
                System.exit( 0 );   
            }   
        }   
    }   

}
