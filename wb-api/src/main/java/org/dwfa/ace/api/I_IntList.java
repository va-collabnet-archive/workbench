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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.ihtsdo.tk.api.NidListBI;

public interface I_IntList extends ListDataListener, NidListBI {

    public void contentsChanged(ListDataEvent e);

    public void intervalAdded(ListDataEvent e);

    public void intervalRemoved(ListDataEvent e);

    public boolean addListDataListener(ListDataListener o);

    public boolean removeListDataListener(ListDataListener o);

}
