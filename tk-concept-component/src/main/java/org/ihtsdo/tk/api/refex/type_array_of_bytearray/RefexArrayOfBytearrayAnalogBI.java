/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api.refex.type_array_of_bytearray;

import java.beans.PropertyVetoException;
import org.ihtsdo.tk.api.refex.RefexAnalogBI;

// TODO: Auto-generated Javadoc
/**
 * The Interface RefexArrayOfBytearrayAnalogBI.
 *
 * @param <A> the generic type
 * @author kec
 */
public interface RefexArrayOfBytearrayAnalogBI <A extends RefexArrayOfBytearrayAnalogBI<A>> 
	extends RefexAnalogBI<A>, RefexArrayOfBytearrayVersionBI<A> {
    
      /**
       * Sets the array of byte array.
       *
       * @param arrayOfByteArray the new array of byte array
       * @throws PropertyVetoException the property veto exception
       */
      void setArrayOfByteArray(byte[][] arrayOfByteArray) throws PropertyVetoException;
}