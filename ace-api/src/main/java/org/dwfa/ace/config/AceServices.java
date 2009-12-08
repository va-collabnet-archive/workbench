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
        /*
         * Order is important. You want the registration services to start
         * in the first phase, and the queue viewer to start in the last phase.
         */
        add(CoreServices.CO_ClassServer);
        add(CoreServices.CO_ServiceRegistrar);
        add(CoreServices.CO_TransactionAggregator);
        add(CoreServices.CO_TransactionManager);
        // add(CoreServices.CO_Timer);

        // add(CoreServices.CO_AgingQueue);
        // add(CoreServices.CO_Archival);
        // add(CoreServices.CO_ComputeQueue);
        // add(CoreServices.CO_LauncherQueue);
        // add(CoreServices.CO_OutboxQueue);
        // add(CoreServices.CO_SyncQueue);
        // add(CoreServices.CO_Inbox);
        add(CoreServices.CO_MultiQueueStarter);

        // add(CO_queue_kp_user2_editor_Inbox);
        // add(CO_queue_kp_user2_editor_Outbox);
        // add(CO_queue_kp_user3_editor_Inbox);
        // add(CO_queue_kp_user3_editor_Outbox);
        // add(CO_queue_va_user1_editor_Inbox);
        // add(CO_queue_va_user1_editor_Outbox);
        // add(CO_queue_va_user1_assignmentManager_Inbox);
        // add(CO_queue_va_user1_assignmentManager_Outbox);
        // add(CO_queue_va_user4_editor_Inbox);
        // add(CO_queue_va_user4_editor_Outbox);

        // add(CoreServices.CO_GenericWorkerManager);
        // add(CoreServices.CO_GenericWorkerManagerNewFrame);
        // add(CoreServices.CO_JavaSpace);
        add(CoreServices.CO_LogManagerService);
        add(CoreServices.CO_LogViewerNewFrame);
        add(CoreServices.CO_PhantomFrame);
        add(CoreServices.CO_ServiceBrowserNewFrame);
        // add(CoreServices.CO_ProcessBuilder);
        // add(CO_FormBuilderNewFrame);
        add(CO_AceEditor);

    }

    public static String FORM_BUILDER = "Form Builder";
    public static String FORM_BUILDER_PROP = "org.jehri.FORM_BUILDER";

    public static ServiceConfigOption CO_FormBuilderNewFrame = new ServiceConfigOption(FORM_BUILDER,
        "config${/}newWindowGenerator.policy", "config${/}newWindowGeneratorSecure.policy", FORM_BUILDER_PROP,
        "Allows building encoded forms. ", true, "", null, CoreServices.dwaPath,
        "org.dwfa.bpa.util.NewWindowGenerator", // mainclass
        new String[] { "config${/}newLogViewerFrame.config" }, // args
        new String[] { "config${/}newLogViewerFrameSecure.config" }, // secure
                                                                     // args
        true, false, false, "");

    public static String ACE_EDITOR_SERVICE = "Chart Service";
    public static String ACE_EDITOR_SERVICE_PROP = "org.dwfa.ACE_EDITOR_SERVICE";
    public static ServiceConfigOption CO_AceEditor = new ServiceConfigOption(ACE_EDITOR_SERVICE,
        "config${/}ace.policy", "config${/}aceSecure.policy", ACE_EDITOR_SERVICE_PROP, "Starts the Ace Editor. ", true,
        "", null, CoreServices.dwaPath, "org.dwfa.ace.config.AceRunner", new String[] { "config${/}ace.config" },
        new String[] { "config${/}aceSecure.config" }, true, false, false, "");

    public static ServiceConfigOption CO_queue_kp_user2_editor_Inbox = configInbox("queue.kp.user2.editor.Inbox");
    public static ServiceConfigOption CO_queue_kp_user2_editor_Outbox = configOutbox("queue.kp.user2.editor.Outbox");
    public static ServiceConfigOption CO_queue_kp_user3_editor_Inbox = configInbox("queue.kp.user3.editor.Inbox");
    public static ServiceConfigOption CO_queue_kp_user3_editor_Outbox = configOutbox("queue.kp.user3.editor.Outbox");
    public static ServiceConfigOption CO_queue_va_user1_editor_Inbox = configInbox("queue.va.user1.editor.Inbox");
    public static ServiceConfigOption CO_queue_va_user1_editor_Outbox = configOutbox("queue.va.user1.editor.Outbox");
    public static ServiceConfigOption CO_queue_va_user1_assignmentManager_Inbox = configInbox("queue.va.user1.assignmentManager.Inbox");
    public static ServiceConfigOption CO_queue_va_user1_assignmentManager_Outbox = configOutbox("queue.va.user1.assignmentManager.Outbox");
    public static ServiceConfigOption CO_queue_va_user4_editor_Inbox = configInbox("queue.va.user4.editor.Inbox");
    public static ServiceConfigOption CO_queue_va_user4_editor_Outbox = configOutbox("queue.va.user4.editor.Outbox");

    public static ServiceConfigOption configInbox(String queueName) {
        ServiceConfigOption sco = new ServiceConfigOption(CoreServices.INBOX_QUEUE + queueName, "config${/}"
            + queueName + ".policy", "config${/}" + queueName + "Secure.policy", CoreServices.INBOX_QUEUE_PROP
            + queueName, "Publishes an Inbox Queue for business processes. ", true,
            "jiniPortUrlPart, VHelp.addDlVersion(\"queue-dl\"), \"", "lib", CoreServices.dwaPath,
            "org.dwfa.queue.QueueServer", new String[] { "config${/}" + queueName + ".config" },
            new String[] { "config${/}" + queueName + "Secure.config" }, true, false, true,
            "<html>Every workflow node needs at least one inbox queue to recieve processes<br>"
                + " from other nodes. Please ensure that at least one of the machines in this node<br>"
                + " has an inbox queue.");
        sco.setPrefix(queueName.replace('.', '_'));
        return sco;
    }

    public static ServiceConfigOption configOutbox(String queueName) {
        ServiceConfigOption sco = new ServiceConfigOption(CoreServices.OUTBOX_QUEUE + queueName, "config${/}"
            + queueName + ".policy", "config${/}" + queueName + "Secure.policy", CoreServices.OUTBOX_QUEUE_PROP
            + queueName, "Publishes an OutBox Queue for workflow. ", true,
            "jiniPortUrlPart, VHelp.addDlVersion(\"queue-dl\"), \"", "lib", CoreServices.dwaPath,
            "org.dwfa.queue.QueueServer", new String[] { "config${/}" + queueName + ".config" },
            new String[] { "config${/}" + queueName + "Secure.config" }, true, false, true,
            "<html>Every workflow node needs at least one outbox queue to send processes<br>"
                + " to other nodes. Please ensure that at least one of the machines in this node<br>"
                + " has an outbox queue.");
        sco.setPrefix(queueName.replace('.', '_'));
        return sco;
    }

}
