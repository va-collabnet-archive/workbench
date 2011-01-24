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

package org.ihtsdo.translation.ui;

import java.io.Serializable;

/**
 * The Class TreeObj.
 */
public class FolderTreeObj implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Instantiates a new tree obj.
     * 
     * @param sObjType the s obj type
     * @param sAtrName the s atr name
     * @param sAtrValue the s atr value
     */
	
    public FolderTreeObj(String sObjType,String sAtrName,Object sAtrValue) {
        _objType=sObjType;
        _atrName=sAtrName;
        _atrValue=sAtrValue;
    }
    
    /** The _obj type. */
    private String _objType;
    
    /** The _atr name. */
    private String _atrName;
    
    /** The _atr value. */
    private Object _atrValue;
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString(){
        return _atrName ;
    }
    
    /**
     * Gets the obj type.
     * 
     * @return the obj type
     */
    public String getObjType(){
        return _objType;
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
     * Gets the atr value.
     * 
     * @return the atr value
     */
    public Object getAtrValue(){
        return _atrValue;
    }
    
    /**
     * Sets the atr name.
     * 
     * @param sAtrName the new atr name
     */
    public void setAtrName(String sAtrName){
        _atrName=sAtrName;
    }
}
