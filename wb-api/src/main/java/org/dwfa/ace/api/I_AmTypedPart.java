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
import org.ihtsdo.tk.api.AnalogBI;

public interface I_AmTypedPart<T extends AnalogBI> extends I_AmPart<T> {

    public int getTypeNid();

    public void setTypeNid(int typeNid) throws PropertyVetoException;

    @Deprecated
    public void convertIds(I_MapNativeToNative jarToDbNativeMap);
    @Deprecated
    public int getTypeId();
    @Deprecated
    public void setTypeId(int typeNid) throws PropertyVetoException;
    

}