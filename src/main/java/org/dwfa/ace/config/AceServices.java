package org.dwfa.ace.config;

import java.util.ArrayList;

import org.dwfa.config.CoreServices;
import org.dwfa.config.ServiceConfigOption;

public class AceServices extends ArrayList<ServiceConfigOption> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AceServices() {
		super();
		/* Order is important. You want the registration services to start
		 * in the first phase, and the queue viewer to start in the last phase. 
		 */
		add(CoreServices.CO_ClassServer);
		add(CoreServices.CO_ServiceRegistrar);
		add(CoreServices.CO_TransactionAggregator);
		add(CoreServices.CO_TransactionManager);
		add(CoreServices.CO_Timer);

		add(CoreServices.CO_AgingQueue);
		add(CoreServices.CO_Archival);
		add(CoreServices.CO_ComputeQueue);
		add(CoreServices.CO_LauncherQueue);
		add(CoreServices.CO_OutboxQueue);
		add(CoreServices.CO_SyncQueue);
				
		add(CoreServices.CO_GenericWorkerManager);
		add(CoreServices.CO_GenericWorkerManagerNewFrame);
		add(CoreServices.CO_JavaSpace);
		add(CoreServices.CO_LogManagerService);
		add(CoreServices.CO_LogViewerNewFrame);
		add(CoreServices.CO_PhantomFrame);
		add(CoreServices.CO_ServiceBrowserNewFrame);
		add(CO_FormBuilderNewFrame);
		add(CO_AceEditor);

	}

    public static String FORM_BUILDER =  "Form Builder";
    public static String FORM_BUILDER_PROP =  "org.jehri.FORM_BUILDER";

     public static ServiceConfigOption CO_FormBuilderNewFrame = new ServiceConfigOption(
    		FORM_BUILDER, "config${/}newWindowGenerator.policy", "config${/}newWindowGeneratorSecure.policy", 
    		FORM_BUILDER_PROP,
            "Allows building encoded forms. ",
            true, 
            "", 
            null, 
            CoreServices.dwaPath,
            "org.dwfa.bpa.util.NewWindowGenerator", // mainclass
            new String[] { "config${/}newLogViewerFrame.config" }, //args
            new String[] { "config${/}newLogViewerFrameSecure.config" }, // secure args
            true,
            false, 
            false, 
            "");
 
 

    public static String ACE_EDITOR_SERVICE = "Chart Service";
    public static String ACE_EDITOR_SERVICE_PROP = "org.dwfa.ACE_EDITOR_SERVICE";
    public static ServiceConfigOption CO_AceEditor = new ServiceConfigOption(
    		ACE_EDITOR_SERVICE, "config${/}ace.policy", 
    		"config${/}aceSecure.policy", ACE_EDITOR_SERVICE_PROP,
            "Starts the Ace Editor. ", 
            true, "", null, CoreServices.dwaPath, 
            "org.dwfa.ace.config.AceRunner",
            new String[] { "config${/}ace.config" }, 
            new String[] { "config${/}aceSecure.config" }, 
            true, 
            false, false, "");
    

}