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

package org.dwfa.bpa.tasks;

import java.awt.FileDialog;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.PropertyChangeSupportWithPropagationId;
import org.ihtsdo.ttk.lookup.LookupService;

/**
 * @author kec
 * 
 */
public abstract class AbstractTask implements I_DefineTask {
    private static final Logger logger = Logger.getLogger(I_DefineTask.class.getName());
    private int id = -1;

    public AbstractTask() {

    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getLogger()
     */
    @Override
    public Logger getLogger() {
        return logger;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#isLoggable(java.util.logging.Level)
     */
    @Override
    public boolean isLoggable(Level level) {
        return logger.isLoggable(level);
    }

    // export VALUES with these two items
    private static final Condition[] contiueConditionArray = { Condition.CONTINUE };
    private static final Condition[] itemSkipOrCompleteArray = { Condition.ITEM_COMPLETE, Condition.ITEM_SKIPPED };
    private static final Condition[] itemCancelOrCompleteArray = { Condition.ITEM_COMPLETE, Condition.ITEM_CANCELED };
    private static final Condition[] conditionalTestConditionArray = { Condition.FALSE, Condition.TRUE };
    private static final Condition[] conditionalTestConditionReverse = { Condition.TRUE, Condition.FALSE };
    private static final Condition[] stopConditionArray = { Condition.STOP };
    private static final Condition[] completeConditionArray = { Condition.PROCESS_COMPLETE };
    private static final Condition[] waitForWebFormArray = { Condition.WAIT_FOR_WEB_FORM };
    private static final Condition[] prevContinueCancelArray = { Condition.PREVIOUS, Condition.CONTINUE,
                                                                Condition.ITEM_CANCELED };
    private static final Condition[] continueCancelArray = { Condition.CONTINUE, Condition.ITEM_CANCELED };
    private static final Condition[] prevTrueFalseArray = { Condition.PREVIOUS, Condition.TRUE, Condition.FALSE };

    public static final List<Condition> CONTINUE_CONDITION = Collections.unmodifiableList(Arrays.asList(contiueConditionArray));
    public static final List<Condition> ITEM_SKIPPED_OR_COMPLETE = Collections.unmodifiableList(Arrays.asList(itemSkipOrCompleteArray));
    public static final List<Condition> ITEM_CANCELED_OR_COMPLETE = Collections.unmodifiableList(Arrays.asList(itemCancelOrCompleteArray));
    public static final List<Condition> CONDITIONAL_TEST_CONDITIONS = Collections.unmodifiableList(Arrays.asList(conditionalTestConditionArray));
    public static final List<Condition> CONDITIONAL_TEST_CONDITIONS_REVERSE = Collections.unmodifiableList(Arrays.asList(conditionalTestConditionReverse));
    public static final List<Condition> STOP_CONDITION = Collections.unmodifiableList(Arrays.asList(stopConditionArray));
    public static final List<Condition> COMPLETE_CONDITION = Collections.unmodifiableList(Arrays.asList(completeConditionArray));

    public static final List<Condition> WAIT_FOR_WEB_FORM_CONDITION = Collections.unmodifiableList(Arrays.asList(waitForWebFormArray));

    public static final List<Condition> PREVIOUS_CONTINUE_CANCEL = Collections.unmodifiableList(Arrays.asList(prevContinueCancelArray));

    public static final List<Condition> CONTINUE_CANCEL = Collections.unmodifiableList(Arrays.asList(continueCancelArray));

    public static final List<Condition> PREVIOUS_TRUE_FALSE = Collections.unmodifiableList(Arrays.asList(prevTrueFalseArray));

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);
    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupportWithPropagationId(this);

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(this.id);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.id = in.readInt();
            this.vetoSupport = new VetoableChangeSupport(this);
            this.changeSupport = new PropertyChangeSupportWithPropagationId(this);
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }
    
    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getName()
     */
    @Override
    public final String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getId()
     */
    @Override
    public int getId() {
        return this.id;
    }
    
    public final String getIdAndName() {
        return this.id + ": " + getName();
    }

    /**
     * @throws PropertyVetoException
     * @see org.dwfa.bpa.process.I_DefineTask#setId(int)
     */
    @Override
    public void setId(int id) throws PropertyVetoException {
        int oldId = this.id;
        this.vetoSupport.fireVetoableChange("id", oldId, id);
        this.id = id;
        this.changeSupport.firePropertyChange("id", oldId, this.id);
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @return
     */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return changeSupport.getPropertyChangeListeners();
    }

    /**
     * @param propertyName
     * @return
     */
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return changeSupport.getPropertyChangeListeners(propertyName);
    }

    /**
     * @param listener
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * @param listener
     */
    @Override
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        vetoSupport.addVetoableChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     */
    @Override
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        vetoSupport.addVetoableChangeListener(propertyName, listener);
    }

    /**
     * @return
     */
    @Override
    public VetoableChangeListener[] getVetoableChangeListeners() {
        return vetoSupport.getVetoableChangeListeners();
    }

    /**
     * @param propertyName
     * @return
     */
    @Override
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return vetoSupport.getVetoableChangeListeners(propertyName);
    }

    /**
     * @param listener
     */
    @Override
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        vetoSupport.removeVetoableChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     */
    @Override
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        vetoSupport.removeVetoableChangeListener(propertyName, listener);
    }

    /**
     * @param evt
     */
    protected void firePropertyChange(PropertyChangeEvent evt) {
        changeSupport.firePropertyChange(evt);
    }

    /**
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    protected void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    protected void firePropertyChange(String propertyName, int oldValue, int newValue) {
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * @param evt
     * @throws java.beans.PropertyVetoException
     */
    protected void fireVetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        vetoSupport.fireVetoableChange(evt);
    }

    /**
     * @param propertyName
     * @param oldValue
     * @param newValue
     * @throws java.beans.PropertyVetoException
     */
    protected void fireVetoableChange(String propertyName, boolean oldValue, boolean newValue)
            throws PropertyVetoException {
        vetoSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }

    /**
     * @param propertyName
     * @param oldValue
     * @param newValue
     * @throws java.beans.PropertyVetoException
     */
    protected void fireVetoableChange(String propertyName, int oldValue, int newValue) throws PropertyVetoException {
        vetoSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }

    /**
     * @param propertyName
     * @param oldValue
     * @param newValue
     * @throws java.beans.PropertyVetoException
     */
    protected void fireVetoableChange(String propertyName, Object oldValue, Object newValue)
            throws PropertyVetoException {
        vetoSupport.fireVetoableChange(propertyName, oldValue, newValue);
    }

    protected Object getService(I_Work worker, Class<?> interfaceClass, String message, long timeout) throws IOException, TaskFailedException {
        return LookupService.get().lookup(interfaceClass);
    }

    /**
     * @param termConfig
     * @throws FileNotFoundException
     * @throws IOException
     * @throws TaskFailedException
     */
    protected void promptUserAndWriteObjectToDisk(Object termConfig, String prompt, String directory, String defaultName)
            throws FileNotFoundException, IOException, TaskFailedException {
        // Create a file dialog box to prompt for a new file to display
        FileDialog f = new FileDialog(new JFrame(), prompt, FileDialog.SAVE);
        f.setDirectory(System.getProperty("user.dir") + System.getProperty("file.separator") + directory);
        f.setName(defaultName);
        f.setVisible(true); // Display dialog and wait for response
        if (f.getFile() != null) {
            File processBinaryFile = new File(f.getDirectory(), f.getFile());
            FileOutputStream fos = new FileOutputStream(processBinaryFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
                    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                        oos.writeObject(termConfig);
                    }
        } else {
            throw new TaskFailedException("User did not select file");
        }

    }

    @Override
    public String toString() {
        return this.getName() + " id: " + id;
    }

    /**
     * @throws IntrospectionException
     * @see org.dwfa.bpa.process.I_DefineTask#getBeanInfo()
     */
    @Override
    public BeanInfo getBeanInfo() throws IntrospectionException {
        return Introspector.getBeanInfo(this.getClass());
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getAllPropertiesBeanInfo()
     */
    @Override
    public BeanInfo getAllPropertiesBeanInfo() throws IntrospectionException {
        return Introspector.getBeanInfo(this.getClass());
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    @Override
    public int[] getDataContainerIds() {
        throw new UnsupportedOperationException();
    }

    protected <T> T getProperty(I_EncodeBusinessProcess process, Class<T> type, String propertyName)
            throws TerminologyException {
        try {
            Object rawProperty = process.readProperty(propertyName);
            if (rawProperty == null) {
                throw new TerminologyException("The property '" + propertyName + "' has not been specified.");
            }
            try {
                return type.cast(rawProperty);
            } catch (ClassCastException e) {
                throw new TerminologyException("Incorrect type for property '" + propertyName + "'. Expected "
                    + type.getName() + " but got " + rawProperty.getClass().getName());
            }
        } catch (TerminologyException e) {
            throw e;
        } catch (Exception e) {
            throw new TerminologyException("Invalid property. " + e.getMessage(), e);
        }
    }
}
