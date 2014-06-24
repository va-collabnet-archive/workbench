package org.dwfa.ace.api;

import java.util.List;


public interface I_HelpMemberRefsetsCalculateConflicts extends I_HelpCaculateMemberRefsets {
    
    public boolean hasConflicts();
    
    public List<String> getConclictDetails();

}
