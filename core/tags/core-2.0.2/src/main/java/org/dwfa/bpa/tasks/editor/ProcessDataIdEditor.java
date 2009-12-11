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
/*
 * Created on Feb 22, 2006
 */
package org.dwfa.bpa.tasks.editor;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;

public class ProcessDataIdEditor extends DataIdEditor implements
        I_OnlyWantOneLine {

    public Class getAcceptableClass() {
        return I_EncodeBusinessProcess.class;
    }

    public ProcessDataIdEditor(Object obj) throws ClassNotFoundException {
        super(obj);
    }

}
