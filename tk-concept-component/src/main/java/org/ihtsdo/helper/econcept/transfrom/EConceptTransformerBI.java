/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.helper.econcept.transfrom;

import java.io.IOException;
import org.ihtsdo.tk.dto.concept.TkConcept;

/**
 * The Interface EConceptTransformerBI should be implemented by classes who wish
 * to transform eConcepts and write the tranformation to a file.
 *
 */
public interface EConceptTransformerBI {

    /**
     * Transforms the concept according to the implemented method.
     *
     * @param tkConcept the concept to process
     * @throws Exception indicates an exception has occurred
     */
    public void process(TkConcept tkConcept) throws Exception;

    /**
     * Closes the file writers.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    public void close() throws IOException;
}
