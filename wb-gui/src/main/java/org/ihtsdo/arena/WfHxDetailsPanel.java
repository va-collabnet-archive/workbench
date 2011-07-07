package org.ihtsdo.arena;

import java.awt.Color;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.AceTableRenderer;
import org.ihtsdo.arena.conceptview.ConceptViewSettings;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetReader;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WfHxDetailsPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private class Workflow {
		private String id;
		private String action;
		private String state;
		private String modeler;
		private String time;
		
		public String getId() {
			return id;
		}

		public String getAction() {
			return action;
		}

		public String getState() {
			return state;
		}

		public String getModeler() {
			return modeler;
		}

		public String getTime() {
			return time;
		}

		public void setId(String uuid) {
			id = uuid;
		}
		
		public void setAction(String act) {
			action = act;
		}
		
		public void setState(String st) {
			state = st;
		}
		
		public void setModeler(String mod) {
			modeler = mod;
		}

		public void setTime(String t) {
			time = t;
		}
	}
	
	public class WfHxXmlHandler extends DefaultHandler {
		 
	    private String tempVal;
		private Workflow tempWorkflow;
		
		public void startDocument() throws SAXException {
	        allWorkflows = new ArrayList<Workflow>();
    		tempWorkflow = new Workflow();
	    }

	    public void endDocument() throws SAXException {
	        
	    }

	    public void startElement(String uri, String localName,
	    						 String qName, Attributes attributes)
	    	throws SAXException {
	    }

	    public void endElement(String uri, String localName, String qName)
	    	throws SAXException {
	    	if(qName.equalsIgnoreCase("workflow")) {
				//add it to the list
	    		if (tempWorkflow != null) {
	    			allWorkflows.add(tempWorkflow);	    
	    		}
	    		tempWorkflow = new Workflow();
	    	} else if (qName.equalsIgnoreCase("Id")) {
				tempWorkflow.setId(tempVal);
	    	} else if (qName.equalsIgnoreCase("Action")) {
				tempWorkflow.setAction(tempVal);
			} else if (qName.equalsIgnoreCase("State")) {
				tempWorkflow.setState(tempVal);
			} else if (qName.equalsIgnoreCase("Modeler")) {
				tempWorkflow.setModeler(tempVal);
			} else if (qName.equalsIgnoreCase("Time")) {
				tempWorkflow.setTime(tempVal);
			}
	    }

	    public void characters(char ch[], int start, int length)
	    throws SAXException {
	        tempVal = new String(ch, start, length);
	    }
	}

	private JEditorPane htmlPane = new JEditorPane();
	private DefaultHandler xmlHandler;
	
	private List<Workflow> allWorkflows;

	private String currentHtml;  

	//	private boolean currentlyDisplayed = true;
	private long currentLatestTimestamp = Long.MIN_VALUE;
	private I_GetConceptData currentConcept = null;
	
	private ConceptViewSettings settings;

	public WfHxDetailsPanel(ConceptViewSettings settings) {
		super(new GridLayout(1,1));

		this.settings = settings;

        htmlPane.setContentType("text/html");
        htmlPane.setEditable(false);
    	currentHtml = generateWfHxAsHtml(settings.getConcept());
    	currentConcept = settings.getConcept();
		WorkflowHistoryJavaBean latestBean = null;
		
		try {
			latestBean = WorkflowHelper.getLatestWfHxJavaBeanForConcept(settings.getConcept());
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Couldn't find Wf Hx for Arena Panel for Concept: " + settings.getConcept());
		}
		
		if (latestBean != null) {
			currentLatestTimestamp = latestBean.getWorkflowTime();
		}

		htmlPane.setText(currentHtml);
		
		add(htmlPane);
	}
	 
	public boolean isNewHtmlCodeRequired(I_GetConceptData arenaConcept) {
		boolean generateNewHtml = false;
		
		if (arenaConcept != null) {
			if (arenaConcept.getPrimUuid() != null) {
				// Proper Concept
				if (currentConcept == null || !arenaConcept.getPrimUuid().equals(currentConcept.getPrimUuid())) {
					generateNewHtml = true;
					currentConcept = arenaConcept;
				}  
	
				try {
					WorkflowHistoryJavaBean latestBean = WorkflowHelper.getLatestWfHxJavaBeanForConcept(arenaConcept);
					
					if (latestBean != null) {
						// Greater means new, lesser means undo performed
						if (latestBean.getWorkflowTime() != currentLatestTimestamp) {
							generateNewHtml = true;
						}
		
						currentLatestTimestamp = latestBean.getWorkflowTime();
					}
				} catch (Exception e) {
					AceLog.getAppLog().log(Level.WARNING, "Failure to identify latest WfHx for concept: " + arenaConcept);
				}
			} else {
				// Concept is not correct state (missing a PrimUid).
				// Therefore, generate new (blank) wfHx details, but do not 
				// assign the currentConcept nor currentLatestTimestamp.
				generateNewHtml = true;
			}
		}
		
		return generateNewHtml;
	}

	private String generateWfHxAsHtml(I_GetConceptData arenaConcept) {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        TreeSet<WorkflowHistoryJavaBean> allRows = WorkflowHelper.getAllWorkflowHistory(arenaConcept);
        String inputXml = generateXsltXml(allRows);

        return transformerWithoutXslt(inputXml);
	}

	private void parseDocument(String inputXml) {

		//get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {

			//get a new instance of parser
			SAXParser sp = spf.newSAXParser();
			xmlHandler = new WfHxXmlHandler();
			//parse the file and also register this class for call backs
			StringReader sr = new StringReader(inputXml);
			sp.parse(new StringBufferInputStream(inputXml), xmlHandler);

		}catch(SAXException se) {
			se.printStackTrace();
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	private String transformerWithoutXslt(String inputXml) {
		Color headerColor = Color.LIGHT_GRAY;
		StringBuffer retStr = new StringBuffer();
		
		// Setup XML
		retStr.append("\n<html><body>");
		
		// Setup Table
		retStr.append("<TABLE border=\"1\" cellpadding=\"3\" cellspacing=\"0\"> 	<tbody>");
		retStr.append("<tr bgcolor=\"" + colorToHtml(headerColor) + "\">	<th>Action</th>		<th>State</th> 	<th>Modeler</th>      <th>Timestamp</th>    </tr>");
		
		if (inputXml.length() > 0) {
			parseDocument(inputXml);
			Iterator<Workflow> itr = allWorkflows.iterator();
			String currentUid = UUID.randomUUID().toString();
			Color currentColor =  Color.WHITE;
			
			while(itr.hasNext()) {
				Workflow currentWf = (Workflow)itr.next();
				
				if (!currentUid.equalsIgnoreCase(currentWf.getId())) {
					currentUid = currentWf.getId();
					if (currentColor.equals( AceTableRenderer.LEMON_CHIFFON)) {
						currentColor = Color.WHITE;
					} else {
						currentColor = AceTableRenderer.LEMON_CHIFFON;
					}
				} 
				
				retStr.append("<tr bgcolor=\"" + colorToHtml(currentColor) + "\">");
				
				retStr.append(processColumn(currentWf.getAction()));
				retStr.append(processColumn(currentWf.getState()));
				retStr.append(processColumn(currentWf.getModeler()));
				retStr.append(processColumn(currentWf.getTime()));

				retStr.append("</tr>");
			}
		}
		
		retStr.append("\n</tbody></TABLE>");
		retStr.append("\n</body></html>");
		return retStr.toString();
	}

	private Object processColumn(String str) {
		return "<td>" + 
		   	"<font face='Dialog' size='3'>" + 
			   str +
			   "</font></td>";
	}

	private String colorToHtml(Color c) {
        String r = (c.getRed() < 16) ? "0" + Integer.toHexString(c.getRed()) : Integer.toHexString(c.getRed());
        String g = (c.getGreen() < 16) ? "0" + Integer.toHexString(c.getGreen()) : Integer.toHexString(c.getGreen());
        String b = (c.getBlue() < 16) ? "0" + Integer.toHexString(c.getBlue()) : Integer.toHexString(c.getBlue());
        return "#" + r + g + b;
	}

	private String generateXsltXml(TreeSet<WorkflowHistoryJavaBean> allRows) {
		StringBuffer retStr = new StringBuffer();
		
		if (allRows.size() > 0) {
			retStr.append("\n<workflows>");
		}
		
		for (WorkflowHistoryJavaBean bean : allRows) {
			retStr.append("\n\t\t" + WorkflowHistoryRefsetReader.generateXmlForXslt(bean));
		}

		if (allRows.size() > 0) {
			retStr.append("\n</workflows>");
		}

		return retStr.toString();
	}

	public String getCurrentHtml() {
		return currentHtml;
	}
}
