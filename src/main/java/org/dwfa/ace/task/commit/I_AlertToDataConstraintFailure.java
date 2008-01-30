package org.dwfa.ace.task.commit;


public interface I_AlertToDataConstraintFailure {
    /**
     * 
     * @param alertMessage
     * @param fixOptions The fixOptions objects need to have a toString method that is suitable
     *                      for display in a combo box. 
     * @return selected fixOption or null
     */
    public Object alert(String alertMessage, Object[] fixOptions);
    
    /**
     * 
     * @param alertMessage
     */
    public void alert(String alertMessage);
}
