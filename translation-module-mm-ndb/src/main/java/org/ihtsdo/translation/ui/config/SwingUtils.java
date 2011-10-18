package org.ihtsdo.translation.ui.config;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JPanel;

public class SwingUtils {
	
	public static void disabledAllComponents(JPanel panel, boolean disable){
		Component[] components = panel.getComponents();
		for (Component component : components) {
			if(component instanceof JPanel){
				disabledAllComponents((JPanel)component, disable);
			}
			component.setEnabled(!disable);
		}
	}

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
