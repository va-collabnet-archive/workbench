/*
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

package org.ihtsdo.project.view.details;

/**
 * The Class CellObject.
 *
 * @author arodriguez
 */
public class CellObject {
    
    /**
     * Creates a new instance of CellObject.
     *
     * @param sAtrName the s atr name
     * @param fontColor the font color
     * @param bgColor the bg color
     */
    public CellObject(String sAtrName,String fontColor,String bgColor) {
        _fontColor=fontColor;
        _atrName=sAtrName;
        _bgColor=bgColor;
    }
    
    /** The _font color. */
    private String _fontColor;
    
    /** The _atr name. */
    private String _atrName;
    
    /** The _bg color. */
    private String _bgColor;
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
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
    
    /**
     * Gets the atr name.
     *
     * @return the atr name
     */
    public String getAtrName(){
        return _atrName;
    }
	
	/**
	 * Sets the _font color.
	 *
	 * @param color the new _font color
	 */
	public void set_fontColor(String color) {
		_fontColor = color;
	}
	
	/**
	 * Sets the _bg color.
	 *
	 * @param color the new _bg color
	 */
	public void set_bgColor(String color) {
		_bgColor = color;
	}
	
	/**
	 * Sets the _atr name.
	 *
	 * @param name the new _atr name
	 */
	public void set_atrName(String name) {
		_atrName = name;
	}
}
