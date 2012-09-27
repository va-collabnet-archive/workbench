/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.rules.test;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The Class MemoriousJTabbedPane.
 */
public class MemoriousJTabbedPane extends JTabbedPane {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The registered indexes. */
	public List<Integer> registeredIndexes = new LinkedList<Integer>();

	/** The change listener. */
	public ChangeListener changeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent changeEvent) {
			registeredIndexes.add(getSelectedIndex());
		}
	};

	/**
	 * Instantiates a new memorious j tabbed pane.
	 */
	public MemoriousJTabbedPane() {
		super();
		this.addChangeListener(changeListener);
	}

	/**
	 * Instantiates a new memorious j tabbed pane.
	 *
	 * @param tabPlacement the tab placement
	 * @param tabLayoutPolicy the tab layout policy
	 */
	public MemoriousJTabbedPane(int tabPlacement, int tabLayoutPolicy) {
		super(tabPlacement, tabLayoutPolicy);
		this.addChangeListener(changeListener);
	}

	/**
	 * Instantiates a new memorious j tabbed pane.
	 *
	 * @param tabPlacement the tab placement
	 */
	public MemoriousJTabbedPane(int tabPlacement) {
		super(tabPlacement);
		this.addChangeListener(changeListener);
	}

	/**
	 * Memorize index.
	 *
	 * @param index the index
	 */
	public void memorizeIndex(Integer index) {
		registeredIndexes.add(index);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JTabbedPane#removeTabAt(int)
	 */
	@Override
	public void removeTabAt(int index) {
		boolean removedTabWasSelected = (index == getSelectedIndex());
		// Clean references to removed tab
		while (registeredIndexes.remove(new Integer(index))); // NOSONAR
		// Remove tab with superclass method
		super.removeTabAt(index);
		// Do nothing if the first and only selected tab was removed
		if (registeredIndexes.size() > 1 && removedTabWasSelected) {
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
