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
 * Created on Feb 17, 2006
 */
package org.dwfa.queue.bpa.tasks.hide;

import org.dwfa.bpa.tasks.editor.DataIdEditor;
import org.dwfa.queue.bpa.tasks.failsafe.QueueEntryData;

/**
 * @author kec
 *
 */
public class QueueEntryDataIdEditor extends DataIdEditor {

    /**
     * @param arg0
     * @throws ClassNotFoundException
     */
    public QueueEntryDataIdEditor(Object obj) throws ClassNotFoundException {
        super(obj);
    }

    public Class getAcceptableClass() {
        return QueueEntryData.class;
    }

}
