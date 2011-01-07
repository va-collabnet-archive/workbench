package org.dwfa.ace;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MemoriousJTabbedPane extends JTabbedPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public List<Integer> registeredIndexes = new LinkedList<Integer>();

	public ChangeListener changeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent changeEvent) {
			registeredIndexes.add(getSelectedIndex());
		}
	};

	public MemoriousJTabbedPane() {
		super();
		this.addChangeListener(changeListener);
	}

	public MemoriousJTabbedPane(int tabPlacement, int tabLayoutPolicy) {
		super(tabPlacement, tabLayoutPolicy);
		this.addChangeListener(changeListener);
	}

	public MemoriousJTabbedPane(int tabPlacement) {
		super(tabPlacement);
		this.addChangeListener(changeListener);
	}

	public void memorizeIndex(Integer index) {
		registeredIndexes.add(index);
	}

	@Override
	public void removeTabAt(int index) {
		// Clean references to removed tab
		while (registeredIndexes.remove(new Integer(index)));
		// Remove tab with superclass method
		super.removeTabAt(index);
		// Do nothing if the first and only selected tab was removed
		if (registeredIndexes.size() > 1) {
			// Remove last selected tab, is the automatic one
			registeredIndexes.remove(registeredIndexes.size()-1);
			// Update registered indexes to the right, now that we have one less tab
			for (int i=0; i<registeredIndexes.size(); i++) {
				if (registeredIndexes.get(i) > index) {
					int outdatedValue = registeredIndexes.get(i);
					registeredIndexes.set(i, outdatedValue -1);
				}
			}
			// if lastRegisteredIndex is valid, switch to that one
			int lastRegisteredIndex = registeredIndexes.get(registeredIndexes.size()-1);
			if (lastRegisteredIndex < getTabCount() ) {
				this.setSelectedIndex(lastRegisteredIndex);
			} 
		}
	}

}
