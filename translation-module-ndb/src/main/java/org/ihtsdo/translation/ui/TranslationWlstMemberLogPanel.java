/*
 * Created by JFormDesigner on Mon Jul 19 16:03:08 GMT-03:00 2010
 */

package org.ihtsdo.translation.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.issue.Issue;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.issue.manager.implementation.CollabnetIssueManager;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.issue.manager.TextAreaRenderer;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.panel.details.*;
import org.ihtsdo.project.panel.details.LogObjectContainer;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.PromotionRefset;

/**
 * @author Guillermo Reynoso
 */
public class TranslationWlstMemberLogPanel extends JPanel {
	private WorkListMember member;
	private SimpleDateFormat formatter;
	private TreeMap<String, List<LogObjectContainer>> hashList;
	private HashMap<Integer,Integer> hashVarCol;
	private TranslationProject translationProject;
	private IssueRepository repo;
	private IssueRepoRegistration regis;
	public TranslationWlstMemberLogPanel() {
		initComponents();
		formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	}
	public void showMemberChanges(WorkListMember member,TranslationProject translationProject, IssueRepository repo, IssueRepoRegistration regis){
		this.member=member;
		this.translationProject=translationProject;
		if (repo==null || regis==null){	
			try {

				if (translationProject.getProjectIssueRepo()!=null){
					this.repo= IssueRepositoryDAO.getIssueRepository(translationProject.getProjectIssueRepo());

					this.regis=IssueRepositoryDAO.getRepositoryRegistration(this.repo.getUuid(), Terms.get().getActiveAceFrameConfig());

				}
			} catch (TerminologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

		}else{
			this.repo=repo;
			this.regis=regis;
		}
		hashList= new TreeMap<String, List<LogObjectContainer>>();
		getPromotionStatuses();
		getDescriptions();
		getComments();
		if (this.repo!=null && this.regis!=null){	
			getIssues();
		}
		completeResultTable();
	}

	private String getSiteUserPassword() {
		return regis.getPassword();
	}

	private String getSiteUserName() {
		return regis.getUserId();
	}
	private void getIssues() {
		if (repo!=null && regis!=null){
			List<Issue>issueL=new ArrayList<Issue>();
			I_GetConceptData concept = this.member.getConcept();
			String strDate;
			if (member.getConcept()!=null){
				if (repo.getType()==IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal()){
					CollabnetIssueManager cIM=new CollabnetIssueManager();

					try{
						cIM.openRepository(repo, getSiteUserName(), getSiteUserPassword());
					}catch(Exception e){
						e.printStackTrace();
						message("Sorry, cannot connect to repository.\n" + e.getMessage());
						return;
					}
					try {
						issueL=cIM.getIssuesForComponentId(concept.getUids().iterator().next().toString());
						for (Issue issue:issueL){
							strDate = formatter.format(issue.getLastModifiedDate());
							addIssueTolist(issue.getUser(),strDate, issue.getTitle() + "\n" + issue.getDescription());

						}
					} catch (Exception e) {
						e.printStackTrace();
						message("Sorry, cannot retrieve comments for this issue.\n" + e.getMessage());
						return;
					}
				}
			}
		}
	}
	private void addIssueTolist(String userName,String version, String stringIssue) {
		List<LogObjectContainer> objList;

		LogObjectContainer loc=new LogObjectContainer(LogObjectContainer.PARTS.ISSUE_PART,stringIssue, userName);
		if (hashList.containsKey(version)){
			objList=hashList.get(version);
		}else{
			objList=new ArrayList<LogObjectContainer>();
		}
		objList.add(loc);
		hashList.put(version, objList);
	}
	
	private void completeResultTable() {
		//		String[] columnNames = {"Author","Date",
		//				"Prom.Status","Comment", "Issue"};

		//		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
		//			private static final long serialVersionUID = 1L;
		//
		//			public boolean isCellEditable(int x, int y) {
		//				return false;
		//			}
		//		};
		List <String> lstHeaders=new ArrayList<String>();
		lstHeaders.add("Date");
		lstHeaders.add("Author");
		lstHeaders.add("Change");
//		lstHeaders.add("Comment");
//		lstHeaders.add("Issue");
//
//		for (Integer did:hashVarCol.keySet()){
//			hashVarCol.put(did, lstHeaders.size());
//			lstHeaders.add("Desc Id: " + did);
//			//			lstHeaders.add("Lang Refset for Desc Id: " + did);
//		}
		int[] colWidth=new int[lstHeaders.size()];

		int rowCount=0;
		List<LogObjectContainer> locList;
		for (String key: hashList.keySet()){
			locList=hashList.get(key);
			rowCount+=locList.size();
		}
		int[] rowHeight= new int[rowCount];
		List<String[]>dataVecList=new ArrayList<String[]>();

		Font fontTmp = tblLog.getFont();
		FontMetrics fontMetrics=new FontMetrics(fontTmp){};
		Rectangle2D bounds;
		int tmpWidth;
		int tmpHeight;
		rowCount=-1;
		for (String key: hashList.keySet()){
			locList=hashList.get(key);
			for (LogObjectContainer loc:locList){
			rowCount++;
			String[] objStringdata=new String[lstHeaders.size()];
			objStringdata[0]=key;
			bounds = fontMetrics.getStringBounds(objStringdata[0], null);  
	        tmpWidth = (int) bounds.getWidth();  
			if (colWidth[0]< tmpWidth){
				colWidth[0]=tmpWidth;
			}  
			tmpHeight = (int) bounds.getHeight();  
			if (rowHeight[rowCount]< tmpHeight){
				rowHeight[rowCount]=tmpHeight;
			}
			
				Integer index;
				I_GetConceptData status;
				String text;
				switch (loc.getPartType()){
				case PROMOTION_PART:
					try {
						I_ExtendByRefPartCid pval=(I_ExtendByRefPartCid)loc.getPart();
						status=Terms.get().getConcept(pval.getC1id());
						objStringdata[1]=Terms.get().getConcept(pval.getAuthorNid()).toString();
						bounds = fontMetrics.getStringBounds(objStringdata[1], null);  
				        tmpWidth = (int) bounds.getWidth();  
						if (colWidth[1]< tmpWidth){
							colWidth[1]=tmpWidth;
						}
						tmpHeight = (int) bounds.getHeight();  
						if (rowHeight[rowCount]< tmpHeight){
							rowHeight[rowCount]=tmpHeight;
						}
						objStringdata[2]=status.toString();
						bounds = fontMetrics.getStringBounds(objStringdata[2], null);  
				        tmpWidth = (int) bounds.getWidth();  
						if (colWidth[2]< tmpWidth){
							colWidth[2]=tmpWidth;
						}
						tmpHeight = (int) bounds.getHeight();  
						if (rowHeight[rowCount]< tmpHeight){
							rowHeight[rowCount]=tmpHeight;
						}
					} catch (TerminologyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;

				case COMMENTS_PART:
					text=loc.getStringPart();
					objStringdata[2]=text;

					bounds = fontMetrics.getStringBounds(objStringdata[2], null);  
			        tmpWidth = (int) bounds.getWidth();  
					if (colWidth[2]< tmpWidth){
						colWidth[2]=tmpWidth;
					}
//					tmpHeight = (int) bounds.getHeight();  
					if (rowHeight[rowCount]< 66){
						rowHeight[rowCount]=66;
					}
					break;

				case ISSUE_PART:
					objStringdata[1]=loc.getUserName();
					bounds = fontMetrics.getStringBounds(objStringdata[1], null);  
			        tmpWidth = (int) bounds.getWidth();  
					if (colWidth[1]< tmpWidth){
						colWidth[1]=tmpWidth;
					}
					tmpHeight = (int) bounds.getHeight();  
					if (rowHeight[rowCount]< tmpHeight){
						rowHeight[rowCount]=tmpHeight;
					}
					text=loc.getStringPart();
					objStringdata[2]=text;
					bounds = fontMetrics.getStringBounds(objStringdata[2], null);  
			        tmpWidth = (int) bounds.getWidth();  
					if (colWidth[2]< tmpWidth){
						colWidth[2]=tmpWidth;
					}
//					tmpHeight = (int) bounds.getHeight();  
					if (rowHeight[rowCount]< 66){
						rowHeight[rowCount]=66;
					}
					break;

				case DESCRIPTION_PART:
					try {
						I_DescriptionPart dval=(I_DescriptionPart)loc.getPart();
//						index = hashVarCol.get(loc.getDid());

						objStringdata[1]=Terms.get().getConcept(dval.getAuthorNid()).toString();
						bounds = fontMetrics.getStringBounds(objStringdata[1], null);  
				        tmpWidth = (int) bounds.getWidth();  
						if (colWidth[1]< tmpWidth){
							colWidth[1]=tmpWidth;
						}
						tmpHeight = (int) bounds.getHeight();  
						if (rowHeight[rowCount]< tmpHeight){
							rowHeight[rowCount]=tmpHeight;
						}
						if (objStringdata[2]!=null && !objStringdata[2].equals(""))
							objStringdata[2]+="\n";
						else
							objStringdata[2]="";

						objStringdata[2] +=dval.getText(); 
						I_GetConceptData type=Terms.get().getConcept(dval.getTypeId());
						objStringdata[2] +="\nType: " + type.toString(); 
						status=Terms.get().getConcept(dval.getStatusId());
						objStringdata[2] += "\nDesc.Status: " +status.toString();
						status=Terms.get().getConcept(dval.getStatusId());
						objStringdata[2] += "\nICS: " + dval.isInitialCaseSignificant();

						bounds = fontMetrics.getStringBounds(objStringdata[2], null);  
				        tmpWidth = (int) bounds.getWidth();  
						if (colWidth[2]< tmpWidth){
							colWidth[2]=tmpWidth;
						}
//						tmpHeight = (int) bounds.getHeight();  
						if (rowHeight[rowCount]< 66){
							rowHeight[rowCount]=66;
						}
					} catch (TerminologyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;

				case LANG_REFSET_PART:
					try{
						I_ExtendByRefPartCid rval=(I_ExtendByRefPartCid)loc.getPart();
						objStringdata[1]=Terms.get().getConcept(rval.getAuthorNid()).toString();
						bounds = fontMetrics.getStringBounds(objStringdata[1], null);  
				        tmpWidth = (int) bounds.getWidth();  
						if (colWidth[1]< tmpWidth){
							colWidth[1]=tmpWidth;
						}
						tmpHeight = (int) bounds.getHeight();  
						if (rowHeight[rowCount]< tmpHeight){
							rowHeight[rowCount]=tmpHeight;
						}
//						index = hashVarCol.get(loc.getDid());
						I_GetConceptData accept=Terms.get().getConcept(rval.getC1id());
						if (objStringdata[2]!=null && !objStringdata[2].equals(""))
							objStringdata[2]+="\n";
						else
							objStringdata[2]="";

						objStringdata[2] += "Acceptability: " + accept.toString();

						status=Terms.get().getConcept(rval.getStatusId());
						objStringdata[2] += "\nLang.Refset Status: " + status.toString();

						bounds = fontMetrics.getStringBounds(objStringdata[2], null);  
				        tmpWidth = (int) bounds.getWidth();  
						if (colWidth[2]< tmpWidth){
							colWidth[2]=tmpWidth;
						}
//						tmpHeight = (int) bounds.getHeight();  
						if (rowHeight[rowCount]< 100){
							rowHeight[rowCount]=100;
						}

					} catch (TerminologyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;

				}				
				dataVecList.add(objStringdata);
			}
		}

		String[][] data = new String[dataVecList.size()][lstHeaders.size()];
		dataVecList.toArray(data);
		String[] columnNames =new String[lstHeaders.size()];
		lstHeaders.toArray(columnNames);
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		tblLog.setModel(tableModel);
		TableColumnModel cmodel = tblLog.getColumnModel(); 
		LogAreaRenderer logAreaRenderer = new LogAreaRenderer();
		for (int i=0;i<cmodel.getColumnCount();i++){
			cmodel.getColumn(i).setCellRenderer(logAreaRenderer);
			if (colWidth[i]==0){
				cmodel.getColumn(i).setWidth(60);
			}else if (colWidth[i]>350){
				cmodel.getColumn(i).setPreferredWidth(350);
			}else{
				cmodel.getColumn(i).setPreferredWidth(colWidth[i] + 10);
			}
			//			cmodel.getColumn(i).sizeWidthToFit();
		} 

		for (int i=0;i<tableModel.getRowCount();i++){
			tblLog.setRowHeight(i, rowHeight[i] + 5);
		}
//		tblLog.setRowHeight();
		tblLog.revalidate();

	}
	private void getComments() {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			I_GetConceptData langRefsetConcept = translationProject.getTargetLanguageRefset();
			LanguageMembershipRefset targetLangRefset = new LanguageMembershipRefset(langRefsetConcept,config);
			long thickVer ;
			String strDate ;

			List<String>comments=new ArrayList<String>();
			if (targetLangRefset!=null){
				comments=targetLangRefset.getCommentsRefset(config).getComments(this.member.getId());
				for (int i=comments.size()-1;i>-1;i--) {
					thickVer = Long.valueOf(comments.get(i).substring(comments.get(i).trim().lastIndexOf(" ") +1));
					strDate = formatter.format(thickVer);
					addStringTolist(strDate, "Language refset comment: " + comments.get(i).substring(0,comments.get(i).lastIndexOf(" - Time:") ));

				}
			}
			comments=TerminologyProjectDAO.getWorkList(Terms.get().getConcept(this.member.getWorkListUUID()), config).getCommentsRefset(config).getComments(this.member.getId());

			for (int i=comments.size()-1;i>-1;i--) {
				thickVer = Terms.get().convertToThickVersion(Integer.parseInt(comments.get(i).substring(comments.get(i).trim().lastIndexOf(" ") +1)));
				strDate = formatter.format(thickVer);
				addStringTolist(strDate, "Workflow comment: " + comments.get(i).substring(0,comments.get(i).lastIndexOf(" - Time:") ));

			}
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private void getDescriptions() {
		try {
			hashVarCol=new HashMap<Integer,Integer>();

			I_GetConceptData langRefset = translationProject.getTargetLanguageRefset();
			List<ContextualizedDescription> descriptions = ContextualizedDescription.getContextualizedDescriptions(
					member.getId(), langRefset.getConceptNid(), true);
			for (ContextualizedDescription description:descriptions){
				if (description.getLanguageExtension()!=null){
					Integer did=description.getDescId();
					if (!hashVarCol.containsKey(did)){
						hashVarCol.put(did,0);
					}
					List<? extends I_DescriptionPart> parts = description.getDescriptionParts();
					if (parts!=null){
						for (I_AmPart part: parts){
							addPartTolist(part, LogObjectContainer.PARTS.DESCRIPTION_PART, did);
						}
					}
					List<? extends I_ExtendByRefPart> lParts = description.getLanguageRefsetParts();

					if (lParts!=null){
						for (I_AmPart part: lParts){
							addPartTolist(part, LogObjectContainer.PARTS.LANG_REFSET_PART, did);
						}
					}
				}
			}
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private void getPromotionStatuses() {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			WorkList worklist = TerminologyProjectDAO.getWorkList(Terms.get().getConcept(this.member.getWorkListUUID()), config);
			PromotionRefset promoRefset=worklist.getPromotionRefset(config);
			for (I_ExtendByRef promotionMember : Terms.get().getAllExtensionsForComponent(member.getId(), true)) {
				if (promotionMember.getRefsetId() == promoRefset.getRefsetId()) {
					List<? extends I_ExtendByRefPart> loopParts = promotionMember.getMutableParts();
					for (I_ExtendByRefPart loopPart : loopParts) {
						addPartTolist(loopPart,LogObjectContainer.PARTS.PROMOTION_PART, null);
					}
					return ;
				}
			}
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void addStringTolist(String version,String stringPart){
		List<LogObjectContainer> objList;

		LogObjectContainer loc=new LogObjectContainer(LogObjectContainer.PARTS.COMMENTS_PART,stringPart);
		if (hashList.containsKey(version)){
			objList=hashList.get(version);
		}else{
			objList=new ArrayList<LogObjectContainer>();
		}
		objList.add(loc);
		hashList.put(version, objList);
	}

	private void addPartTolist(I_AmPart part,LogObjectContainer.PARTS partType, Integer did){
		String version;
		long thickVer;
		List<LogObjectContainer> objList;

		thickVer=Terms.get().convertToThickVersion(part.getVersion());

		version=formatter.format(thickVer);
		LogObjectContainer loc=new LogObjectContainer(partType,part,did);
		if (hashList.containsKey(version)){
			objList=hashList.get(version);
		}else{
			objList=new ArrayList<LogObjectContainer>();
		}
		objList.add(loc);
		hashList.put(version, objList);
	}
	
	 class LogAreaRenderer extends JTextArea implements TableCellRenderer { 
		    
		    /** The renderer. */
		    private final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer(); 
		 
		    // Column heights are placed in this Map 
		    /** The tablecell sizes. */
		  
		    /**
		     * Instantiates a new text area renderer.
		     */ 
		    public LogAreaRenderer() { 
		        setLineWrap(true); 
		        setWrapStyleWord(true); 
		    } 
		 
		    /* (non-Javadoc)
		     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		     */ 
		    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
		                                                   boolean hasFocus, int row, int column) { 
		        // set the Font, Color, etc. 
		        renderer.getTableCellRendererComponent(table, value, 
		                isSelected, hasFocus, row, column); 
		        setForeground(renderer.getForeground()); 
		        setBackground(renderer.getBackground()); 
		        setBorder(renderer.getBorder()); 
		        setFont(renderer.getFont()); 
		        String text=renderer.getText();
		        setText(text); 
		        
		        setToolTipText(text);
		        int height_wanted = (int) getPreferredSize().getHeight();
		        if (height_wanted>48)
		        	setRows(6);
		 
//		        TableColumnModel columnModel = table.getColumnModel(); 
//		        setSize(columnModel.getColumn(column).getWidth(), 0); 
//		        int height_wanted = (int) getPreferredSize().getHeight(); 
//		        addSize(table, row, column, height_wanted); 
//		        height_wanted = findTotalMaximumRowSize(table, row); 
//		        if (height_wanted != table.getRowHeight(row)) { 
//		            table.setRowHeight(row, height_wanted); 
//		        } 
		        return this; 
		    } 
	 }
		 
	/**
	 * Message.
	 * 
	 * @param string the string
	 */
	private void message(String string) {

		JOptionPane.showOptionDialog(   
				this,   
				string,   
				"Information", JOptionPane.DEFAULT_OPTION,   
				JOptionPane.INFORMATION_MESSAGE, null, null,   
				null );   
	}

	private void button1ActionPerformed() {
		showMemberChanges(this.member,this.translationProject, this.repo, this.regis);
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		panel1 = new JPanel();
		button1 = new JButton();
		scrollPane1 = new JScrollPane();
		tblLog = new ZebraJTable();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//---- button1 ----
			button1.setText("refresh");
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed();
				}
			});
			panel1.add(button1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== scrollPane1 ========
		{

			//---- tblLog ----
			tblLog.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			tblLog.setAutoCreateRowSorter(true);
			tblLog.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			scrollPane1.setViewportView(tblLog);
		}
		add(scrollPane1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel panel1;
	private JButton button1;
	private JScrollPane scrollPane1;
	private ZebraJTable tblLog;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
