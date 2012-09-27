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
package org.ihtsdo.helper.econcept.transfrom;

import java.io.IOException;
import org.ihtsdo.tk.dto.concept.TkConcept;

// TODO: Auto-generated Javadoc
/**
 * The Interface EConceptTransformerBI.
 *
 * @author kec
 */
public interface EConceptTransformerBI {
    
    /**
     * Process.
     *
     * @param c the c
     * @throws Exception the exception
     */
    public void process(TkConcept c) throws Exception;
    
    /**
     * Close.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void close() throws IOException;
    
}
