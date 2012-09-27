/*
 * Created by JFormDesigner on Mon Oct 11 12:47:55 GMT-03:00 2010
 */

package org.dwfa.ace.modeler.tool;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.swing.*;

import org.dwfa.ace.api.I_ConfigAceFrame;

public class ShowConceptData extends JPanel {

	private static final String ERROR_SPELL_KEY = "@@@Error@@@";
	private Hashtable<String,String> usspelling ;
	private Hashtable<String,String> ukspelling;
	private String strSplitChars;
	private boolean propsLoaded;
	private InputStream fis;
	private BufferedReader in;

	private Hashtable<String,String> words ;
	private boolean wordsLoaded;
	private InputStream fisW;
	private BufferedReader inW;
	
	private DefaultListModel parentModel;
	private String conceptName;
	private String semtag;
	private boolean ICS;
	private DefaultListModel lstSpeModel;
	
	public ShowConceptData() {
		initComponents();
	}

	public ShowConceptData(I_ConfigAceFrame config, DefaultListModel listModel,
			String conceptName, String semtag) {
		initComponents();
		this.parentModel=listModel;
		this.conceptName=conceptName;
		this.semtag=semtag;
		fis=(InputStream) ShowConceptData.class.getResourceAsStream("GB-US-spellingdiffs.txt");
		usspelling = new Hashtable<String,String>();
		ukspelling = new Hashtable<String,String>();
		propsLoaded=false;

		fisW=(InputStream) ShowConceptData.class.getResourceAsStream("IcsWords.txt");
		words = new Hashtable<String,String>();
		wordsLoaded=false;
		
		lblCpt.setText(conceptName + " (" + semtag  + ")");
		lstPar.setModel(this.parentModel);

		
		DefaultListModel lstTerModel=new DefaultListModel();
		lstTerModel.addElement(conceptName + " (" + semtag + ")");
		lstTerModel.addElement(conceptName );
		lstTer.setModel(lstTerModel);
		
		lstSpeModel=new DefaultListModel();
		String spTerm = getSpellingTerm(conceptName, "en");
		if (!spTerm.equals("")){
			if (spTerm.equals(ERROR_SPELL_KEY)){
				lstSpeModel.addElement("<html><font style=\"color:red;\">There is a spelling error in concept name.</font></html>");
			}else{
				lstSpeModel.addElement(spTerm + " (" + semtag + ")");
				lstSpeModel.addElement(spTerm );
			}
		}
		lstSpe.setModel(lstSpeModel);

		String ICS = getICSCategory(conceptName);
		if (ICS.equals("0")){
			this.ICS=false;
			lblICS.setText("False");
		}else{
			this.ICS=true;
			lblICS.setText("True");
		}
		
		
	}

	private String getICSCategory(String term){
		String retString="0";
		String word;
        if (term.indexOf(" ")>0)
            word=term.substring(0,term.indexOf(" "));
        else
            word=term;
		try{
			String aLine=null;

			if (!wordsLoaded){
				inW = new BufferedReader(new InputStreamReader(fisW, "UTF-8"));

				//Read in GB-US-spellingdiffs.txt UK<space>US
				while((aLine = inW.readLine()) != null){
					aLine.trim();
					String[] line = aLine.split(" ");
					words.put(line[0], line[1]);
				}
				wordsLoaded=true;
				inW.close();
			}

			if(words.containsKey(word)){
				retString=(String)words.get(word);
			}
		}catch(Exception e){
			System.out.println("Exception in ShowConceptData.checkWordInList: " + e);
		}
		return retString;
	}


	private String getSpellingTerm(String term,String language){

		try{
			if (!propsLoaded){
				if (!LoadProperties())
					return "";
			}
			String GB = "en-gb";
			String EN = "en";
			String US = "en-us";


			String origvalue=term.trim();

			int index = origvalue.lastIndexOf("(");
			if (index>-1){
				origvalue = origvalue.substring(0, index );
				origvalue =origvalue.trim();
			}

			StringBuffer usstring = new StringBuffer();
			StringBuffer ukstring = new StringBuffer();

			createDialectTerms(origvalue,usstring,ukstring,strSplitChars);
			if (!usstring.toString().equals(ukstring.toString())){
				if (!term.equals(usstring.toString()) && !term.equals(ukstring.toString())){
					return ERROR_SPELL_KEY;
				}
				if(origvalue.equals(usstring.toString().trim()) && (language.toLowerCase().equals(EN) || language.toLowerCase().equals(US))){
					return ukstring.toString();
				}else if(origvalue.equals(ukstring.toString().trim()) && language.toLowerCase().equals(GB) ){
					return usstring.toString();
				}else{
					return "";
				}
			}


		}catch(Exception e){
			System.out.println("Exception in ShowConceptData.checkTermSpelling: " + e);
		}
		return "";
	}

	private boolean LoadProperties(){

		try{
			String aLine=null;

			in = new BufferedReader(new InputStreamReader(fis, "UTF-8"));

			strSplitChars=" ,-()";

			//Read in GB-US-spellingdiffs.txt UK<space>US
			while((aLine = in.readLine()) != null){
				aLine.trim();
				String[] line = aLine.split(" ");
				if (line.length>1){
					usspelling.put(line[1], line[0]);
					ukspelling.put(line[0], line[1]);
				}
			}
			in.close();
		}catch(Exception e){
			System.out.println("Exception in ShowConceptData.LoadProperties: " + e);
			propsLoaded=false;
			return false;
		}
		propsLoaded=true;
		return true;
	}
	private void createDialectTerms(String strToSplit,StringBuffer UsString,StringBuffer UkString,String SplitChars ){

		String strtmp="";
		String initialLowerStr;
		String initialStr;
		String endStr;
		String gettedStr;

		if (SplitChars.length()>0){
			String Charac=SplitChars.substring(0,1);
			//                Charac="\\" + Charac;
			String nextSplitChars=SplitChars.substring(1);
			String[] line = strToSplit.split("\\" + Charac);
			int j;	
			//Process GB spelling
			for(int i = 0; i < line.length; i++){
				if (line[i].length()>0){
					initialLowerStr=line[i].substring(0,1).toLowerCase();
					initialStr=line[i].substring(0,1);
					if (line[i].length()>1)
						endStr=line[i].substring(1);
					else
						endStr="";
					strtmp=initialLowerStr + endStr;

					if(usspelling.containsKey(strtmp)){
						gettedStr=(String)usspelling.get(strtmp);
						if (initialLowerStr.equals(initialStr) )
							UkString.append(gettedStr);
						else if (gettedStr.length()>1)
							UkString.append(gettedStr.substring(0,1).toUpperCase() + gettedStr.substring(1));
						else    
							UkString.append(gettedStr.substring(0,1).toUpperCase() );

						UsString.append(line[i]);
					}else if(ukspelling.containsKey(strtmp)){
						gettedStr=(String)ukspelling.get(strtmp);
						if (initialLowerStr.equals(initialStr) )
							UsString.append(gettedStr);
						else if (gettedStr.length()>1)
							UsString.append(gettedStr.substring(0,1).toUpperCase() + gettedStr.substring(1));
						else
							UsString.append(gettedStr.substring(0,1).toUpperCase() );

						UkString.append(line[i]);
					}else{
						if (nextSplitChars.length()>0 ){
							for (j=0;j<nextSplitChars.length();j++){
								if (line[i].indexOf(nextSplitChars.charAt(j))>-1)
									break;
							}
							if (j<nextSplitChars.length()){
								createDialectTerms(line[i],UsString,UkString,nextSplitChars.substring(j));
							}
							else{
								UsString.append(line[i]);
								UkString.append(line[i]);
							}
						}else{
							UsString.append(line[i]);
							UkString.append(line[i]);
						}
					}
				}
				if(i < line.length-1){
					UsString.append(Charac);
					UkString.append(Charac);
				}
			}
			if (strToSplit.endsWith(Charac)){
				UsString.append(Charac);
				UkString.append(Charac);
			}
		}
	}
	
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		lblCpt = new JLabel();
		label3 = new JLabel();
		scrollPane1 = new JScrollPane();
		lstPar = new JList();
		label7 = new JLabel();
		scrollPane4 = new JScrollPane();
		lstTer = new JList();
		label5 = new JLabel();
		scrollPane2 = new JScrollPane();
		lstSpe = new JList();
		label6 = new JLabel();
		lblICS = new JLabel();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 160, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Concept:");
		add(label1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(lblCpt, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label3 ----
		label3.setText("Parents:");
		add(label3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== scrollPane1 ========
		{
			scrollPane1.setViewportView(lstPar);
		}
		add(scrollPane1, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label7 ----
		label7.setText("Terms");
		add(label7, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== scrollPane4 ========
		{
			scrollPane4.setViewportView(lstTer);
		}
		add(scrollPane4, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label5 ----
		label5.setText("Spelling differences:");
		add(label5, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== scrollPane2 ========
		{
			scrollPane2.setViewportView(lstSpe);
		}
		add(scrollPane2, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label6 ----
		label6.setText("Initial Capital Significant");
		add(label6, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		add(lblICS, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label1;
	private JLabel lblCpt;
	private JLabel label3;
	private JScrollPane scrollPane1;
	private JList lstPar;
	private JLabel label7;
	private JScrollPane scrollPane4;
	private JList lstTer;
	private JLabel label5;
	private JScrollPane scrollPane2;
	private JList lstSpe;
	private JLabel label6;
	private JLabel lblICS;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

	public DefaultListModel getParentModel() {
		return parentModel;
	}

	public void setParentModel(DefaultListModel parentModel) {
		this.parentModel = parentModel;
	}

	public String getConceptName() {
		return conceptName;
	}

	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}

	public String getSemtag() {
		return semtag;
	}

	public void setSemtag(String semtag) {
		this.semtag = semtag;
	}

	public boolean isICS() {
		return ICS;
	}

	public void setICS(boolean iCS) {
		ICS = iCS;
	}

	public DefaultListModel getLstSpeModel() {
		return lstSpeModel;
	}

	public void setLstSpeModel(DefaultListModel lstSpeModel) {
		this.lstSpeModel = lstSpeModel;
	}
}
