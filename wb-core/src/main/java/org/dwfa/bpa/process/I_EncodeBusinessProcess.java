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
 * Created on Dec 20, 2004
 */
package org.dwfa.bpa.process;

import java.awt.Rectangle;
import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.bpa.Branch;
import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.NoBranchForConditionException;
import org.ihtsdo.tk.db.DbDependency;

/**
 * Business Processes are a series of tasks encoded as JavaBeans.
 * <p>
 * 
 * No concurrent threads of execution, but tasks can launch processes, which can
 * execute concurrently.
 * <p>
 * 
 * Business Processes implement the <code>I_DefineTask</code> interface, so that
 * processes themselves can encapsulate a series of tasks inside of other
 * processes.
 * 
 * 
 * @author kec
 * 
 */
public interface I_EncodeBusinessProcess extends I_DefineTask, I_ManageProperties, I_DescribeBusinessProcess {

    /**
     * <code>CURRENT_TASK_ID</code> bound property name of the current task.
     */
    public static final String CURRENT_TASK_ID = "currentTaskId";
    /**
     * <code>NAME</code> is the bound property name for a descriptive name of
     * this business process.
     */
    public static final String NAME = "name";
    /**
     * <code>PROCESS_ID</code> is the bound property name for the identifier of
     * the business process.
     */
    public static final String PROCESS_ID = "processID";

    /**
     * Prerequisites allow the business process to define conditions that must
     * be met before this process can be executed. Examples might include a
     * specific
     * version of a terminology.
     * 
     * @return A set of environmental prerequsites the environment must meet
     *         before it can execute this business process.
     */
    public Set<?> getPrerequisites();

    /**
     * Add a task to this business process. This method will set the identifier
     * on this task to corespond with the processes internal representation of
     * tasks.
     * 
     * @param task A new task that has not yet been added to another business
     *            process.
     * @return The task with its identifier set.
     * @throws PropertyVetoException If the task vetos the identifer change.
     */
    public I_DefineTask addTask(I_DefineTask task) throws PropertyVetoException;

    /**
     * Remove a task from this business process. This method will use the
     * identifier
     * on this task to corespond with the internal representation of tasks.
     * 
     * @param task A new task that has not yet been added to another business
     *            process.
     * @return The task with its identifier set.
     * @throws PropertyVetoException If the task cannot be removed.
     */
    public void removeTask(I_DefineTask task) throws PropertyVetoException;

    /**
     * @return A collection of all the tasks in this process.
     */
    public Collection<I_DefineTask> getTasks();

    /**
     * @param taskId The identifier for the requested task.
     * @return A task corresponding to the provided identifier.
     */
    public I_DefineTask getTask(int taskId);

    /**
     * Typically called when processes are executed, after being deserialized.
     * 
     * @param newId The new identifier for this process.
     */
    public void setProcessID(ProcessID newId);

    /**
     * @return The origin task in a directed graph of tasks. The first task that
     *         will be executed (or has been executed of the process has already
     *         intiated).
     */
    public I_DefineTask getRootTask();

    /**
     * 
     * @param origin the origin of a branch connecting two tasks
     * @param destination the destination of a branch connecting two tasks
     * @param condition the condition that execution of the origin task must
     *            meet for this branch to be traversed.
     */
    public void addBranch(I_DefineTask origin, I_DefineTask destination, Condition condition);

    /**
     * @param origin the orogin of a branch connecting two tasks
     * @param destination the destination of a branch connecting two tasks
     * @param condition the condition that execution of the origin task must
     *            meet for this branch to be traversed
     */
    public void removeBranch(I_DefineTask origin, I_DefineTask destination, Condition condition);

    /**
     * @param from The task originating the requested branches
     * @return All branches originating from the provided task.
     */
    public Collection<Branch> getBranches(I_DefineTask from);

    /**
     * @return All execution records for this business process.
     */
    public Collection<ExecutionRecord> getExecutionRecords();

    /**
     * @param taskId Identifier for the task for with the execution records are
     *            requested.
     * @return All execution records for the identified task.
     */
    public Collection<ExecutionRecord> getExecutionRecords(int taskId);

    /**
     * Clear the list of execution records.
     */
    public void clearExecutionRecords();

    /**
     * Initiates execution of this business process, and will continue until
     * and exception is thrown, or a task completes with a stop condition
     * 
     * @param worker The worker that provides the resources and thread of
     *            execution
     *            for this business process. Note that this thread of execution
     *            is typicall not the
     *            event dispatch thread, so tasks must ensure that steps that
     *            must execute on the event
     *            dispatch thread (such as Swing operations) execute on the
     *            proper thread.
     * @throws TaskFailedException
     */
    public Condition execute(I_Work worker) throws TaskFailedException;

    /**
     * @param deadline Deadline by which this process should complete.
     */
    public void setDeadline(Date deadline);

    /**
     * @param priority Priority of executing this process.
     */
    public void setPriority(Priority priority);

    /**
     * @param address An address where outbox workers deliver this process for
     *            execution.
     */
    public void setDestination(String address);

    /**
     * @param address An address where this process originated.
     */
    public void setOriginator(String address);

    /**
     * Task bounds allow layout of a business process with in a GUI builder or
     * viewer.
     * 
     * @param id A task identifier
     * @param bounds Location of a particualar task within the business process.
     */
    public void setTaskBounds(int id, Rectangle bounds);

    /**
     * @param id task identifier for with the bounds are requested.
     */
    public Rectangle getTaskBounds(int id);

    /**
     * Given an identifier for an task, return the task that would be
     * executed next if for the given condition.
     * 
     * @param id identifier for the origin task.
     * @param condition Condition for which the next task is desired.
     * @return Task to be executed if the condition is met.
     * @throws NoBranchForConditionException
     */
    public I_DefineTask getTaskForCondition(int id, Condition condition) throws NoBranchForConditionException;

    /**
     * @return Identifier of the task that will be executed next.
     */
    public int getCurrentTaskId();

    /**
     * @param currentTaskId Sets the current task of this process.
     */
    public void setCurrentTaskId(int currentTaskId);

    /**
     * @return the identifier of the last task that was executed.
     */
    public int getLastTaskId();

    /**
     * @return The number of tasks in this business process.
     */
    public int getNumberOfTasks();

    /**
     * Data containers are a means of representing data accessable to all the
     * tasks
     * within a process.
     * 
     * @param data The data container.
     * @return The data container with its identifier set by the business
     *         process.
     * @throws PropertyVetoException
     * @deprecated use attachments instead.
     */
    public I_ContainData addDataContainer(I_ContainData data) throws PropertyVetoException;

    /**
     * @param id identifier for the data container requested.
     * @return A data container corresponding to the requested id.
     * @deprecated use attachments instead.
     */
    public I_ContainData getDataContainer(int id);

    /**
     * @return All the data containers in this process.
     * @deprecated use attachments instead.
     */
    public List<I_ContainData> getDataContainers();

    /**
     * @param id The identifier for the data container for which the bounds
     *            specified.
     * @param bounds The bounds of the specified data container.
     * @deprecated use attachments instead.
     */
    public void setDataContainerBounds(int id, Rectangle bounds);

    /**
     * Check if a message of the given level would actually be logged by this
     * logger.
     * This check is based on the Loggers effective level,
     * which may be inherited from its parent.
     * 
     * @param level a messaging log level
     * @return true if the given message level is currently being logged.
     */
    public boolean isLoggable(Level level);

    /**
     * Provides a uniform logging strategy for logging messages by business
     * processes.
     * 
     * @return A logger named by the I_EncodeBusinessProcess interface.
     */
    public Logger getLogger();

    /**
     * Get the attached object associated with the key.
     * 
     * @param key Key for the desired attachment.
     * @return The attachment associated with the key.
     */
    public Object readAttachement(String key);

    /**
     * Add an attachment to this business process.
     * 
     * @param key Key for the desired attachment.
     * @return The attachment associated with the key.
     */
    public void writeAttachment(String key, Object attachment);

    /**
     * Takes the attachment from the business processes, and removes the key
     * from
     * the attachment list.
     * 
     * @param key Key for the desired attachment.
     * @return The attachment associated with the key.
     */
    public Object takeAttachment(String key);

    /**
     * Renames an attachment's key, and ensures that all corresponding
     * information (property descriptors, externalization, etc) is
     * properly renamed as well.
     * 
     * @param oldKey the old key for the attachment
     * @param newKey the new key for the attachment
     * @throws PropertyVetoException Thrown if the newKey is already used.
     */
    public void renameAttachment(String oldKey, String newKey) throws PropertyVetoException;

    /**
     * @return Collection of all the keys associated with attachments.
     */
    public Collection<String> getAttachmentKeys();

    /**
     * Set the name of this business process. The name is used by builder
     * applications to tell the
     * user the function of the business process.
     * 
     * @param name
     * @throws PropertyVetoException
     */
    public void setName(String name) throws PropertyVetoException;

    public void setSubject(String subject) throws PropertyVetoException;

    public boolean isPropertyExternal(PropertySpec spec);

    public PropertySpec getExternalSpec(PropertySpec key);

    public void setPropertyExternal(PropertySpec spec, boolean external);

    public I_DefineTask getLastTaskAdded();

    public I_DefineTask getLastTaskRemoved();

    /**
     * This method provides tasks with a uniform means of resolving URLs that
     * may
     * contain the following protocol extensions:
     * <p>
     * <em>business process attachments:</em> bpa:&lt;key name&gt;
     * <p>
     * <em>task properties:</em> tp://&lt;task id>/&lt;property name&gt;
     * <p>
     * <em>data container:</em> dc:&lt;container name&gt;
     * <p>
     * <p>
     * These extensions allow a uniform means to access objects located within
     * the business process or elsewhere on the network.
     * 
     * @param locator the URL.
     * @return An <code>InputStream</code> derived from the provided
     *         <code>URL</code>.
     */
    public InputStream getStreamFromURL(URL locator) throws IOException;

    /**
     * This method provides tasks with a uniform means of resolving URLs that
     * may
     * contain the following protocol extensions:
     * <p>
     * <em>business process attachments:</em> bpa:&lt;key name&gt;
     * <p>
     * <em>task properties:</em> tp://&lt;task id>/&lt;property name&gt;
     * <p>
     * <p>
     * These extensions allow a uniform means to access objects located within
     * the business process or elsewhere on the network.
     * 
     * @param locator the URL.
     * @return An <code>Object</code> derived from the provided <code>URL</code>
     *         .
     * @throws IOException
     */
    public Object getObjectFromURL(URL locator) throws IOException;

    /**
     * Read a property specified by the property label.
     * 
     * @param propertyLabel The label that specifies the property to read
     * @return The current value of of the specified property.
     * @throws IntrospectionException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @deprecated use getProperty
     */
    public Object readProperty(String propertyLabel) throws IntrospectionException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException;

    /**
     * Get a property specified by the property label.
     * 
     * @param propertyLabel The label that specifies the property to read
     * @return The current value of of the specified property.
     * @throws IntrospectionException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Object getProperty(String propertyLabel) throws IntrospectionException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException;

    public void setProperty(String propertyLabel, Object value) throws IntrospectionException, IllegalAccessException,
            InvocationTargetException;

    public void setMessageRenderer(I_RenderMessage renderer);

    public I_RenderMessage getMessageRenderer();

    public String getProcessDocumentation(I_RenderDocumentation renderer) throws Exception;

    public String getProcessDocumentation() throws Exception;

    public String getProcessDocumentationSource();

    public void setProcessDocumentationSource(String docSource);

    /**
     * This method processes an input string looking for substitution variables
     * of the form: <code>${locator}</code> and replaces those values with the
     * results of the<code>toString()</code> method called on
     * the object returned by the <code>getObjectFromURL(URL locator)</code> or
     * an error message
     * if the object returned is null.
     * 
     * <br>
     * In addition to standard url protocols, a "bpa" protocol is supported that
     * will retrieve a business process
     * attachment. The form of the url would be
     * <code>bpa:<attachment name></code>. In addition, a "tp" protocol is
     * defined for "task properties", but this definition has no implementation
     * at the moment... <br>
     * 
     * @param input <code>String</code> that may contain substitution variables
     *            of the form: <code>${locator}</code>.
     * @return A <code>String</code> with the substitution variables replaced
     *         with results of the<code>toString()</code> method called on
     *         the object returned by the
     *         <code>getObjectFromURL(URL locator)</code> or an error message
     *         if the object returned is null.
     *         <p>
     * 
     * @throws IOException
     * @throws MalformedURLException
     */
    public String substituteProperties(String input) throws MalformedURLException, IOException;
    
    public Collection<DbDependency> getDbDependencies();

    public void setDbDependencies(Collection<DbDependency> dbDependencies);


}
