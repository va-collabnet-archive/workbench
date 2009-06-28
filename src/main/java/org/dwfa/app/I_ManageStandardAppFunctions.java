package org.dwfa.app;

/**
 * This interface facilitates integration with native
 * platform conventions such as menu items for
 * preferences, about boxes, and quitting the application.
 * <p>
 * Platform-specific events are routed to a platform-specific 
 * class that implements this interface. Platform-specific classes
 * can be compiled and managed across platforms by using reflection. 
 * 
 * @see OSXAdapter
 * @author kec
 *
 */
public interface I_ManageStandardAppFunctions {
    /** 
     * Present an "about box" to the user.
     *
     */
    public void about();
    
    /**
     * Called with the application receives an Open Application event
     * from the finder or another application.
     *
     */
    public void openApplication();
    
    /**
     * Called when the application receives an Open Document event
     * from the Finder or another application.
     *
     */
    public void openFile();

    /** 
     * Present a global "preferences" dialog to the user.
     */
    public void preferences();

    /**
     * Called when the application is sent a request
     * to print a particular file or files.
     *
     */
    public void printFile();
    
    /** 
     * Manage the process of quitting the application.
     */
    public boolean quit();
    
    /**
     * Called when the application receives a 
     * Reopen Application Event from the finder or
     * another application. 
     *
     */
    public void reOpenApplication();

}
