/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.helper.promote;

import java.io.IOException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;

/**
 * The interface
 * <code>TerminologyPromoterBI</code> provides methods for promoting terminology
 * from one path to another. Use the TerminologyStoreDI.getTerminolgoyPromoter methods
 * to get a <code>TerminolgoyPromoterBI</code> object.
 * 
 * @see TerminologyStoreDI
 */
public interface TerminologyPromoterBI {

    /**
     * Promotes the given nids using the coordinates specified in the promoter.
     * Performs a commit of all uncommitted changed as part of promotion.
     *
     * @param promotionNids the set of nids representing the concepts to promote
     * @param writeBack set to <code>true</code> to write changes back to the
     * source path, otherwise <code>false</code>
     * @return <code>true</code> if promotion was successful, otherwise <code>false</code>
     * @throws IOException signals that an I/O exception has occurred
     * @throws Exception indicates an exception occurred
     */
    public boolean promote(NidBitSetBI promotionNids, boolean writeBack) throws IOException, Exception;
}
