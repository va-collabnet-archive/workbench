package org.dwfa.ace.config;

import java.util.ArrayList;

import org.dwfa.config.CoreServices;
import org.dwfa.config.ServiceConfigOption;

public class AceReadOnlyServices extends ArrayList<ServiceConfigOption> {
   
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public AceReadOnlyServices() {
      super();

      //add(AceLocalServices.CO_LocalTransactionManager);
      //add(CoreServices.CO_TransactionAggregator);
      add(CO_AceViewer);

   }

   public static String ACE_READONLY_SERVICE = "Ace Viewer";
   public static String ACE_READONLY_SERVICE_PROP = "org.dwfa.ACE_READONLY_SERVICE";
   public static ServiceConfigOption CO_AceViewer = new ServiceConfigOption(
         ACE_READONLY_SERVICE, "config${/}ace.policy", 
         "config${/}aceSecure.policy", ACE_READONLY_SERVICE_PROP,
           "Starts the Ace Viewer. ", 
           true, "", null, CoreServices.dwaPath, 
           "org.dwfa.ace.config.AceReadOnlyRunner",
           new String[] { "config${/}aceViewer.config" }, 
           new String[] { "config${/}aceSecure.config" }, 
           true, 
           false, false, "");


}