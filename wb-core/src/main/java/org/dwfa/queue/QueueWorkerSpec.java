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
 * Created on Apr 21, 2005
 */
package org.dwfa.queue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;

import org.dwfa.bpa.process.I_SelectProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.worker.task.I_GetWorkFromQueue;

/**
 * @author kec
 * 
 */
public class QueueWorkerSpec {
    private Class<? extends I_Work> workerClass;

    private String workerName;

    private UUID workerId;

    private I_SelectProcesses selector;

    private String[] specifiedConfigArgs;

    /**
     * @return Returns the selector.
     */
    public I_SelectProcesses getSelector() {
        return selector;
    }

    /**
     * @return Returns the workerClass.
     */
    public Class<? extends I_Work> getWorkerClass() {
        return workerClass;
    }

    /**
     * @return Returns the workerId.
     */
    public UUID getWorkerId() {
        return workerId;
    }

    /**
     * @return Returns the workerName.
     */
    public String getWorkerName() {
        return workerName;
    }

    /**
     * @param workerClass
     * @param workerName
     * @param workerId
     * @param selector
     */
    public QueueWorkerSpec(Class<? extends I_Work> workerClass, String workerName, UUID workerId,
            I_SelectProcesses selector) {
        this(workerClass, workerName, workerId, selector, null);
    }

    public QueueWorkerSpec(Class<? extends I_Work> workerClass, String workerName, UUID workerId,
            I_SelectProcesses selector, String[] specifiedConfigArgs) {
        super();
        this.workerClass = workerClass;
        this.workerName = workerName;
        this.workerId = workerId;
        this.selector = selector;
        this.specifiedConfigArgs = specifiedConfigArgs;
    }

    public I_GetWorkFromQueue create(Configuration config) throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException,
            ConfigurationException {
        Constructor<? extends I_Work> c = this.workerClass.getConstructor(new Class[] { Configuration.class,
                                                                                       UUID.class, String.class,
                                                                                       I_SelectProcesses.class });

        Object[] constructorArgs;
        if (this.specifiedConfigArgs == null) {
            constructorArgs = new Object[] { config, this.workerId, this.workerName, this.selector };
        } else {
            Configuration specifiedConfig = ConfigurationProvider.getInstance(this.specifiedConfigArgs);
            constructorArgs = new Object[] { specifiedConfig, this.workerId, this.workerName, this.selector };
        }
        return (I_GetWorkFromQueue) c.newInstance(constructorArgs);

    }
}
