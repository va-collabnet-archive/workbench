/*
 * CellObject.java
 *
 * Created on January 19, 2007, 12:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ihtsdo.project.panel.details;

/**
 *
 * @author arodriguez
 */
public class CellObject {
    
    /** Creates a new instance of CellObject */
    public CellObject(String sAtrName,String fontColor,String bgColor) {
        _fontColor=fontColor;
        _atrName=sAtrName;
        _bgColor=bgColor;
    }
    private String _fontColor;
    private String _atrName;
    private String _bgColor;
    public String toString(){
    	StringBuffer sb=new StringBuffer("");
    	if (!_fontColor.equals("") ||!_bgColor.equals("")){
    		sb.append("<html>");
    		if (!_bgColor.equals("")){
    			sb.append("<p bgcolor='");
    			sb.append(_bgColor);
    			sb.append("'>");
    		}
    		if (!_fontColor.equals("")){
    			sb.append("<font color='");
    			sb.append(_fontColor);
    			sb.append("'><b>");
    			sb.append(_atrName);
    			sb.append("</b></font>");
    		}
    		else{
    			sb.append(_atrName);
    		}
    		if (!_bgColor.equals("")){
    			sb.append("</p>");
    		}
    		return sb.toString();
    	}		
    			
        return _atrName ;
    }
    public String getAtrName(){
        return _atrName;
    }
	public void set_fontColor(String color) {
		_fontColor = color;
	}
	public void set_bgColor(String color) {
		_bgColor = color;
	}
	public void set_atrName(String name) {
		_atrName = name;
	}
}
