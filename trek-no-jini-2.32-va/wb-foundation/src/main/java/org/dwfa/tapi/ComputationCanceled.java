package org.dwfa.tapi;

public class ComputationCanceled extends TerminologyException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ComputationCanceled() {
       super();
    }

    public ComputationCanceled(String message) {
        super(message);
    }

    public ComputationCanceled(Throwable cause) {
        super(cause);
    }

    public ComputationCanceled(String message, Throwable cause) {
        super(message, cause);
   }

}
