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
package org.ihtsdo.translation.ui.config;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;

/**
 * The Class SwingUtils.
 */
public class SwingUtils {
	
	/**
	 * Disabled all components.
	 *
	 * @param panel the panel
	 * @param disable the disable
	 */
	public static void disabledAllComponents(JPanel panel, boolean disable){
		Component[] components = panel.getComponents();
		for (Component component : components) {
			if(component instanceof JPanel){
				disabledAllComponents((JPanel)component, disable);
			}
			component.setEnabled(!disable);
		}
	}

	/**
	 * Gets the j panel clone.
	 *
	 * @param panel the panel
	 * @return the j panel clone
	 * @throws Exception the exception
	 */
	public JPanel getJPanelClone(JPanel panel) throws Exception {
		JPanel result = null;

		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(panel);
			oos.flush();

			ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
			ois = new ObjectInputStream(bin);
			result = (JPanel) ois.readObject();
		} catch (Exception e) {
			System.out.println("Exception in ObjectCloner = " + e);
			throw e;
		} finally {
			oos.close();
			ois.close();
		}
		return result;
	}
	
}
