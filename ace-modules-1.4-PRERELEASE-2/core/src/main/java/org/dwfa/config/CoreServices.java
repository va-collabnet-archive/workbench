/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.config;

import java.util.ArrayList;

public class CoreServices extends ArrayList<ServiceConfigOption> {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public CoreServices() {
        super();
        /*
         * Order is important. You want the registration services to start
         * in the first phase, and the queue viewer to start in the last phase.
         */
        add(CO_ClassServer);
        add(CO_ServiceRegistrar);
        add(CO_TransactionAggregator);
        add(CO_TransactionManager);
        add(CO_Timer);

        add(CO_AgingQueue);
        add(CO_Archival);
        add(CO_ComputeQueue);
        add(CO_GenericWorkerManager);
        add(CO_GenericWorkerManagerNewFrame);
        // add(CO_Inbox);
        add(CO_JavaSpace);
        add(CO_LauncherQueue);
        add(CO_LogManagerService);
        add(CO_LogViewer);
        add(CO_LogViewerNewFrame);
        add(CO_OutboxQueue);
        //add(CO_PhantomFrame);
        add(CO_ProcessBuilder);
        // add(CO_SequenceAuthorityOidUuid);
        // add(CO_SequenceAuthorityUuid);
        // add(CO_SequenceAuthorityVerhoeff);
        add(CO_ServiceBrowser);
        add(CO_ServiceBrowserNewFrame);
        add(CO_SyncQueue);
        // add(CO_Tunnel);
        // add(CO_WebQueue);
        // add(CO_WebServer);
        add(CO_QueueViewer);

    }

    public static String dwaPath = "VHelp.addLibVersion(\"core\") + \"${path.separator}\" + "
        + "VHelp.addLibVersion(\"activation\") + \"${path.separator}\" + " + "VHelp.addLibVersion(\"mail\")";

    public static String PHANTOM_FRAME = "Phantom Frame";
    public static String PHANTOM_FRAME_PROP = "org.dwfa.PHANTOM_FRAME";
    public static String LOG_VIEWER = "Log Viewer";
    public static String LOG_VIEWER_PROP = "org.dwfa.LOG_VIEWER";
    public static String LOG_VIEWER_NEW_FRAME = "Log Viewer New Frame";
    public static String LOG_VIEWER_NEW_FRAME_PROP = "org.dwfa.LOG_VIEWER_NEW_FRAME";
    public static String JINI_CLASS_SERVER = "Jini Class Server";
    public static String JINI_CLASS_SERVER_PROP = "org.dwfa.JINI_CLASS_SERVER";
    public static String WORKFLOW_CLASS_SERVER = "Workflow Class Server";
    public static String WORKFLOW_CLASS_SERVER_PROP = "org.dwfa.WORKFLOW_CLASS_SERVER";
    public static String JINI_SERVICE_BROWSER = "Jini Service Browser";
    public static String JINI_SERVICE_BROWSER_PROP = "org.dwfa.JINI_SERVICE_BROWSER";
    public static String JINI_SERVICE_BROWSER_NEW_FRAME = "Jini Service Browser New Frame";
    public static String JINI_SERVICE_BROWSER_NEW_FRAME_PROP = "org.dwfa.JINI_SERVICE_BROWSER_NEW_FRAME";
    public static String SERVICE_REGISTRAR = "Service Registrar";
    public static String SERVICE_REGISTRAR_PROP = "org.dwfa.SERVICE_REGISTRAR";
    public static String TRANSACTION_MANAGER = "Transaction Manager";
    public static String TRANSACTION_MANAGER_PROP = "org.dwfa.TRANSACTION_MANAGER";
    public static String TRANSACTION_AGGREGATOR = "Transaction Aggregator";
    public static String TRANSACTION_AGGREGATOR_PROP = "org.dwfa.TRANSACTION_AGGREGATOR";
    public static String GENERIC_WORKER_MANAGER = "Generic Worker Manager";
    public static String GENERIC_WORKER_MANAGER_PROP = "org.dwfa.GENERIC_WORKER_MANAGER";
    public static String GENERIC_WORKER_MANAGER_NEW_FRAME = "Generic Worker Manager New Frame";
    public static String GENERIC_WORKER_MANAGER_NEW_FRAME_PROP = "org.dwfa.GENERIC_WORKER_MANAGER_NEW_FRAME";
    public static String INBOX_QUEUE = "InBox Queue";
    public static String INBOX_QUEUE_PROP = "org.dwfa.INBOX_QUEUE";
    public static String CREATE_MAP_INBOX_QUEUE = "Create Map InBox Queue";
    public static String CREATE_MAP_INBOX_QUEUE_PROP = "org.dwfa.CREATE_MAP_INBOX_QUEUE";
    public static String REVIEW_INBOX_QUEUE = "Review InBox Queue";
    public static String REVIEW_INBOX_QUEUE_PROP = "org.dwfa.REVIEW_INBOX_QUEUE";
    public static String OUTBOX_QUEUE = "OutBox Queue";
    public static String OUTBOX_QUEUE_PROP = "org.dwfa.OUTBOX_QUEUE";

    public static String AGING_QUEUE = "Aging Queue";
    public static String AGING_QUEUE_PROP = "org.dwfa.AGING_QUEUE";
    public static ServiceConfigOption CO_AgingQueue = new ServiceConfigOption(AGING_QUEUE,
        "config${/}queueAging.policy", "config${/}queueAgingSecure.policy", AGING_QUEUE_PROP,
        "Publishes an Aging Queue for workflow. ", true, "jiniPortUrlPart, VHelp.addDlVersion(\"queue-dl\"), \"",
        "lib", dwaPath, "org.dwfa.queue.QueueServer", new String[] { "config${/}queueAging.config" },
        new String[] { "config${/}queueAgingSecure.config" }, true, false, false, "");

    public static String COMPUTE_QUEUE = "Compute Queue";
    public static String COMPUTE_QUEUE_PROP = "org.dwfa.COMPUTE_QUEUE";
    public static String LAUNCHER_QUEUE = "Launcher Queue";
    public static String LAUNCHER_QUEUE_PROP = "org.dwfa.LAUNCHER_QUEUE";
    public static String LOG_SERVICE = "Log Service";
    public static String LOG_SERVICE_PROP = "org.dwfa.LOG_SERVICE";
    public static String SEQUENCE_AUTHORITY_VERHOEFF = "Sequence Authority Service (with Verhoeff check digit)";
    public static String SEQUENCE_AUTHORITY_VERHOEFF_PROP = "org.dwfa.SEQUENCE_AUTHORITY_VERHOEFF";
    public static String OID_UUID_AUTHORITY = "OID with UUID Extension Authority Service";
    public static String OID_UUID_AUTHORITY_PROP = "org.dwfa.OID_UUID_AUTHORITY";
    public static String UUID_AUTHORITY = "UUID Authority Service";
    public static String UUID_AUTHORITY_PROP = "org.dwfa.UUID_AUTHORITY";
    public static String QUEUE_VIEWER = "Queue Viewer";
    public static String QUEUE_VIEWER_PROP = "org.dwfa.QUEUE_VIEWER";
    public static String PROCESS_BUILDER = "Process Builder";
    public static String PROCESS_BUILDER_PROP = "org.dwfa.bpa.process_BUILDER";
    public static String TERM_SERVER_DERBY = "Terminology Server (Derby)";
    public static String TERM_SERVER_DERBY_PROP = "org.dwfa.TERM_SERVER_DERBY";
    public static String TERM_SERVER_RAM = "Terminology Server (RAM)";
    public static String TERM_SERVER_RAM_PROP = "org.dwfa.TERM_SERVER_RAM";
    public static String TERM_SERVER_RAM_ICD = "Terminology Server (RAM - ICD)";
    public static String TERM_SERVER_RAM_ICD_PROP = "org.dwfa.TERM_SERVER_RAM_ICD";
    public static String TERM_SERVER_RAM_ICD_SNOMED = "Terminology Server (RAM - ICD & SNOMED)";
    public static String TERM_SERVER_RAM_ICD_SNOMED_PROP = "org.dwfa.TERM_SERVER_RAM_ICD_SNOMED";
    public static String JAVA_SPACE = "Java Space";
    public static String JAVA_SPACE_PROP = "org.dwfa.JAVA_SPACE";
    public static String TUNNEL_SERVICE = "Tunnel Service";
    public static String TUNNEL_SERVICE_PROP = "org.dwfa.TUNNEL_SERVICE";

    public static ServiceConfigOption CO_PhantomFrame = new ServiceConfigOption(PHANTOM_FRAME,
        "config${/}newWindowGenerator.policy", "config${/}newWindowGeneratorSecure.policy", PHANTOM_FRAME_PROP,
        "Ensures there is a window menu even if all windows have been closed. ", true, "", null, dwaPath,
        "org.dwfa.bpa.util.PhantomFrame", new String[] { "config${/}phantomFrame.config" },
        new String[] { "config${/}phantomFrameSecure.config" }, false, false, false, "");

    public static ServiceConfigOption CO_LogViewer = new ServiceConfigOption(
        LOG_VIEWER,
        "config${/}logViewer.policy",
        "config${/}logViewerSecure.policy",
        LOG_VIEWER_PROP,
        "<html>Allows viewing local logs, and logs published by the <font color='blue'>Log Service</font> on other JVMs. ",
        false, "", null, dwaPath, "org.dwfa.log.LogViewerFrame", new String[] { "config${/}logViewer.config" },
        new String[] { "config${/}logViewerSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_LogViewerNewFrame = new ServiceConfigOption(
        LOG_VIEWER_NEW_FRAME,
        "config${/}newWindowGenerator.policy",
        "config${/}newWindowGeneratorSecure.policy",
        LOG_VIEWER_NEW_FRAME_PROP,
        "<html>Menu option to allow viewing local logs, and logs published by the <font color='blue'>Log Service</font> on other JVMs. ",
        true, // enabled by default
        "", // codebase
        null, // jardir
        dwaPath, // classpath
        "org.dwfa.bpa.util.NewWindowGenerator", // mainclass
        new String[] { "config${/}newLogViewerFrame.config" }, // args
        new String[] { "config${/}newLogViewerFrameSecure.config" }, // secure
                                                                     // args
        true, // optional
        false, // alert if selected
        false, // alert if deselected
        ""); // alert string

    public static ServiceConfigOption CO_ClassServer = new ServiceConfigOption(JINI_CLASS_SERVER,
        "config${/}classServer.policy", "config${/}classServerSecure.policy", JINI_CLASS_SERVER_PROP,
        "Must be turned on to allow Jini services to run on this machine. ", true, "", null,
        "VHelp.addLibVersion(\"tools\")", "com.sun.jini.tool.ClassServer", new String[] { "-port", "jiniPort", "-dir",
                                                                                         "lib-dl", "-verbose", },
        new String[] { "-port", "jiniPort", "-dir", "lib-dl", "-verbose", }, false, false, false, "");

    public static ServiceConfigOption CO_ServiceBrowser = new ServiceConfigOption(JINI_SERVICE_BROWSER,
        "config${/}browser.policy", "config${/}browserSecure.policy", JINI_SERVICE_BROWSER_PROP,
        "Optional component to allow browsing Jini services on the network. ", false,
        "jiniPortUrlPart, VHelp.addDlVersion(\"browser-dl\"), \"", "lib", "VHelp.addLibVersion(\"browser\")"
            + "+ \"${path.separator}\" + VHelp.addLibVersion(\"macadaptor\")",
        "org.dwfa.servicebrowser.BrowserAdaptor", new String[] { "config${/}browser.config" },
        new String[] { "config${/}browserSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_ServiceBrowserNewFrame = new ServiceConfigOption(
        JINI_SERVICE_BROWSER_NEW_FRAME, "config${/}newWindowGenerator.policy",
        "config${/}newWindowGeneratorSecure.policy", JINI_SERVICE_BROWSER_NEW_FRAME_PROP,
        "Menu option to create a window to allow browsing Jini services on the network. ", true, // enabled
                                                                                                 // by
                                                                                                 // default
        "", // codebase
        null, // jardir
        dwaPath, // classpath
        "org.dwfa.bpa.util.NewWindowGenerator", // mainclass
        new String[] { "config${/}newBrowserFrame.config" }, // args
        new String[] { "config${/}newBrowserFrameSecure.config" }, // secure
                                                                   // args
        true, // optional
        false, // alert if selected
        false, // alert if deselected
        ""); // alert string

    public static ServiceConfigOption CO_ServiceRegistrar = new ServiceConfigOption(SERVICE_REGISTRAR,
        "config${/}reggie.policy", "config${/}reggieSecure.policy", SERVICE_REGISTRAR_PROP,
        "There must be at least one on the network, or you can't discover services. ", true,
        "jiniPortUrlPart, VHelp.addDlVersion(\"reggie-dl\"), \"", "lib", "VHelp.addLibVersion(\"reggie\")",
        "com.sun.jini.reggie.TransientRegistrarImpl", new String[] { "config${/}reggie.config" },
        new String[] { "config${/}reggieSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_TransactionManager = new ServiceConfigOption(TRANSACTION_MANAGER,
        "config${/}transactionManager.policy", "config${/}transactionManagerSecure.policy", TRANSACTION_MANAGER_PROP,
        "There must be at least one on the network, or you run services that use transactions. ", true,
        "jiniPortUrlPart, VHelp.addDlVersion(\"mahalo-dl\"), \"", "lib", "VHelp.addLibVersion(\"mahalo\")",
        "com.sun.jini.mahalo.TransientMahaloImpl", new String[] { "config${/}transactionManager.config" },
        new String[] { "config${/}transactionManagerSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_JavaSpace = new ServiceConfigOption(JAVA_SPACE,
        "config${/}transient-outrigger.policy", "config${/}transient-outriggerSecure.policy", JAVA_SPACE_PROP,
        "There must be at least one on the network to support generic workers and grid-based workflow. ", true,
        "jiniPortUrlPart, VHelp.addDlVersion(\"outrigger-dl\"), \"", "lib", "VHelp.addLibVersion(\"outrigger\")",
        "com.sun.jini.outrigger.TransientOutriggerImpl", new String[] { "config${/}transient-outrigger.config" },
        new String[] { "config${/}transient-outriggerSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_TransactionAggregator = new ServiceConfigOption(TRANSACTION_AGGREGATOR,
        "config${/}transactionAggregator.policy", "config${/}transactionAggregatorSecure.policy",
        TRANSACTION_AGGREGATOR_PROP, "There must be one on each JVM to participate in Jini Transactions. ", true, "",
        null, dwaPath, "org.dwfa.jini.TransactionParticipantAggregator",
        new String[] { "config${/}transactionAggregator.config" },
        new String[] { "config${/}transactionAggregatorSecure.config" }, false, false, false, "");

    public static ServiceConfigOption CO_GenericWorkerManager = new ServiceConfigOption(GENERIC_WORKER_MANAGER,
        "config${/}genericWorkerManager.policy", "config${/}genericWorkerManagerSecure.policy",
        GENERIC_WORKER_MANAGER_PROP, "Starts generic workers that participate in grid-based workflow activities. ",
        false, "", null, dwaPath, "org.dwfa.bpa.worker.GenericWorkerManager",
        new String[] { "config${/}genericWorkerManager.config" },
        new String[] { "config${/}genericWorkerManagerSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_GenericWorkerManagerNewFrame = new ServiceConfigOption(
        GENERIC_WORKER_MANAGER_NEW_FRAME, "config${/}newWindowGenerator.policy",
        "config${/}newWindowGeneratorSecure.policy", GENERIC_WORKER_MANAGER_NEW_FRAME_PROP,
        "Menu option to start generic workers that participate in grid-based workflow activities. ", true, // enabled
                                                                                                           // by
                                                                                                           // default
        "", // codebase
        null, // jardir
        dwaPath, // classpath
        "org.dwfa.bpa.util.NewWindowGenerator", // mainclass
        new String[] { "config${/}newGenericWorkerManagerFrame.config" }, // args
        new String[] { "config${/}newGenericWorkerManagerFrameSecure.config" }, // secure
                                                                                // args
        true, // optional
        false, // alert if selected
        false, // alert if deselected
        "");

    public static ServiceConfigOption CO_Tunnel = new ServiceConfigOption(TUNNEL_SERVICE, "config${/}tunnel.policy",
        "config${/}tunnelSecure.policy", TUNNEL_SERVICE_PROP,
        "Allows services on one lookup group to tunnel to another lookup service via a URL. ", false,
        "jiniPortUrlPart, VHelp.addDlVersion(\"tunnel-dl\"), \"\"), \"", "lib", dwaPath,
        "org.dwfa.tunnel.TunnelService", new String[] { "config${/}tunnelService.config" },
        new String[] { "config${/}tunnelServiceSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_Inbox = new ServiceConfigOption(INBOX_QUEUE, "config${/}queueInbox.policy",
        "config${/}queueInboxSecure.policy", INBOX_QUEUE_PROP, "Publishes an Inbox Queue for business processes. ",
        true, "jiniPortUrlPart, VHelp.addDlVersion(\"queue-dl\"), \"", "lib", dwaPath, "org.dwfa.queue.QueueServer",
        new String[] { "config${/}queueInbox.config" }, new String[] { "config${/}queueInboxSecure.config" }, true,
        false, true, "<html>Every workflow node needs at least one inbox queue to recieve processes<br>"
            + " from other nodes. Please ensure that at least one of the machines in this node<br>"
            + " has an inbox queue.");

    public static ServiceConfigOption CO_OutboxQueue = new ServiceConfigOption(OUTBOX_QUEUE,
        "config${/}queueOutbox.policy", "config${/}queueOutboxSecure.policy", OUTBOX_QUEUE_PROP,
        "Publishes an OutBox Queue for workflow. ", true, "jiniPortUrlPart, VHelp.addDlVersion(\"queue-dl\"), \"",
        "lib", dwaPath, "org.dwfa.queue.QueueServer", new String[] { "config${/}queueOutbox.config" },
        new String[] { "config${/}queueOutboxSecure.config" }, true, false, true,
        "<html>Every workflow node needs at least one outbox queue to send processes<br>"
            + " to other nodes. Please ensure that at least one of the machines in this node<br>"
            + " has an outbox queue.");

    public static ServiceConfigOption CO_ComputeQueue = new ServiceConfigOption(COMPUTE_QUEUE,
        "config${/}queueCompute.policy", "config${/}queueComputeSecure.policy", COMPUTE_QUEUE_PROP,
        "Publishes a Compute Queue (does not support user interaction) for workflow. ", true,
        "jiniPortUrlPart, VHelp.addDlVersion(\"queue-dl\"), \"", "lib", dwaPath, "org.dwfa.queue.QueueServer",
        new String[] { "config${/}queueCompute.config" }, new String[] { "config${/}queueComputeSecure.config" }, true,
        false, false, "");

    public static ServiceConfigOption CO_LauncherQueue = new ServiceConfigOption(
        LAUNCHER_QUEUE,
        "config${/}queueLauncher.policy",
        "config${/}queueLauncherSecure.policy",
        LAUNCHER_QUEUE_PROP,
        "<html>Publishes queue from which processes can be launched, but they will remain in the queue for subsequent launches<br><font color='blue'>(take operations are actually read operations). ",
        true, "jiniPortUrlPart, VHelp.addDlVersion(\"queue-dl\"), \"", "lib", dwaPath, "org.dwfa.queue.QueueServer",
        new String[] { "config${/}queueLauncher.config" }, new String[] { "config${/}queueLauncherSecure.config" },
        true, false, false, "");

    public static String SYNC_QUEUE = "Sync Queue";
    public static String SYNC_QUEUE_PROP = "org.dwfa.SYNC_QUEUE";
    public static ServiceConfigOption CO_SyncQueue = new ServiceConfigOption(SYNC_QUEUE, "config${/}queueSync.policy",
        "config${/}queueSyncSecure.policy", SYNC_QUEUE_PROP, "Publishes a Synchronization Queue for workflow. ", true,
        "jiniPortUrlPart, VHelp.addDlVersion(\"queue-dl\"), \"", "lib", dwaPath, "org.dwfa.queue.QueueServer",
        new String[] { "config${/}queueSync.config" }, new String[] { "config${/}queueSyncSecure.config" }, true,
        false, false, "");

    public static String WEB_QUEUE = "Web Queue";
    public static String WEB_QUEUE_PROP = "org.dwfa.WEB_QUEUE";
    public static ServiceConfigOption CO_WebQueue = new ServiceConfigOption(WEB_QUEUE, "config${/}queueWeb.policy",
        "config${/}queueWebSecure.policy", WEB_QUEUE_PROP, "Publishes a Web Queue for workflow. ", true,
        "jiniPortUrlPart, VHelp.addDlVersion(\"queue-dl\"), \"", "lib", dwaPath, "org.dwfa.queue.QueueServer",
        new String[] { "config${/}queueWeb.config" }, new String[] { "config${/}queueWebSecure.config" }, true, false,
        false, "");

    public static ServiceConfigOption CO_LogManagerService = new ServiceConfigOption(LOG_SERVICE,
        "config${/}logManagerService.policy", "config${/}logSecure.policy", LOG_SERVICE_PROP,
        "Makes the logs from the local JVM available on the network. ", true,
        "jiniPortUrlPart, VHelp.addDlVersion(\"log-dl\"), \"", "lib", dwaPath, "org.dwfa.log.LogManagerService",
        new String[] { "config${/}logManagerService.config" },
        new String[] { "config${/}logManagerServiceSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_SequenceAuthorityVerhoeff = new ServiceConfigOption(
        SEQUENCE_AUTHORITY_VERHOEFF, "config${/}assignmentAuthorityVerhoeff.policy",
        "config${/}assignmentAuthorityVerhoeffSecure.policy", SEQUENCE_AUTHORITY_VERHOEFF_PROP,
        "Provides an identifier generation service for network use. ", true,
        "jiniPortUrlPart, VHelp.addDlVersion(\"assignmentAuthority-dl\"), \"", "lib", dwaPath,
        "org.dwfa.aa.AssignmentAuthorityService", new String[] { "config${/}assignmentAuthorityLongVerhoeff.config" },
        new String[] { "config${/}assignmentAuthorityLongVerhoeffSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_SequenceAuthorityOidUuid = new ServiceConfigOption(OID_UUID_AUTHORITY,
        "config${/}assignmentAuthorityOidUuid.policy", "config${/}assignmentAuthorityOidUuidSecure.policy",
        OID_UUID_AUTHORITY_PROP, "Provides an identifier generation service for network use. ", true,
        "jiniPortUrlPart, VHelp.addDlVersion(\"assignmentAuthority-dl\"), \"", "lib", dwaPath,
        "org.dwfa.aa.AssignmentAuthorityService", new String[] { "config${/}assignmentAuthorityOidUuid.config" },
        new String[] { "config${/}assignmentAuthorityOidUuidSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_SequenceAuthorityUuid = new ServiceConfigOption(UUID_AUTHORITY,
        "config${/}assignmentAuthorityUuid.policy", "config${/}assignmentAuthorityUuidSecure.policy",
        UUID_AUTHORITY_PROP, "Provides an identifier generation service for network use. ", true,
        "jiniPortUrlPart, VHelp.addDlVersion(\"assignmentAuthority-dl\"), \"", "lib", dwaPath,
        "org.dwfa.aa.AssignmentAuthorityService", new String[] { "config${/}assignmentAuthorityUuid.config" },
        new String[] { "config${/}assignmentAuthorityUuidSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_QueueViewer = new ServiceConfigOption(QUEUE_VIEWER,
        "config${/}queueViewer.policy", "config${/}queueViewerSecure.policy", QUEUE_VIEWER_PROP,
        "Allows examination of local and remote queue contents. ", true, "", null, dwaPath,
        "org.dwfa.queue.gui.QueueViewerFrame", new String[] { "config${/}queueViewer.config" },
        new String[] { "config${/}queueViewerSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_ProcessBuilder = new ServiceConfigOption(PROCESS_BUILDER,
        "config${/}processBuilder.policy", "config${/}processBuilderSecure.policy", PROCESS_BUILDER_PROP,
        "Allows building processes and examination/editing of existing processes. ", true, "", null, dwaPath,
        "org.dwfa.bpa.gui.ProcessBuilderFrame", new String[] { "config${/}processBuilder.config" },
        new String[] { "config${/}processBuilderSecure.config" }, true, false, false, "");

    public static String ARCHIVAL_QUEUE = "Archival Queue";
    public static String ARCHIVAL_QUEUE_PROP = "org.dwfa.ARCHIVAL";
    public static ServiceConfigOption CO_Archival = new ServiceConfigOption(ARCHIVAL_QUEUE,
        "config${/}queueArchival.policy", "config${/}queueArchivalSecure.policy", ARCHIVAL_QUEUE_PROP,
        "Publishes an Archival Queue for workflow. ", true, "jiniPortUrlPart, VHelp.addDlVersion(\"queue-dl\"), \"",
        "lib", dwaPath, "org.dwfa.queue.QueueServer", new String[] { "config${/}queueArchival.config" },
        new String[] { "config${/}queueArchivalSecure.config" }, true, false, false, "");

    public static String TIMER = "Timer";
    public static String TIMER_PROP = "org.dwfa.TIMER";
    public static ServiceConfigOption CO_Timer = new ServiceConfigOption(TIMER, "config${/}timeServer.policy",
        "config${/}timeServerSecure.policy", TIMER_PROP, "Starts a time server for workflow. ", true, "", "lib",
        dwaPath, "org.dwfa.clock.TimeServer", new String[] { "config${/}timeServer.config" },
        new String[] { "config${/}timeServerSecure.config" }, true, false, false, "");

    public static String MULTI_QUEUE_STARTER = "Multi queue starter";
    public static String MULTI_QUEUE_STARTER_PROP = "org.dwfa.MULTI_QUEUE_STARTER";
    public static ServiceConfigOption CO_MultiQueueStarter = new ServiceConfigOption(MULTI_QUEUE_STARTER,
        "config${/}multiQueueStarter.policy", "config${/}multiQueueStarterSecure.policy", MULTI_QUEUE_STARTER_PROP,
        "Starts all the queues in a directory. ", true, "", "lib", dwaPath, "org.dwfa.queue.MultiQueueStarter",
        new String[] { "config${/}multiQueueStarter.config" },
        new String[] { "config${/}multiQueueStarterSecure.config" }, true, false, false, "");

    public static String WEB_SERVER = "Web Server";
    public static String WEB_SERVER_PROP = "org.dwfa.WEB_SERVER";
    public static ServiceConfigOption CO_WebServer = new ServiceConfigOption(WEB_SERVER, "config${/}webServer.policy",
        "config${/}webServerSecure.policy", WEB_SERVER_PROP, "Starts a web server for workflow. ", true, "", "lib",
        dwaPath, "org.dwfa.web.DwfaWebServer", new String[] { "config${/}webServer.config" },
        new String[] { "config${/}webServerSecure.config" }, true, false, false, "");

}
