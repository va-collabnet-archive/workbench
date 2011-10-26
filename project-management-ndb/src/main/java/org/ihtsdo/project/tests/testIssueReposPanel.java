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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.issuerepository.manager.IssueRepoPanel;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

/**
 * The Class testIssueReposPanel.
 */
public class testIssueReposPanel extends javax.swing.JFrame
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
					new testIssueReposPanel().setVisible(true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Instantiates a new test issue repos panel.
	 * 
	 * @throws Exception the exception
	 */
	public testIssueReposPanel() throws Exception{
		System.out.println( "Attempting connection..." );
		vodbDirectory = new File("C:\\dev\\MrcmWS2\\miniSct-ide-sa2\\target\\sct-wb-ide-sa-bundle.dir\\berkeley-db");
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
			try {
				FileInputStream fin = new FileInputStream(
				"C:\\dev\\MrcmWS2\\project-manager\\src\\main\\java\\org\\dwfa\\termmed\\projectmanager\\tests\\config.dat");
				ObjectInputStream ois = new ObjectInputStream(fin);
				profile = (I_ConfigAceFrame) ois.readObject();
				ois.close();
			}
			catch (Exception e) { e.printStackTrace(); }

//			I_Path path = termFactory.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")});
//			I_Position position = termFactory.newPosition(path, Integer.MAX_VALUE);
//			profile.addViewPosition(position);
//			profile.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
//			profile.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid());
//			profile.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
//			profile.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
//			profile.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			termFactory.setActiveAceFrameConfig(profile);
//			
			iniComponents();
			
//			for (I_Position loopPosition : termFactory.getActiveAceFrameConfig().getViewPositionSet()) {
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
////			path = LocalVersionedTerminology.get().getPath(new UUID[] {UUID.fromString("7fe45a5a-99bb-4837-ab4f-3436191d7cbd")});
//	/*		List<I_Path> lpath=LocalVersionedTerminology.get().getPaths();
//			for (I_Path lpathi:lpath){
//				System.out.println("Path:" + lpathi.getUniversal().toString() );
//				List <UUID> pathId=lpathi.getUniversal()..getPathId();
//				for (UUID ui:pathId){
//					System.out.println("UUId:" + ui.toString());
//				}
//				System.out.println("PathId:" + lpathi.getUniversal().getPathId().toString() );
//				
//			}*/
////			f6adcbdd-bb44-4598-b891-f2a2665fdc54
//			path = LocalVersionedTerminology.get().getPath(new UUID[] {UUID.fromString("c35aaad7-17d2-4a01-83b0-f11e9b1e9205")});
//			position = LocalVersionedTerminology.get().newPosition( path, Integer.MAX_VALUE);
//			profile.addViewPosition(position);	
//			termFactory.setActiveAceFrameConfig(profile);
//			for (I_Position loopPosition : termFactory.getActiveAceFrameConfig().getViewPositionSet()) {
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
//				thickVer=LocalVersionedTerminology.get().convertToThickVersion(descriptionTuple.getVersion());
//				 System.out.println("date: " + formatter.format(thickVer));
//
//			}
//			System.out.println( "Closing..." );
//			I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) LocalVersionedTerminology.get();
//			termFactoryImpl.close();
//			System.out.println( "Connection closed" );
			this.setSize(400, 250);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		System.exit(0);

	}
	
	/**
	 * Ini components.
	 * 
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void iniComponents() throws TerminologyException, IOException{
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		
		pm=new IssueRepoPanel(termFactory);
		this.getContentPane().add(pm);
		this.addWindowListener(new AreYouSure());
	}
	
	/** The pm. */
	private IssueRepoPanel pm;

    /**
     * The Class AreYouSure.
     */
    private class AreYouSure extends WindowAdapter {   
        
        /* (non-Javadoc)
         * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
         */
        public void windowClosing( WindowEvent e ) {   
            int option = JOptionPane.showOptionDialog(   
            		testIssueReposPanel.this,   
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
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    			System.out.println( "Connection closed" );
                System.exit( 0 );   
            }   
        }   
    }   

}
