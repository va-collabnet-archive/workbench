/*
 * Created on Mar 23, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.net.URL;

import javax.swing.JPanel;

/**
 * @author kec
 *
 */
public class StopProcessBeanInfo extends SimpleBeanInfo {

	/**
	 * 
	 */
	public StopProcessBeanInfo() {
		super();
	}
       public PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }        
    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
           BeanDescriptor bd = new BeanDescriptor(StopProcess.class);
           bd.setDisplayName("<html><font color='red'>Stop");
           
        return bd;
    }
    /**
     * @see java.beans.SimpleBeanInfo#getIcon(int)
     */
    @Override
    public Image getIcon(int iconKind) {
        switch (iconKind) {
        case BeanInfo.ICON_COLOR_32x32: 
        case BeanInfo.ICON_MONO_32x32: 
            URL iconUrl = this.getClass().getResource("/Stop32x32.gif");
            JPanel p = new JPanel();
            Toolkit tk = p.getToolkit();
            return tk.getImage(iconUrl);
            
        case BeanInfo.ICON_COLOR_16x16:
        case BeanInfo.ICON_MONO_16x16:
            iconUrl = this.getClass().getResource("/Stop16x16.gif");
            p = new JPanel();
            tk = p.getToolkit();
            return tk.getImage(iconUrl);
            
        }
        URL iconUrl = this.getClass().getResource("/Stop32x32.gif");
        JPanel p = new JPanel();
        Toolkit tk = p.getToolkit();
        return tk.getImage(iconUrl);
    }

}
