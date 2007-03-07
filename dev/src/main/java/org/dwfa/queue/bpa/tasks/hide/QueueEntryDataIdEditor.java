/*
 * Created on Feb 17, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.hide;

import org.dwfa.bpa.tasks.editor.DataIdEditor;
import org.dwfa.queue.bpa.tasks.failsafe.QueueEntryData;

/**
 * @author kec
 *
 */
public class QueueEntryDataIdEditor extends DataIdEditor {

    /**
     * @param arg0
     * @throws ClassNotFoundException
     */
    public QueueEntryDataIdEditor(Object obj) throws ClassNotFoundException {
        super(obj);
    }
    public Class getAcceptableClass() {
        return QueueEntryData.class;
    }

}
