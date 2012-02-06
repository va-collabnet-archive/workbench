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
package org.ihtsdo.translation.ui;

import javax.swing.Icon;
import javax.swing.JPanel;



/**
 * The Class MenuComponentPanel.
 */
public abstract class MenuComponentPanel extends JPanel {

	/** The Constant serialVersionUID. */
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Gets the label text.
	 *
	 * @return the label text
	 */
	public abstract String getLabelText();

	/**
	 * Gets the label icon.
	 *
	 * @return the label icon
	 */
	public abstract Icon getLabelIcon();
	
	/**
	 * Sets the label text.
	 *
	 * @param labelText the new label text
	 */
	public abstract void setLabelText(String labelText);
	
	/**
	 * Sets the label icon.
	 *
	 * @param icon the new label icon
	 */
	public abstract void setLabelIcon(Icon icon);

}
