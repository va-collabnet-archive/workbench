package org.dwfa.ace.search;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.gui.TaskPanel.EditorGlue;

public class CriterionPanel extends JPanel {

    public I_TestSearchResults bean;

    public class CriterionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {
                editorPanel.removeAll();
                editorPanel.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.weightx = 1.0;
                gbc.weighty = 0;

                String menuItem = (String) criterionCombo.getSelectedItem();
                BeanInfo searchInfo = menuInfoMap.get(menuItem);
                bean = menuBeanMap.get(menuItem);

                for (PropertyDescriptor pd : searchInfo.getPropertyDescriptors()) {
                    gbc.weightx = 0.0;
                    JLabel editorLabel = new JLabel(pd.getDisplayName());
                    editorLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 2, 5));
                    editorLabel.setToolTipText(pd.getShortDescription());
                    editorPanel.add(editorLabel, gbc);
                    gbc.weightx = 1.0;
                    gbc.gridx++;
                    PropertyEditor editor = pd.createPropertyEditor(bean);
                    if (AbstractButton.class.isAssignableFrom(editor.getCustomEditor().getClass())) {
                        gbc.weightx = 0.0;
                    }

                    Method readMethod = pd.getReadMethod();
                    Object value = readMethod.invoke(bean, (Object[]) null);
                    editor.setValue(value);

                    editor.addPropertyChangeListener(new EditorGlue(editor, pd.getWriteMethod(), bean));
                    JComponent editorComponent = (JComponent) editor.getCustomEditor();
                    editorComponent.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 8));
                    editorPanel.add(editorComponent, gbc);

                    gbc.gridx++;
                }
                editorPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
                editorPanel.invalidate();
                editorPanel.validate();
                editorPanel.doLayout();

                CriterionPanel.this.invalidate();
                CriterionPanel.this.validate();
                CriterionPanel.this.doLayout();
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    List<I_TestSearchResults> criterionOptions;

    Map<I_TestSearchResults, BeanInfo> criterionMap = new HashMap<I_TestSearchResults, BeanInfo>();

    Map<String, BeanInfo> menuInfoMap = new HashMap<String, BeanInfo>();

    Map<String, I_TestSearchResults> menuBeanMap = new HashMap<String, I_TestSearchResults>();

    List<String> comboItems = new ArrayList<String>();

    JPanel editorPanel = new JPanel();

    private JComboBox criterionCombo;

    public CriterionPanel(SearchPanel searchPanel, I_TestSearchResults beanToSet) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        super(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;

        JButton addButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/add2.png")));
        addButton.setIconTextGap(0);
        addButton.addActionListener(new AddCriterion(searchPanel));
        add(addButton, gbc);
        gbc.gridx++;
        JButton removeButton = new JButton(new ImageIcon(ACE.class.getResource("/16x16/plain/delete2.png")));
        removeButton.addActionListener(new RemoveCriterion(searchPanel, this));
        removeButton.setIconTextGap(0);
        add(removeButton, gbc);

        gbc.gridx++;

        setupCriterionOptions();

        criterionCombo = new JComboBox(comboItems.toArray()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void setSize(Dimension d) {
				d.width = getMinimumSize().width;
				super.setSize(d);
			}

			@Override
			public void setSize(int width, int height) {
				super.setSize(getMinimumSize().width, height);
			}

			@Override
			public void setBounds(int x, int y, int width, int height) {
				super.setBounds(x, y, getMinimumSize().width, height);
			}

			@Override
			public void setBounds(Rectangle r) {
				r.width  = getMinimumSize().width;
				super.setBounds(r);
			}

			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.width  = getMinimumSize().width;
				return d;
			}
        	
        };
        criterionCombo.setMinimumSize(new Dimension(175,20));
        gbc.fill = GridBagConstraints.BOTH;
        add(criterionCombo, gbc);
        criterionCombo.addActionListener(new CriterionListener());
        gbc.fill = GridBagConstraints.NONE;
        if (beanToSet != null) {
            for (String comboItem : comboItems) {
                I_TestSearchResults menuBean = menuBeanMap.get(comboItem);
                if (menuBean.getClass().equals(beanToSet.getClass())) {
                    menuBeanMap.put(comboItem, beanToSet);
                    BeanInfo info = criterionMap.remove(menuBean);
                    criterionMap.put(beanToSet, info);
                    
                    int index = criterionOptions.indexOf(menuBean);
                    criterionOptions.set(index, beanToSet);
                    
                    criterionCombo.setSelectedItem(comboItem);
                    break;
                }
            }
        } else {
            criterionCombo.setSelectedIndex(0);
        }

        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx++;
        add(editorPanel, gbc);
    }

    public CriterionPanel(SearchPanel searchPanel) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException {
        this(searchPanel, null);
    }

    @SuppressWarnings("unchecked")
    private void setupCriterionOptions() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        File searchPluginFolder = new File("search");
        criterionOptions = new ArrayList<I_TestSearchResults>();
        File[] searchPlugins = searchPluginFolder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".task");
            }
        });
        if (searchPlugins != null) {
	        for (File plugin : searchPlugins) {
	            try {
	                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(plugin)));
	                Object pluginObj = ois.readObject();
	                ois.close();
	                if (I_TestSearchResults.class.isAssignableFrom(pluginObj.getClass())) {
	                    criterionOptions.add((I_TestSearchResults) pluginObj);
	                }
	            } catch (IOException ex) {
	                AceLog.getAppLog().alertAndLogException(ex);
	            } catch (ClassNotFoundException ex) {
	                AceLog.getAppLog().alertAndLogException(ex);
	            }
	        }
        } else {
        	AceLog.getAppLog().alertAndLogException(this, new Exception("No search plugins in folder: " 
        			+ searchPluginFolder.getAbsolutePath()));
        }
        for (I_TestSearchResults bean : criterionOptions) {
            String searchInfoClassName = bean.getClass().getName() + "SearchInfo";
            Class<BeanInfo> searchInfoClass = (Class<BeanInfo>) bean.getClass().getClassLoader()
                    .loadClass(searchInfoClassName);
            BeanInfo searchInfo = searchInfoClass.newInstance();
            comboItems.add(searchInfo.getBeanDescriptor().getDisplayName());
            menuInfoMap.put(searchInfo.getBeanDescriptor().getDisplayName(), searchInfo);
            menuBeanMap.put(searchInfo.getBeanDescriptor().getDisplayName(), bean);
        }
    }

    public I_TestSearchResults getBean() {
        return bean;
    }
}
