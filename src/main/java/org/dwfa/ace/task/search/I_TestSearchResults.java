package org.dwfa.ace.task.search;

import java.io.Serializable;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.bpa.process.TaskFailedException;

public interface I_TestSearchResults extends Serializable {

    public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException;

}