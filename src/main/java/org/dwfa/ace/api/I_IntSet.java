package org.dwfa.ace.api;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public interface I_IntSet extends ListDataListener {

	public boolean contains(int key);

	public int[] getSetValues();

	public void add(int key);

	public void remove(int key);

	public void addAll(int[] keys);

	public void removeAll(int[] keys);

	public void clear();

	public void contentsChanged(ListDataEvent e);

	public void intervalAdded(ListDataEvent e);

	public void intervalRemoved(ListDataEvent e);

	public boolean addListDataListener(ListDataListener o);

	public boolean removeListDataListener(ListDataListener o);

}