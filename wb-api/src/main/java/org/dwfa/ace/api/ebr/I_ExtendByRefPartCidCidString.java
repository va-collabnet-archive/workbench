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
package org.dwfa.ace.api.ebr;

import java.beans.PropertyVetoException;
import org.ihtsdo.tk.api.refex.type_nid_nid_string.RefexNidNidStringAnalogBI;

public interface I_ExtendByRefPartCidCidString<A extends RefexNidNidStringAnalogBI<A>>
	extends I_ExtendByRefPartCidCid<A>,
		I_ExtendByRefPartStr<A> {

	public int getC1id();

	public void setC1id(int c1id)throws PropertyVetoException;

	public int getC2id();

	public void setC2id(int c2id)throws PropertyVetoException;

	public String getString1Value();
        
        @Override
	public void setString1Value(String value) throws PropertyVetoException;

}
