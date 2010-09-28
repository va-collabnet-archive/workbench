package org.ihtsdo.tk.helper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

public class SpellingDiffHelper {

	private static Hashtable<String,String> usspelling ;
	private static Hashtable<String,String> ukspelling;
	private static String strSplitChars;
	private static boolean propsLoaded;
	private static InputStream fis;
	
	static {
		fis=(InputStream) SpellingDiffHelper.class.getResourceAsStream("GB-US-spellingdiffs.txt");
		usspelling = new Hashtable<String,String>();
		ukspelling = new Hashtable<String,String>();
		propsLoaded=false;
	}
	private static BufferedReader in;

	public SpellingDiffHelper(){
		
	}
	public static void main(String args[]){
		boolean b=checkTermSpelling("colour","en");
		System.out.println("b=" + b);
		System.out.println( getSpellingTerm("color","en"));
	}
	
	public static boolean checkTermSpelling(String term,String language){

		try{
			if (!propsLoaded){
				if (!LoadProperties())
					return false;
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

			if(!origvalue.equals(usstring.toString().trim()) && (language.toLowerCase().equals(EN) || language.toLowerCase().equals(US))){
				return false;
			}else if(!origvalue.equals(ukstring.toString().trim()) && language.toLowerCase().equals(GB) ){
				return false;
			}


		}catch(Exception e){
			System.out.println("Exception in SpellingDiffHelper.checkTermSpelling: " + e);
		}
		return true;
	}
	
	public static String getSpellingTerm(String term,String language){

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
			if(origvalue.equals(usstring.toString().trim()) && (language.toLowerCase().equals(EN) || language.toLowerCase().equals(US))){
				return ukstring.toString();
			}else if(origvalue.equals(ukstring.toString().trim()) && language.toLowerCase().equals(GB) ){
				return usstring.toString();
			}else{
				return "";
			}
			}


		}catch(Exception e){
			System.out.println("Exception in SpellingDiffHelper.checkTermSpelling: " + e);
		}
		return "";
	}

	private static boolean LoadProperties(){

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
			System.out.println("Exception in SpellingDiffHelper.LoadProperties: " + e);
			propsLoaded=false;
			return false;
		}
		propsLoaded=true;
		return true;
	}
	private static void createDialectTerms(String strToSplit,StringBuffer UsString,StringBuffer UkString,String SplitChars ){

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
}
