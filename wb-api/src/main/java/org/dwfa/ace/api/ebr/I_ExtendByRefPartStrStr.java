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
import org.ihtsdo.tk.api.refex.type_string.RefexStringAnalogBI;
import org.ihtsdo.tk.api.refex.type_string_string.RefexStringStringAnalogBI;

public interface I_ExtendByRefPartStrStr<A extends RefexStringStringAnalogBI<A>> extends I_ExtendByRefPartStr<A> {

    public I_ExtendByRefPartStrStr<A> duplicate();

    public String getString2Value();

    public void setString2Value(String string2Value) throws PropertyVetoException;

}
