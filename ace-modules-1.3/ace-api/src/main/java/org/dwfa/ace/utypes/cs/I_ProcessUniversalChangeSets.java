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
package org.dwfa.ace.utypes.cs;

import java.io.IOException;

import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalIdList;

public interface I_ProcessUniversalChangeSets {

    public void processUniversalAceBean(UniversalAceBean bean, long commitTime) throws IOException;

    public void processIdList(UniversalIdList list, long commitTime) throws IOException;

    public void processAcePath(UniversalAcePath path, long commitTime) throws IOException;

    public void processAceEbr(UniversalAceExtByRefBean bean, long time) throws IOException;

}
