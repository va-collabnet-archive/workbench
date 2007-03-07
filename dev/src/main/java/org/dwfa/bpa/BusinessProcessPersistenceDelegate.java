/*
 * Created on Jan 8, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Statement;
import java.util.Collection;
import java.util.List;

import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.process.PropertySpec;

public class BusinessProcessPersistenceDelegate extends
        DefaultPersistenceDelegate {

    public BusinessProcessPersistenceDelegate() {
        super();
    }

    /**
     * @see java.beans.DefaultPersistenceDelegate#initialize(java.lang.Class,
     *      java.lang.Object, java.lang.Object, java.beans.Encoder)
     */
    @Override
    protected void initialize(Class<?> type, Object oldInstance,
            Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);

        BusinessProcess p = (BusinessProcess) oldInstance;

        Collection<I_ContainData> data = p.getDataContainers();
        for (I_ContainData d : data) {
            out.writeStatement(new Statement(oldInstance, "addDataContainer",
                    new Object[] { d }));
        }

        List<TaskInfo> taskInfoList = p.getTaskInfoList();
        for (TaskInfo ti : taskInfoList) {
            out.writeStatement(new Statement(oldInstance, "setTaskInfo",
                    new Object[] { ti }));
        }

        Collection<PropertySpec> externalProperties = p.getExternalProperties();
        for (PropertySpec ps : externalProperties) {
            out.writeStatement(new Statement(oldInstance,
                    "setPropertyExternal", new Object[] { ps.getPropertyName(),
                            ps.getType().name(), ps.getId(), new Boolean(true) }));
        }

        Collection<String> keys = p.getAttachmentKeys();
        for (String k : keys) {
            Object obj = p.readAttachement(k);
            out.writeStatement(new Statement(oldInstance, "writeAttachment",
                    new Object[] { k, obj }));
        }
    }

}
