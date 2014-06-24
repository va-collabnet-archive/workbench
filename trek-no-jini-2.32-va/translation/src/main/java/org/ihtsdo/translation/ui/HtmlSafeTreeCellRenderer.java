/**
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
package org.ihtsdo.translation.ui;

import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * The Class HtmlSafeTreeCellRenderer.
 */
public class HtmlSafeTreeCellRenderer extends DefaultTreeCellRenderer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new html safe tree cell renderer.
	 */
	public HtmlSafeTreeCellRenderer() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.tree.DefaultTreeCellRenderer#firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
    {
        if(propertyName.equals("text"))
        {
            super.firePropertyChange(propertyName, oldValue, newValue);
            this.updateUI(); // this is the line that made the difference
        }
    }

}
