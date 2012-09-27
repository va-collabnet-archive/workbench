/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.api;

import java.beans.PropertyVetoException;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.AnalogGeneratorBI;
import org.ihtsdo.tk.api.ComponentVersionBI;

public interface I_AmPart<T extends AnalogBI> extends ComponentVersionBI,
        AnalogGeneratorBI<T> {
	
    @Override
	public int getStatusNid();
	public void setStatusNid(int statusNid) throws PropertyVetoException;

    @Override
	public int getAuthorNid();
	public void setAuthorNid(int authorNid) throws PropertyVetoException;

    @Override
	public int getPathNid();
	public void setPathNid(int pathNid) throws PropertyVetoException;
    
    @Override
	public int getModuleNid();
	public void setModuleNid(int moduleNid) throws PropertyVetoException;
        
    @Override
	public long getTime();
	public void setTime(long time) throws PropertyVetoException;
	
	public ArrayIntList getPartComponentNids();
	
	/**
	 * 1. Analog, an object, concept or situation which in some way resembles a different situation
	 * 2. Analogy, in language, a comparison between concepts
	 * @param statusNid
         * @param time
         * @param author
         * @param module
         * @param pathNid
	 */
    @Override
	public T makeAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid);
	
	@Deprecated
	public int getPathId();
	@Deprecated
	public int getStatusId();
	@Deprecated
	public int getVersion();
	@Deprecated
	public void setPathId(int pathId) throws PropertyVetoException;
	@Deprecated
	public void setStatusId(int statusId) throws PropertyVetoException;

}
