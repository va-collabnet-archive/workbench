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
/*
 * Created on Mar 18, 2005
 */
package org.dwfa.bpa;

import java.awt.Image;
import java.awt.Rectangle;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.PropertyVetoException;
import java.beans.SimpleBeanInfo;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.bpa.gui.SimpleMessageRenderer;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_DescribeBusinessProcess;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_ManageVetoableProperties;
import org.dwfa.bpa.process.I_RenderDocumentation;
import org.dwfa.bpa.process.I_RenderMessage;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.Priority;
import org.dwfa.bpa.process.ProcessID;
import org.dwfa.bpa.process.PropertySpec;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.process.PropertySpec.SourceType;
import org.dwfa.bpa.tasks.deadline.SetDeadlineRelative;
import org.dwfa.bpa.tasks.editor.AttachmentNameReadOnlyEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelEditor;
import org.dwfa.bpa.tasks.editor.PropertyNameLabelFrozenEditor;
import org.dwfa.bpa.tasks.util.ChangeProcessInstanceId;
import org.dwfa.util.bean.PropertyChangeSupportWithPropagationId;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.db.DbDependency;

/**
 * @author kec
 * 
 */
public class BusinessProcess implements I_EncodeBusinessProcess, VetoableChangeListener {

    public class AttachmentGlue {
        private String attachmentKey;

        /**
         * @param key
         */
        public AttachmentGlue(String key) {
            super();
            attachmentKey = key;
        }

        public Object read() {
            return BusinessProcess.this.readAttachement(attachmentKey);
        }

        public void write(Object attachment) {
            BusinessProcess.this.writeAttachment(attachmentKey, attachment);
        }

        public Object getAttachmentKey() {
            return "A: " + attachmentKey;
        }

        public Method getReadMethod() throws SecurityException, NoSuchMethodException {
            return this.getClass().getMethod("read", new Class[] {});
        }

        public Method getWriteMethod() throws SecurityException, NoSuchMethodException {
            return this.getClass().getMethod("write", new Class[] { Object.class });
        }
    }

    public class ProcessBeanInfo extends AllPropertiesForProcessBeanInfo {

        public ProcessBeanInfo() {
            super();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
         */
        public PropertyDescriptorWithTarget[] getPropertyDescriptors() {
            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            for (PropertyDescriptor d : super.getPropertyDescriptors()) {
                PropertyDescriptorWithTarget dwt = (PropertyDescriptorWithTarget) d;
                if (d != null && d.getPropertyEditorClass() != null
                    && PropertyNameLabelEditor.class.isAssignableFrom(d.getPropertyEditorClass())) {
                    if (AttachmentGlue.class.isAssignableFrom(dwt.getTarget().getClass())) {
                        d.setPropertyEditorClass(AttachmentNameReadOnlyEditor.class);
                        AttachmentGlue ag = (AttachmentGlue) dwt.getTarget();
                        try {
                            d.setReadMethod(ag.getClass().getMethod("getAttachmentKey", new Class[] {}));
                        } catch (SecurityException e) {
                            throw new RuntimeException(e);
                        } catch (IntrospectionException e) {
                            throw new RuntimeException(e);
                        } catch (NoSuchMethodException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                PropertySpec propSpec = PropertySpec.make(dwt, dwt.getTarget());

                if (isPropertyExternal(propSpec)) {
                    for (PropertySpec exportSpec : externalProperties) {
                        if (exportSpec.equals(propSpec)) {
                            if (exportSpec.getExternalName() != null) {
                                d.setDisplayName(exportSpec.getExternalName());
                            }
                            if (exportSpec.getShortDescription() != null) {
                                d.setShortDescription(exportSpec.getShortDescription());
                            }
                            break;
                        }
                    }
                    propertyDescriptorList.add(d);
                }
            }
            return propertyDescriptorList.toArray(new PropertyDescriptorWithTarget[propertyDescriptorList.size()]);
        }

    }

    public class AllPropertiesForProcessBeanInfo extends SimpleBeanInfo {

        public AllPropertiesForProcessBeanInfo() {
            super();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getAdditionalBeanInfo()
         */
        public BeanInfo[] getAdditionalBeanInfo() {
            return super.getAdditionalBeanInfo();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getBeanDescriptor()
         */
        public BeanDescriptor getBeanDescriptor() {
            BeanDescriptor bd = new BeanDescriptor(BusinessProcess.class);
            bd.setDisplayName("<html><font color='green'><u><center>" + name);
            return bd;
        }

        /**
         * @see java.beans.SimpleBeanInfo#getDefaultEventIndex()
         */
        public int getDefaultEventIndex() {
            return super.getDefaultEventIndex();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getDefaultPropertyIndex()
         */
        public int getDefaultPropertyIndex() {
            return super.getDefaultPropertyIndex();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getEventSetDescriptors()
         */
        public EventSetDescriptor[] getEventSetDescriptors() {
            return super.getEventSetDescriptors();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getIcon(int)
         */
        public Image getIcon(int arg0) {
            return super.getIcon(arg0);
        }

        /**
         * @see java.beans.SimpleBeanInfo#getMethodDescriptors()
         */
        public MethodDescriptor[] getMethodDescriptors() {

            return super.getMethodDescriptors();
        }

        /**
         * @see java.beans.SimpleBeanInfo#getPropertyDescriptors()
         */
        public PropertyDescriptorWithTarget[] getPropertyDescriptors() {
            List<PropertyDescriptor> propertyDescriptorList = new ArrayList<PropertyDescriptor>();
            Iterator<I_DefineTask> taskItr = getTasks().iterator();
            while (taskItr.hasNext()) {
                I_DefineTask task = taskItr.next();
                try {
                    PropertyDescriptor[] taskPropDescs = task.getBeanInfo().getPropertyDescriptors();
                    if (taskPropDescs != null) {
                        for (int i = 0; i < taskPropDescs.length; i++) {
                            PropertyDescriptorWithTarget processPropDesc = new PropertyDescriptorWithTarget(
                                taskPropDescs[i].getName(), task, taskPropDescs[i].getReadMethod(),
                                taskPropDescs[i].getWriteMethod());
                            processPropDesc.setBound(taskPropDescs[i].isBound());
                            processPropDesc.setConstrained(taskPropDescs[i].isConstrained());
                            processPropDesc.setDisplayName(taskPropDescs[i].getDisplayName());
                            processPropDesc.setExpert(taskPropDescs[i].isExpert());
                            processPropDesc.setHidden(taskPropDescs[i].isHidden());
                            processPropDesc.setName(taskPropDescs[i].getName());
                            processPropDesc.setPreferred(taskPropDescs[i].isPreferred());
                            processPropDesc.setPropertyEditorClass(taskPropDescs[i].getPropertyEditorClass());
                            propertyDescriptorList.add(processPropDesc);
                            PropertySpec taskPropSpec = propertySpecs.get(processPropDesc.getKey());
                            if (processPropDesc.getKey() == null || taskPropSpec == null) {
                                taskPropSpec = PropertySpec.make(taskPropDescs[i], task);
                                if (propertySpecs.containsKey(taskPropSpec.getKey())) {
                                    taskPropSpec = propertySpecs.get(taskPropSpec.getKey());
                                } else {
                                    propertySpecs.put(taskPropSpec.getKey(), taskPropSpec);
                                    taskPropSpec.setExternalToolTip(taskPropDescs[i].getShortDescription());
                                }
                            }
                            processPropDesc.setValue(PropertyDescriptorWithTarget.VALUE.PROPERTY_SPEC.name(),
                                taskPropSpec);
                            processPropDesc.setShortDescription(taskPropDescs[i].getShortDescription());
                        }
                    }
                } catch (IntrospectionException e) {
                    logger.log(Level.SEVERE, "Processing " + task.getName(), e);
                }
            }
            for (Map.Entry<String, ?> entry : attachments.entrySet()) {
                try {
                    String desc = entry.getKey();
                    AttachmentGlue glue = new AttachmentGlue(desc);
                    PropertyDescriptorWithTarget processPropDesc = new PropertyDescriptorWithTarget(desc, glue,
                        glue.getReadMethod().getName(), glue.getWriteMethod().getName());
                    processPropDesc.setBound(false);
                    processPropDesc.setConstrained(false);
                    processPropDesc.setExpert(false);
                    processPropDesc.setHidden(false);
                    processPropDesc.setPreferred(true);
                    processPropDesc.setPropertyEditorClass(PropertyNameLabelFrozenEditor.class);
                    processPropDesc.setName(desc);
                    processPropDesc.setDisplayName("A: " + desc);
                    PropertySpec attachmentSpec = propertySpecs.get(processPropDesc.getKey());
                    if (processPropDesc.getKey() == null || attachmentSpec == null) {
                        attachmentSpec = PropertySpec.make(processPropDesc, glue);
                        if (propertySpecs.containsKey(attachmentSpec.getKey())) {
                            attachmentSpec = propertySpecs.get(attachmentSpec.getKey());
                        } else {
                            String shortDesc = "Attachment with key: " + desc;
                            attachmentSpec.setExternalToolTip(shortDesc);
                            attachmentSpec.setShortDescription(shortDesc);
                            processPropDesc.setShortDescription(shortDesc);
                            propertySpecs.put(attachmentSpec.getKey(), attachmentSpec);
                        }
                    } else {
                        if (attachmentSpec.getPropertyName().equals(processPropDesc.getName()) == false) {
                            throw new RuntimeException(attachmentSpec.getPropertyName() + " does not equal "
                                + processPropDesc.getName());
                        }
                    }
                    processPropDesc.setValue(PropertyDescriptorWithTarget.VALUE.PROPERTY_SPEC.name(), attachmentSpec);
                    propertyDescriptorList.add(processPropDesc);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.toString(), e);
                }
            }
            return propertyDescriptorList.toArray(new PropertyDescriptorWithTarget[propertyDescriptorList.size()]);
        }

        /**
         * @see java.beans.SimpleBeanInfo#loadImage(java.lang.String)
         */
        public Image loadImage(String arg0) {
            return super.loadImage(arg0);
        }

    }

    private static Logger logger = Logger.getLogger(I_EncodeBusinessProcess.class.getName());

    protected List<TaskInfo> taskInfoList = new ArrayList<TaskInfo>();

    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupportWithPropagationId(this);

    private transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);

    private Condition exitCondition;

    private transient Condition[] values;

    private transient List<Condition> conditions;

    private ProcessID processID;

    private int id = -1;

    private String name;

    private int currentTaskId = 0;

    // version 2
    private Date deadline = new Date(Long.MAX_VALUE);

    private Priority priority = Priority.NORMAL;

    private String destination;

    // version 3
    private int lastTaskId = -1;

    // version 4

    private String originator;

    private String subject;

    // version 5
    private Map<String, Object> attachments = new HashMap<String, Object>();

    // version 6
    private HashSet<PropertySpec> externalProperties = new HashSet<PropertySpec>();

    // version 7
    private int firstTaskId = 0;

    // version 8
    private boolean continueUntilComplete = false;

    // version 9
    private I_RenderMessage renderer;

    // version 10
    private Map<String, PropertySpec> propertySpecs = new HashMap<String, PropertySpec>();

    // version 11
    private String docSource;
    
    // version 12
    private Collection<DbDependency> dbDependencies;

	// transient and static
    private transient I_DefineTask lastTaskAdded;

    private transient I_DefineTask lastTaskRemoved;

    private transient boolean addDefaultTasks;

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 12;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(BusinessProcess.dataVersion);
        out.writeObject(this.taskInfoList);
        out.writeObject(null); // was data container list
        out.writeObject(this.exitCondition);
        out.writeObject(this.processID);
        out.writeInt(this.id);
        out.writeObject(this.name);
        out.writeInt(this.getCurrentTaskId());
        out.writeObject(this.deadline);
        out.writeObject(this.priority);
        out.writeObject(this.destination);
        out.writeInt(this.lastTaskId);
        out.writeObject(this.originator);
        out.writeObject(this.subject);
        out.writeObject(this.attachments);
        out.writeObject(this.externalProperties);
        out.writeInt(this.firstTaskId);
        out.writeBoolean(this.continueUntilComplete);
        out.writeObject(this.renderer);
        out.writeObject(this.propertySpecs);
        out.writeObject(this.docSource);
        out.writeObject(this.dbDependencies);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if ((objDataVersion > 0) && (objDataVersion <= dataVersion)) {
            this.taskInfoList = (List<TaskInfo>) in.readObject();
            try {
                in.readObject(); // was data container list
            } catch (Exception e) {
                // ignore any exceptions thrown since we don't support this
                // List<TaskInfo> obj anymore
            }
            this.changeSupport = new PropertyChangeSupportWithPropagationId(this);
            this.vetoSupport = new VetoableChangeSupport(this);
            this.exitCondition = (Condition) in.readObject();
            this.values = new Condition[] { exitCondition };
            this.conditions = Collections.unmodifiableList(Arrays.asList(values));
            if (objDataVersion < 7) {
                this.processID = new ProcessID((UUID) in.readObject());
            } else {
                this.processID = (ProcessID) in.readObject();
            }
            this.id = in.readInt();
            this.name = (String) in.readObject();
            this.setCurrentTaskId(in.readInt());
            if ((objDataVersion > 0)) {
                this.deadline = (Date) in.readObject();
                this.priority = (Priority) in.readObject();
                this.destination = (String) in.readObject();
            } else {
                this.deadline = new Date(Long.MAX_VALUE);
                this.priority = Priority.NORMAL;
                this.destination = (String) in.readObject();
            }
            if ((objDataVersion > 1)) {
                this.lastTaskId = in.readInt();
            } else {
                this.lastTaskId = -1;
            }
            if ((objDataVersion > 3)) {
                this.originator = (String) in.readObject();
                this.subject = (String) in.readObject();
            } else {
                this.originator = null;
                this.subject = null;
            }
            if ((objDataVersion > 4)) {
                this.attachments = (Map<String, Object>) in.readObject();
            } else {
                this.attachments = new HashMap<String, Object>();
            }
            if ((objDataVersion > 5)) {
                this.externalProperties = (HashSet<PropertySpec>) in.readObject();
            } else {
                this.externalProperties = new HashSet<PropertySpec>();
            }
            if ((objDataVersion > 6)) {
                this.firstTaskId = in.readInt();
            } else {
                this.firstTaskId = 0;
            }
            if ((objDataVersion > 7)) {
                this.continueUntilComplete = in.readBoolean();
            } else {
                this.continueUntilComplete = false;
            }
            if ((objDataVersion > 8)) {
                this.renderer = (I_RenderMessage) in.readObject();
            } else {
                this.renderer = null;
            }
            if ((objDataVersion > 9)) {
                this.propertySpecs = (Map) in.readObject();
            } else {
                this.propertySpecs = new HashMap<String, PropertySpec>();
                for (PropertySpec spec : externalProperties) {
                    if (spec.getType() == SourceType.ATTACHMENT) {
                        propertySpecs.put(spec.getPropertyName(), spec);
                    }
                }
            }

            if ((objDataVersion > 10)) {
                this.docSource = (String) in.readObject();
            }

            if ((objDataVersion > 11)) {
                this.dbDependencies = (Collection<DbDependency>) in.readObject();
            } else {
            	this.dbDependencies = new ArrayList<DbDependency>(0);
            }
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    /**
     * /**
     * 
     * @param processID
     *            The processID to set.
     */
    public void setProcessID(ProcessID processID) {
        ProcessID oldValue = this.processID;
        this.processID = processID;
        this.changeSupport.firePropertyChange(PROCESS_ID, oldValue, this.processID);

    }

    /**
     * @param processID
     * @param id
     * @param name
     */
    public BusinessProcess(String name, Condition exitCondition) {
        this(name, exitCondition, true);
    }

    public BusinessProcess() {
        this(null, Condition.CONTINUE, false);
    }

    public BusinessProcess(String name, Condition exitCondition, boolean addDefaultTasks) {
        super();
        this.processID = new ProcessID(UUID.randomUUID());
        this.name = name;
        this.exitCondition = exitCondition;
        this.values = new Condition[] { exitCondition };
        this.conditions = Collections.unmodifiableList(Arrays.asList(values));
        this.addDefaultTasks = addDefaultTasks;
        if (addDefaultTasks) {
            try {
                ChangeProcessInstanceId changeId = new ChangeProcessInstanceId();
                this.addTask(changeId);
                SetDeadlineRelative deadline = new SetDeadlineRelative();
                this.addTask(deadline);
                this.addBranch(changeId, deadline, Condition.CONTINUE);
            } catch (PropertyVetoException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public String getProcessIdStr() {
        return this.processID.toString();
    }

    public void setProcessIdStr(String idStr) {
        this.setProcessID(new ProcessID(UUID.fromString(idStr)));
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getPrerequisites()
     */
    public Set<?> getPrerequisites() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return conditions;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#execute(org.dwfa.bpa.process.I_Work)
     */
    public Condition execute(I_Work worker) throws TaskFailedException {
        Condition condition = this.evaluate(this, worker);
        this.complete(this, worker);
        return condition;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        if (worker.isExecutionStopFlagged()) {
            return Condition.ITEM_CANCELED;
        }
        worker.getProcessStack().push(this);
        I_DefineTask currentTask = this.getTask(this.getCurrentTaskId());
        Condition condition = Condition.CONTINUE;
        I_DefineTask branchTask = null;
        while ((currentTask != null) && (condition.equals(Condition.STOP) == false)
            && (condition.equals(Condition.PROCESS_COMPLETE) == false)
            && (condition.equals(Condition.WAIT_FOR_WEB_FORM) == false)) {
            if (worker.isExecutionStopFlagged()) {
                return Condition.ITEM_CANCELED;
            }
            if (logger.isLoggable(Level.FINE)) {
                fineLogProcessAndTask(process, currentTask, "start");
            }
            try {
                List<String> attachmentSyncList = new ArrayList<String>();
                if (I_EncodeBusinessProcess.class.isAssignableFrom(currentTask.getClass())) {
                    worker.getLogger().info("Task is process");
                    I_EncodeBusinessProcess bpAsTask = (I_EncodeBusinessProcess) currentTask;
                    for (PropertyDescriptor pd : bpAsTask.getBeanInfo().getPropertyDescriptors()) {
                        if (pd.getPropertyType().equals(String.class)
                            && PropertyDescriptorWithTarget.class.isAssignableFrom(pd.getClass())) {
                            PropertyDescriptorWithTarget pdt = (PropertyDescriptorWithTarget) pd;
                            String propertyValue = (String) pd.getReadMethod().invoke(pdt.getTarget(), (Object[]) null);

                            if (propertyValue.startsWith("A: ")) {
                                // if the value is the name of an attachment,
                                // then synchronize

                                // Synchronize attachment here...
                                String attachmentName = propertyValue.substring("A: ".length());
                                attachmentSyncList.add(attachmentName);
                                bpAsTask.writeAttachment(attachmentName, process.readAttachement(attachmentName));
                            } else if (pdt.getValue("PropertySpec") != null) {
                                PropertySpec ps = (PropertySpec) pdt.getValue("PropertySpec");
                                if (ps.getType() == SourceType.ATTACHMENT) {
                                    // if the value is an attachment, then
                                    // synchronize
                                    attachmentSyncList.add(ps.getPropertyName());
                                    bpAsTask.writeAttachment(ps.getPropertyName(),
                                        process.readAttachement(ps.getPropertyName()));
                                }
                            }
                        }
                    }
                }
                condition = currentTask.evaluate(this, worker);
                if (logger.isLoggable(Level.FINE)) {
                    fineLogProcessAndTask(process, currentTask, "evaluate: " + condition);
                }
                if (continueUntilComplete) {
                    if (condition.equals(Condition.STOP_THEN_REPEAT)) {
                        if (logger.isLoggable(Level.FINE)) {
                            fineLogProcessAndTask(process, currentTask, "Continue for Stop then Repeat ");
                        }
                        condition = Condition.CONTINUE;
                    }
                }
                if (condition.isBranchCondition()) {
                    branchTask = getTaskForCondition(this.getCurrentTaskId(), condition);
                    if (branchTask == null) {
                        TaskFailedException ex = new TaskFailedException(worker.getWorkerDesc()
                            + "Malformed process. Branch missing for condition: " + condition + " on task "
                            + currentTask.getName() + " (" + currentTask.getId() + ")");
                        logger.throwing(this.getName(), "evaluate", ex);
                        throw ex;
                    }
                    this.setCurrentTaskId(branchTask.getId());
                }
                ExecutionRecord er = new ExecutionRecord(new Date(), currentTask.getId(), worker.getId(),
                    worker.getWorkerDesc(), condition);
                TaskInfo info = this.taskInfoList.get(currentTask.getId());
                info.addExecutionRecord(er);
                currentTask.complete(this, worker);
                if (I_EncodeBusinessProcess.class.isAssignableFrom(currentTask.getClass())) {
                    I_EncodeBusinessProcess bpAsTask = (I_EncodeBusinessProcess) currentTask;
                    for (String attachmentName : attachmentSyncList) {
                        process.writeAttachment(attachmentName, bpAsTask.readAttachement(attachmentName));
                    }
                }
                if (logger.isLoggable(Level.INFO)) {
                    logger.info(worker.getWorkerDesc() + "; process: " + process.getName() + " ("
                        + process.getProcessID() + ") " + " Current task: " + currentTask.getId() + " "
                        + currentTask.getName() + " : " + condition + " Next task: " + process.getCurrentTaskId());

                }
                currentTask = branchTask;
                branchTask = null;
                if (continueUntilComplete) {
                    if (condition.equals(Condition.STOP)) {
                        condition = Condition.CONTINUE;
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine(process.getProcessID() + ": " + process.getCurrentTaskId() + "/"
                                + currentTask.getId() + " " + currentTask.getName() + " Continue for Stop");
                        }
                    }
                }
            } catch (TaskFailedException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new TaskFailedException(ex);
            }
        }
        if (condition.equals(Condition.PROCESS_COMPLETE)) {
            // Check to see if this process is embedded in another
            if (worker.getProcessStack().size() > 1) {
                // If so, reset the process so subsequent executions restart
                this.setCurrentTaskId(this.getFirstTaskId());
            }
            return this.exitCondition;
        }

        if (condition.equals(Condition.WAIT_FOR_WEB_FORM)) {
            // Check to see if this process is embedded in another
            if (worker.getProcessStack().size() > 1) {
                // If so, reset the process so subsequent executions restart
                this.setCurrentTaskId(this.getFirstTaskId());
            }
            return Condition.WAIT_FOR_WEB_FORM;
        }
        return Condition.STOP_THEN_REPEAT;
    }

    private void fineLogProcessAndTask(I_EncodeBusinessProcess process, I_DefineTask currentTask, String logMsg) {
        logger.fine(process.getProcessID() + ": " + process.getCurrentTaskId() + "/" + currentTask.getId() + " "
            + currentTask.getName() + " " + logMsg);
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
     *      org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        if (worker.getProcessStack().empty() == false) {
            worker.getProcessStack().pop();
        }
    }

    /**
     * @throws NoBranchForConditionException
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getTaskForCondition(int,
     *      org.dwfa.bpa.process.Condition)
     */
    public I_DefineTask getTaskForCondition(int id, Condition condition) throws NoBranchForConditionException {
        TaskInfo info = this.taskInfoList.get(id);
        I_DefineTask task = this.getTask(info.getTaskIdForCondition(condition));
        addThisToVetoListeners(task);
        return task;
    }

    /**
     * @throws PropertyVetoException
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#addTask(org.dwfa.bpa.process.I_DefineTask)
     */
    public I_DefineTask addTask(I_DefineTask task) throws PropertyVetoException {
        task.setId(taskInfoList.size());
        this.taskInfoList.add(new TaskInfo(task, null, null, null));
        addThisToVetoListeners(task);
        setLastTaskAdded(task);
        return task;
    }

    public void removeTask(I_DefineTask task) throws PropertyVetoException {
        TaskInfo info = this.taskInfoList.get(task.getId());
        if ((info.getExecutionRecords() != null) && (info.getExecutionRecords().size() > 0)) {
            throw new PropertyVetoException("This task has execution records", null);
        }
        // Remove references to this task;
        Iterator<TaskInfo> taskInfoItr = this.taskInfoList.iterator();
        while (taskInfoItr.hasNext()) {
            info = taskInfoItr.next();
            if (info != null) {
                ListIterator<Branch> branchItr = info.branches.listIterator();
                while (branchItr.hasNext()) {
                    Branch branch = branchItr.next();
                    if (branch.getDestinationId() == task.getId()) {
                        branchItr.remove();
                    }
                }
            }
        }
        this.taskInfoList.set(task.getId(), null);
        this.setLastTaskRemoved(task);
        if (this.currentTaskId == task.getId()) {
            this.setCurrentTaskId(-1);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getTasks()
     */
    public Collection<I_DefineTask> getTasks() {
        List<I_DefineTask> taskList = new ArrayList<I_DefineTask>(this.taskInfoList.size());
        for (TaskInfo ti : this.taskInfoList) {
            if (ti != null) {
                addThisToVetoListeners(ti.getTask());
                taskList.add(ti.getTask());
            }
        }
        return Collections.unmodifiableCollection(taskList);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getTask(int)
     */
    public I_DefineTask getTask(int taskId) {
        TaskInfo ti = this.taskInfoList.get(taskId);
        addThisToVetoListeners(ti.getTask());
        return ti.getTask();
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getProcessID()
     */
    public ProcessID getProcessID() {
        return this.processID;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getRootTask()
     */
    public I_DefineTask getRootTask() {
        return this.getTask(0);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#addBranch(org.dwfa.bpa.process.I_DefineTask,
     *      org.dwfa.bpa.process.I_DefineTask, org.dwfa.bpa.process.Condition)
     */
    public synchronized void addBranch(I_DefineTask origin, I_DefineTask destination, Condition condition) {
        TaskInfo ti = this.taskInfoList.get(origin.getId());
        ti.addBranch(destination, condition);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Added branch. \nOrigin:" + origin + "\ndestination: " + destination + "\ncondition: "
                + condition + "\nTaskInfo: " + ti);

        }
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Resulting process:\n" + this);

        }
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#removeBranch(org.dwfa.bpa.process.I_DefineTask,
     *      org.dwfa.bpa.process.I_DefineTask, org.dwfa.bpa.process.Condition)
     */
    public synchronized void removeBranch(I_DefineTask origin, I_DefineTask destination, Condition condition) {
        TaskInfo ti = this.taskInfoList.get(origin.getId());
        ti.removeBranch(destination, condition);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getBranches(org.dwfa.bpa.process.I_DefineTask)
     */
    public Collection<Branch> getBranches(I_DefineTask from) {
        TaskInfo ti = this.taskInfoList.get(from.getId());
        if (ti != null) {
            return ti.getBranches();
        }
        return new ArrayList<Branch>();
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getExecutionRecords()
     */
    public Collection<ExecutionRecord> getExecutionRecords() {
        List<ExecutionRecord> records = new ArrayList<ExecutionRecord>();
        for (TaskInfo ti : this.taskInfoList) {
            if (ti != null) {
                records.addAll(ti.getExecutionRecords());
            }
        }
        return records;
    }

    public void clearExecutionRecords() {
        for (TaskInfo ti : this.taskInfoList) {
            if (ti != null) {
                ti.clearExecutionRecords();
            }
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getExecutionRecords(int)
     */
    public Collection<ExecutionRecord> getExecutionRecords(int taskId) {
        TaskInfo ti = this.taskInfoList.get(taskId);
        return ti.getExecutionRecords();
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getName()
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getId()
     */
    public int getId() {
        return this.id;
    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(listener);

    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.removePropertyChangeListener(listener);

    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#getPropertyChangeListeners()
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return this.changeSupport.getPropertyChangeListeners();
    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#addPropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(propertyName, listener);

    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#removePropertyChangeListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.changeSupport.removePropertyChangeListener(propertyName, listener);

    }

    /**
     * @see org.dwfa.bpa.process.I_ManageProperties#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return this.changeSupport.getPropertyChangeListeners(propertyName);
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {

        // TODO Auto-generated method stub

    }

    /**
     * @throws PropertyVetoException
     * @see org.dwfa.bpa.process.I_DefineTask#setId(int)
     */
    public void setId(int id) throws PropertyVetoException {
        int oldId = this.id;
        this.vetoSupport.fireVetoableChange("id", oldId, id);
        this.id = id;
        this.changeSupport.firePropertyChange("id", oldId, this.id);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setTaskBounds(int,
     *      java.awt.Rectangle)
     */
    public void setTaskBounds(int id, Rectangle bounds) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Changing task " + id + " bounds to: " + bounds);
        }
        TaskInfo ti = this.taskInfoList.get(id);
        ti.setBounds(bounds);

    }

    public void setTaskInfo(TaskInfo ti) throws PropertyVetoException {
        try {
            if (ti == null) {
                this.taskInfoList.add(null);
            } else if (ti.getTask().getId() == -1) {
                ti.getTask().setId(this.taskInfoList.size());
                this.taskInfoList.add(ti);
            } else {
                if (this.taskInfoList.size() == ti.getTask().getId()) {
                    this.taskInfoList.add(ti);
                } else {
                    this.taskInfoList.set(ti.getTask().getId(), ti);
                }
            }
        } catch (NullPointerException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            this.taskInfoList.add(null);
        }
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getTaskBounds(int)
     */
    public Rectangle getTaskBounds(int id) {
        TaskInfo ti = this.taskInfoList.get(id);
        return ti.getBounds();
    }

    /**
     * @param currentTaskId
     *            The currentTaskId to set.
     */
    public void setCurrentTaskId(int currentTaskId) {
        this.lastTaskId = this.currentTaskId;
        this.currentTaskId = currentTaskId;
        this.changeSupport.firePropertyChange("currentTaskId", this.lastTaskId, this.currentTaskId);
    }

    /**
     * @return Returns the currentTaskId.
     */
    public int getCurrentTaskId() {
        return currentTaskId;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getNumberOfTasks()
     */
    public int getNumberOfTasks() {
        return this.taskInfoList.size();
    }

    /**
     * @throws PropertyVetoException
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#addDataContainer(org.dwfa.bpa.process.I_ContainData)
     */
    public I_ContainData addDataContainer(I_ContainData data) throws PropertyVetoException {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getDataContainer(int)
     */
    public I_ContainData getDataContainer(int id) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setDataContainerBounds(int,
     *      java.awt.Rectangle)
     */
    public void setDataContainerBounds(int id, Rectangle bounds) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param objToListenTo
     */
    private void addThisToVetoListeners(I_ManageVetoableProperties objToListenTo) {
        objToListenTo.addVetoableChangeListener("id", this);
        objToListenTo.addVetoableChangeListener("description", this);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getDataContainers()
     */
    public List<I_ContainData> getDataContainers() {
        throw new UnsupportedOperationException();
    }

    public void renameAttachment(String oldKey, String newKey) throws PropertyVetoException {
        // check to see if name is already in use
        if (this.attachments.containsKey(newKey)) {
            throw new PropertyVetoException("The newKey is already in use: " + newKey, null);
        }

        // change the external property information

        PropertySpec specToChange = propertySpecs.remove("A: " + oldKey);
        boolean isExternal = externalProperties.remove(specToChange);

        if (specToChange != null) {
            specToChange.setPropertyName(newKey);
            PropertySpec changedSpec = specToChange;
            if (isExternal) {
                externalProperties.add(changedSpec);
            }
            if (changedSpec.getShortDescription() != null) {
                changedSpec.setShortDescription(changedSpec.getShortDescription().replace(oldKey, newKey));
            }
            if (changedSpec.getExternalName() != null) {
                changedSpec.setExternalName(changedSpec.getExternalName().replace(oldKey, newKey));
            }
            if (changedSpec.getExternalToolTip() != null) {
                changedSpec.setExternalToolTip(changedSpec.getExternalToolTip().replace(oldKey, newKey));
            }
            propertySpecs.put(changedSpec.getKey(), changedSpec);
        }
        Object attachment = takeAttachment(oldKey);
        writeAttachment(newKey, attachment);
        this.changeSupport.firePropertyChange(oldKey, attachment, null);
        this.changeSupport.firePropertyChange(newKey, null, attachment);
    }

    /**
     * @see java.beans.VetoableChangeListener#vetoableChange(java.beans.PropertyChangeEvent)
     */
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if (evt.getPropertyName().equals("id")) {
            throw new PropertyVetoException("Component id cannot be changed once associated with a process.", evt);
        } else if (evt.getPropertyName().equals("description")) {
            for (String key : this.getAttachmentKeys()) {
                if (key.equals(evt.getNewValue())) {
                    throw new PropertyVetoException("Description is not unique compared to attachments: ", evt);
                }
            }

        }

    }

    /**
     * @param listener
     */
    public void addVetoableChangeListener(VetoableChangeListener listener) {
        vetoSupport.addVetoableChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     */
    public void addVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        vetoSupport.addVetoableChangeListener(propertyName, listener);
    }

    /**
     * @return
     */
    public VetoableChangeListener[] getVetoableChangeListeners() {
        return vetoSupport.getVetoableChangeListeners();
    }

    /**
     * @param propertyName
     * @return
     */
    public VetoableChangeListener[] getVetoableChangeListeners(String propertyName) {
        return vetoSupport.getVetoableChangeListeners(propertyName);
    }

    /**
     * @param propertyName
     * @return
     */
    public boolean hasListeners(String propertyName) {
        return vetoSupport.hasListeners(propertyName);
    }

    /**
     * @param listener
     */
    public void removeVetoableChangeListener(VetoableChangeListener listener) {
        vetoSupport.removeVetoableChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     */
    public void removeVetoableChangeListener(String propertyName, VetoableChangeListener listener) {
        vetoSupport.removeVetoableChangeListener(propertyName, listener);
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
     */
    public int[] getDataContainerIds() {
        return new int[] {};
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setDeadline(java.util.Date)
     */
    public void setDeadline(Date deadline) {
        this.deadline = deadline;

    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getDeadline()
     */
    public Date getDeadline() {
        return this.deadline;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setPriority(org.dwfa.bpa.process.Priority)
     */
    public void setPriority(Priority priority) {
        Priority old = this.priority;
        this.priority = priority;
        this.changeSupport.firePropertyChange("priority", old, this.priority);

    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getPriority()
     */
    public Priority getPriority() {
        return this.priority;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setDestination(java.lang.String)
     */
    public void setDestination(String address) {
        String old = this.destination;
        this.destination = address;
        this.changeSupport.firePropertyChange("destination", old, this.destination);

    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getDestination()
     */
    public String getDestination() {
        return this.destination;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getLastTaskId()
     */
    public int getLastTaskId() {
        return this.lastTaskId;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#isLoggable(java.util.logging.Level)
     */
    public boolean isLoggable(Level level) {
        return logger.isLoggable(level);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getLogger()
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * @return Returns the originator.
     */
    public String getOriginator() {
        return originator;
    }

    /**
     * @param originator
     *            The originator to set.
     */
    public void setOriginator(String originator) {
        String old = this.originator;
        this.originator = originator;
        this.changeSupport.firePropertyChange("originator", old, this.name);
    }

    /**
     * @return Returns the subject.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject
     *            The subject to set.
     */
    public void setSubject(String subject) {
        String old = this.subject;
        this.subject = subject;
        this.changeSupport.firePropertyChange("subject", old, this.priority);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#readAttachement(java.lang.String)
     */
    public Object readAttachement(String key) {
        return this.attachments.get(key);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#writeAttachment(java.lang.String,
     *      java.lang.Object)
     */
    public void writeAttachment(String key, Object attachment) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("writing attachment: " + key + " value:" + attachment);
        }
        Object oldValue = this.readAttachement(key);
        this.attachments.put(key, attachment);
        this.changeSupport.firePropertyChange(key, oldValue, attachment);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#takeAttachment(java.lang.String)
     */
    public Object takeAttachment(String key) {
        return this.attachments.remove(key);
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#getAttachmentKeys()
     */
    public Collection<String> getAttachmentKeys() {
        return Collections.unmodifiableCollection(this.attachments.keySet());
    }

    /**
     * @throws PropertyVetoException
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setName(java.lang.String)
     */
    public void setName(String name) throws PropertyVetoException {
        String oldName = this.name;
        this.vetoSupport.fireVetoableChange("name", oldName, name);
        this.name = name;
        this.changeSupport.firePropertyChange("name", oldName, this.name);

    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("Name: ");
        buff.append(this.name);
        buff.append("\nId as task: ");
        buff.append(this.id);
        buff.append("\nInstance id: ");
        buff.append(this.processID);
        buff.append("\nSubject: ");
        buff.append(this.subject);
        buff.append("\nOriginator: ");
        buff.append(this.originator);
        buff.append("\nPriority: ");
        buff.append(this.priority);
        buff.append("\nCurrent task: ");
        buff.append(this.currentTaskId);
        buff.append("\nLast task: ");
        buff.append(this.lastTaskId);
        buff.append("\nDeadline: ");
        buff.append(this.deadline);
        buff.append("\nDestination: ");
        buff.append(this.destination);
        Iterator<TaskInfo> taskItr = this.taskInfoList.iterator();
        while (taskItr.hasNext()) {
            TaskInfo info = taskItr.next();
            buff.append("\nTask: ");
            buff.append(info);
        }
        return buff.toString();
    }

    /**
     * @param process
     */
    public void validateAddresses() throws TaskFailedException {
        validateAddresses(this);

    }

    /**
     * @param process
     */
    public void validateDestination() throws TaskFailedException {
        validateAddress(this.destination, this.getProcessID());

    }

    public static void validateAddresses(I_DescribeBusinessProcess processDesc) throws TaskFailedException {
        try {
            validateAddress(processDesc.getDestination(), processDesc.getProcessID());
        } catch (TaskFailedException e) {
            throw new TaskFailedException("Invalid Destination: " + processDesc.getDestination(), e);
        }
        try {
            validateAddress(processDesc.getOriginator(), processDesc.getProcessID());
        } catch (TaskFailedException e) {
            throw new TaskFailedException("Invalid Originator: " + processDesc.getOriginator(), e);
        }
    }

    public static void validateAddress(String address, ProcessID processID) throws TaskFailedException {
        if ((address == null) || address.length() < 6) {
            throw new TaskFailedException("malformed address: " + address + " for process: " + processID);
        }
    }

    public BeanInfo getBeanInfo() throws IntrospectionException {
        return new ProcessBeanInfo();
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#isPropertyExternal(org.dwfa.bpa.process.PropertySpec)
     */
    public boolean isPropertyExternal(PropertySpec spec) {
        return this.externalProperties.contains(spec);
    }

    public PropertySpec getExternalSpec(PropertySpec key) {
        for (PropertySpec spec : externalProperties) {
            if (key.equals(spec)) {
                return spec;
            }
        }
        return null;
    }

    /**
     * @see org.dwfa.bpa.process.I_EncodeBusinessProcess#setPropertyExternal(org.dwfa.bpa.process.PropertySpec,
     *      boolean)
     */
    public void setPropertyExternal(PropertySpec spec, boolean external) {
        if (external) {
            this.externalProperties.add(spec);
        } else {
            this.externalProperties.remove(spec);
        }
        this.changeSupport.firePropertyChange("Externalize: " + spec.getKey(), !external, external);
    }

    public void setPropertyExternal(String propertyName, String sourceType, int id, boolean external) {
        PropertySpec spec = new PropertySpec(SourceType.valueOf(sourceType), id, propertyName);
        this.setPropertyExternal(spec, external);
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getAllPropertiesBeanInfo()
     */
    public BeanInfo getAllPropertiesBeanInfo() throws IntrospectionException {
        return new AllPropertiesForProcessBeanInfo();
    }

    /**
     * @return Returns the lastTaskAdded.
     */
    public I_DefineTask getLastTaskAdded() {
        return lastTaskAdded;
    }

    /**
     * @param lastTaskAdded
     *            The lastTaskAdded to set.
     */
    protected void setLastTaskAdded(I_DefineTask lastTaskAdded) {
        Object prevTask = this.lastTaskAdded;
        this.lastTaskAdded = lastTaskAdded;
        this.changeSupport.firePropertyChange("lastTaskAdded", prevTask, this.lastTaskAdded);
    }

    /**
     * @return Returns the lastTaskRemoved.
     */
    public I_DefineTask getLastTaskRemoved() {
        return lastTaskRemoved;
    }

    /**
     * @param lastTaskRemoved
     *            The lastTaskRemoved to set.
     */
    protected void setLastTaskRemoved(I_DefineTask lastTaskRemoved) {
        Object prevTask = this.lastTaskRemoved;
        this.lastTaskRemoved = lastTaskRemoved;
        this.changeSupport.firePropertyChange("lastTaskRemoved", prevTask, this.lastTaskRemoved);
    }

    /**
     * @return Returns the exitCondition.
     */
    public Condition getExitCondition() {
        return exitCondition;
    }

    /**
     * @return Returns the addDefaultTasks.
     */
    public boolean getAddDefaultTasks() {
        return addDefaultTasks;
    }

    /**
     * @param exitCondition
     *            The exitCondition to set.
     */
    public void setExitCondition(Condition exitCondition) {
        this.exitCondition = exitCondition;
    }

    /**
     * @return Returns the taskInfoList.
     */
    public List<TaskInfo> getTaskInfoList() {
        return taskInfoList;
    }

    /**
     * @return Returns the externalProperties.
     */
    public HashSet<PropertySpec> getExternalProperties() {
        return externalProperties;
    }

    /**
     * @return Returns the firstTaskId.
     */
    public int getFirstTaskId() {
        return firstTaskId;
    }

    /**
     * @param firstTaskId
     *            The firstTaskId to set.
     */
    public void setFirstTaskId(int firstTaskId) {
        this.firstTaskId = firstTaskId;
    }

    public InputStream getStreamFromURL(URL locator) throws IOException {
        String protocol = locator.getProtocol().toLowerCase();
        String host = locator.getHost().toLowerCase();
        if (protocol.equals("file") && (host.equals("attachment.bp") || host.equals("property.bp"))) {
            Object obj = this.getObjectFromURL(locator);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            return new ByteArrayInputStream(bos.toByteArray());
        } else if (protocol.equals("file")) {
            return new FileInputStream(locator.getFile());
        }
        return locator.openStream();
    }

    public Object getObjectFromURL(URL locator) throws IOException {
        String protocol = locator.getProtocol().toLowerCase();
        String host = locator.getHost().toLowerCase();
        if (protocol.equals("file") && host.equals("attachment.bp")) {
            String path = locator.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return this.readAttachement(path);
        } else if (protocol.equals("file") && host.equals("property.bp")) {
            int taskId = new Integer(locator.getAuthority());
            I_DefineTask task = this.getTask(taskId);
            String path = locator.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            try {
                BeanInfo taskInfo = task.getBeanInfo();

                for (PropertyDescriptor d : taskInfo.getPropertyDescriptors()) {
                    if (d.getName().equals(path)) {
                        return d.getReadMethod().invoke(task, new Object[] {});
                    }
                }
                throw new IOException("Can't locate object for: " + path + " from URL: " + locator);
            } catch (Exception e) {
                IOException ioe = new IOException(e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }
        } else if (protocol.equals("file") && host.equals("dataContainer.bp")) {
            String dcName = locator.getPath();
            if (dcName.startsWith("/")) {
                dcName = dcName.substring(1);
            }

        } else if (protocol.equals("file")) {
            BufferedInputStream bis = new BufferedInputStream(locator.openStream());
            ObjectInputStream ois = new ObjectInputStream(bis);
            try {
                return ois.readObject();
            } catch (Exception e) {
                IOException ioe = new IOException(e.getMessage());
                ioe.initCause(e);
                throw ioe;
            }
        } else if (protocol.equalsIgnoreCase("bpa")) {
            return this.readAttachement(locator.getPath());
        } else if (protocol.equalsIgnoreCase("tp")) {
            throw new UnsupportedOperationException();
        }

        return locator.getContent();
    }

    public UUID getObjectID() {
        return this.getProcessID().getUuid();
    }

    public Object readProperty(String propertyLabel) throws IntrospectionException, IllegalAccessException,
            InvocationTargetException {
        return getProperty(propertyLabel);
    }

    public Object getProperty(String propertyLabel) throws IntrospectionException, IllegalAccessException,
            InvocationTargetException {
        propertyLabel = propertyLabel.replaceAll("\\<.*?>", "");
        try {
            BeanInfo info = this.getAllPropertiesBeanInfo();
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                PropertyDescriptorWithTarget pdwt = (PropertyDescriptorWithTarget) pd;
                if (pdwt.getLabel().equals(propertyLabel)) {
                    Method readMethod = pdwt.getReadMethod();
                    return readMethod.invoke(pdwt.getTarget(), new Object[] {});
                }
            }
            if (propertyLabel.startsWith("A: ")) {
                String propertyName = propertyLabel.substring(3);
                return readAttachement(propertyName);
            } else if (attachments.containsKey(propertyLabel)) {
                return readAttachement(propertyLabel);
            }
            throw new IntrospectionException("Property not found: " + propertyLabel);
        } catch (IllegalArgumentException ex) {
            IntrospectionException ie = new IntrospectionException(ex.getMessage());
            ie.initCause(ex);
            throw ie;
        }
    }

    public void setProperty(String propertyLabel, Object value) throws IntrospectionException, IllegalAccessException,
            InvocationTargetException {
        propertyLabel = propertyLabel.replaceAll("\\<.*?>", "");
        try {
            BeanInfo info = this.getAllPropertiesBeanInfo();
            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                PropertyDescriptorWithTarget pdwt = (PropertyDescriptorWithTarget) pd;
                if (pdwt.getLabel().equals(propertyLabel)) {
                    Method writeMethod = pdwt.getWriteMethod();
                    writeMethod.invoke(pdwt.getTarget(), new Object[] { value });
                    this.changeSupport.firePropertyChange(propertyLabel, null, value);
                    return;
                }
            }
            if (propertyLabel.startsWith("A: ")) {
                String propertyName = propertyLabel.substring(3);
                this.writeAttachment(propertyName, value);
                this.changeSupport.firePropertyChange(propertyLabel, null, value);
                return;
            }
            throw new IntrospectionException("Property not found: " + propertyLabel);
        } catch (IllegalArgumentException ex) {
            IntrospectionException ie = new IntrospectionException(ex.getMessage());
            ie.initCause(ex);
            throw ie;
        }
    }

    public boolean getContinueUntilComplete() {
        return continueUntilComplete;
    }

    public void setContinueUntilComplete(boolean continueUntilComplete) {
        this.continueUntilComplete = continueUntilComplete;
    }

    public void setMessageRenderer(I_RenderMessage renderer) {
        this.renderer = renderer;

    }

    public I_RenderMessage getMessageRenderer() {
        if (renderer == null) {
            String[] standardMessageAttachments = new String[] { "MESSAGE", "HTML_INSTRUCTION", "HTML_DETAIL",
                                                                "SIGNPOST_HTML", "INSTRUCTION_STR", };
            for (String key : standardMessageAttachments) {
                if (attachments.containsKey(key) && attachments.get(key) != null) {
                    return new SimpleMessageRenderer((String) attachments.get(key));
                }
            }
        }
        return this.renderer;
    }

    public String getProcessDocumentationSource() {
        return docSource;
    }

    public void setProcessDocumentationSource(String docSource) {
        this.docSource = docSource;
    }

    public String getProcessDocumentation() throws Exception {
        return getProcessDocumentation(null);
    }

    public String getProcessDocumentation(I_RenderDocumentation renderer) throws Exception {
        if (renderer == null) {
            renderer = new DefaultProcessDocRenderer();
        }
        return renderer.getDocumentation(this);
    }

    public void fireDescriptorChanged(PropertyDescriptor pd) {
        this.changeSupport.firePropertyChange("PropertyDescriptor: " + pd.getName(), null, pd);
    }

    public String substituteProperties(String input) throws MalformedURLException, IOException {
        Set<String> locaters = getLocators(input);
        for (String locator : locaters) {
            Object value = getObjectFromURL(new URL(locator));
            String substitutionVariable = "${" + locator + "}";
            if (value == null) {
                input = input.replace(substitutionVariable, "[" + substitutionVariable + " is null]");
            } else {
                input = input.replace("${" + locator + "}", value.toString());
            }

        }
        return input;
    }

    public Set<String> getLocators(String input) {
        Set<String> locaters = new HashSet<String>();
        if (input != null) {
            String[] parts = input.split("\\$\\{");
            if (parts.length > 1) {
                for (int i = 1; i < parts.length; i++) {
                    String part = parts[i];
                    String locator = part.split("}", 2)[0];
                    locaters.add(locator);
                }
            }
        } else {
            logger.log(Level.WARNING, "Input is null in getLocators...");
        }
        return locaters;
    }
    
    public Collection<DbDependency> getDbDependencies() {
		return dbDependencies;
	}

	public void setDbDependencies(Collection<DbDependency> dbDependencies) {
		this.dbDependencies = dbDependencies;
	}

	@Override
	public boolean dbDependenciesAreSatisfied() throws IOException {
		return Ts.get().satisfiesDependencies(getDbDependencies());
	}

}
