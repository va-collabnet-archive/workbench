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
package org.dwfa.ace.list;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.SwingUtilities;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;

public class TerminologyListModel extends AbstractListModel implements I_ModelTerminologyList {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    List<I_GetConceptData> elements = new ArrayList<I_GetConceptData>();

    public TerminologyListModel(List<I_GetConceptData> elements) {
        super();
        this.elements = elements;
    }

    public TerminologyListModel() {
        super();
    }

    public I_GetConceptData getElementAt(int index) {
        if (index >= 0 && index < elements.size()) {
            return elements.get(index);
        }
        return null;
    }

    public int getSize() {
        return elements.size();
    }

    public boolean addElement(I_GetConceptData o) {
        boolean rv = elements.add(o);
        if (SwingUtilities.isEventDispatchThread()) {
            fireIntervalAdded(this, elements.size() - 1, elements.size() - 1);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        fireIntervalAdded(this, elements.size() - 1, elements.size() - 1);
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return rv;
    }

    public boolean addElements(List<I_GetConceptData> conceptList) {
        boolean modified = elements.addAll(conceptList);
        if (modified) {
            if (SwingUtilities.isEventDispatchThread()) {
                fireIntervalAdded(this, elements.size() - 1, elements.size() - 1);
            } else {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            fireIntervalAdded(this, elements.size() - 1, elements.size() - 1);
                        }
                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return modified;
    }
    
    public void addElement(final int index, I_GetConceptData element) {
        elements.add(index, element);
        if (SwingUtilities.isEventDispatchThread()) {
            fireIntervalAdded(this, index, index);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        fireIntervalAdded(this, index, index);
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public I_GetConceptData removeElement(final int index) {
        I_GetConceptData rv = elements.remove(index);
        if (SwingUtilities.isEventDispatchThread()) {
            fireIntervalRemoved(this, index, index);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        fireIntervalRemoved(this, index, index);
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return rv;
    }

    public void clear() {
        final int oldSize = elements.size();
        elements.clear();
        if (SwingUtilities.isEventDispatchThread()) {
            fireIntervalRemoved(this, 0, oldSize);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        fireIntervalRemoved(this, 0, oldSize);
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
