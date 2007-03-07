/*
 * Created on Feb 22, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.editor;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;

public class ProcessDataIdEditor extends DataIdEditor implements I_OnlyWantOneLine{

    public Class getAcceptableClass() {
        return I_EncodeBusinessProcess.class;
    }

    public ProcessDataIdEditor(Object obj) throws ClassNotFoundException {
        super(obj);
    }

}
