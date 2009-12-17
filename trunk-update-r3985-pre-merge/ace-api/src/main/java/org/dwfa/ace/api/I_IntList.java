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

public interface I_IntList extends ListDataListener {

    public void contentsChanged(ListDataEvent e);

    public void intervalAdded(ListDataEvent e);

    public void intervalRemoved(ListDataEvent e);

    public boolean addListDataListener(ListDataListener o);

    public boolean removeListDataListener(ListDataListener o);

    public void add(int index, Integer element);

    public boolean add(Integer o);

    public boolean addAll(Collection<? extends Integer> c);

    public boolean addAll(int index, Collection<? extends Integer> c);

    public void clear();

    public boolean contains(Object o);

    public boolean containsAll(Collection<?> c);

    public Integer get(int index);

    public int indexOf(Object o);

    public boolean isEmpty();

    public Iterator<Integer> iterator();

    public int lastIndexOf(Object o);

    public ListIterator<Integer> listIterator();

    public ListIterator<Integer> listIterator(int index);

    public Integer remove(int index);

    public boolean remove(Object o);

    public boolean removeAll(Collection<?> c);

    public boolean retainAll(Collection<?> c);

    public Integer set(int index, Integer element);

    public int size();

    public List<Integer> subList(int fromIndex, int toIndex);

    public Object[] toArray();

    public <T> T[] toArray(T[] a);

    public List<Integer> getListValues();

    public int[] getListArray();

}
