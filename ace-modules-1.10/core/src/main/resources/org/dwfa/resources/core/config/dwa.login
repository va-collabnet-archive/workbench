/* JAAS login configuration file for server */

org.dwfa.log.LogViewerFrame {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="client"
	keyStoreURL="file:prebuiltkeys/client.keystore"
	keyStorePasswordURL="file:prebuiltkeys/client.password";
};

org.dwfa.bpa.queue.QueueServer.aging {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="server"
	keyStoreURL="file:prebuiltkeys/server.keystore"
	keyStorePasswordURL="file:prebuiltkeys/server.password";
};

org.dwfa.bpa.queue.QueueServer.compute {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="server"
	keyStoreURL="file:prebuiltkeys/server.keystore"
	keyStorePasswordURL="file:prebuiltkeys/server.password";
};

org.dwfa.bpa.queue.QueueServer.inbox {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="server"
	keyStoreURL="file:prebuiltkeys/server.keystore"
	keyStorePasswordURL="file:prebuiltkeys/server.password";
};

org.dwfa.bpa.queue.QueueServer.inboxCreateMap {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="server"
	keyStoreURL="file:prebuiltkeys/server.keystore"
	keyStorePasswordURL="file:prebuiltkeys/server.password";
};

org.dwfa.bpa.queue.QueueServer.inboxReview {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="server"
	keyStoreURL="file:prebuiltkeys/server.keystore"
	keyStorePasswordURL="file:prebuiltkeys/server.password";
};

org.dwfa.bpa.queue.QueueServer.launcher {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="server"
	keyStoreURL="file:prebuiltkeys/server.keystore"
	keyStorePasswordURL="file:prebuiltkeys/server.password";
};

org.dwfa.bpa.queue.QueueServer.outbox {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="server"
	keyStoreURL="file:prebuiltkeys/server.keystore"
	keyStorePasswordURL="file:prebuiltkeys/server.password";
};

org.dwfa.bpa.worker.MasterWorker.queueViewer {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="client"
	keyStoreURL="file:prebuiltkeys/client.keystore";
};

org.dwfa.bpa.worker.MasterWorker.processBuilder {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="client"
	keyStoreURL="file:prebuiltkeys/client.keystore";
};

org.dwfa.bpa.queue.QueueServer.AllGroupsOutboxQueueWorker {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="client"
	keyStoreURL="file:prebuiltkeys/client.keystore"
	keyStorePasswordURL="file:prebuiltkeys/client.password";
};

org.dwfa.bpa.queue.QueueServer.InboxQueueWorker {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="client"
	keyStoreURL="file:prebuiltkeys/client.keystore"
	keyStorePasswordURL="file:prebuiltkeys/client.password";
};


org.dwfa.bpa.queue.QueueServer.HeadlessQueueWorker {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="client"
	keyStoreURL="file:prebuiltkeys/client.keystore"
	keyStorePasswordURL="file:prebuiltkeys/client.password";
};

org.dwfa.log.LogManagerService {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="server"
	keyStoreURL="file:prebuiltkeys/server.keystore"
	keyStorePasswordURL="file:prebuiltkeys/server.password";
};


com.sun.jini.example.browser  {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="client"
	keyStoreURL="file:prebuiltkeys/client.keystore"
	keyStorePasswordURL="file:prebuiltkeys/client.password";
};

/* JAAS login configuration file for Mahalo */

com.sun.jini.mahalo {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="server"
	keyStoreURL="file:prebuiltkeys/server.keystore"
	keyStorePasswordURL="file:prebuiltkeys/server.password";
};

/* JAAS login configuration file for Reggie */

com.sun.jini.Reggie {
    com.sun.security.auth.module.KeyStoreLoginModule required
	keyStoreAlias="reggie"
	keyStoreURL="file:prebuiltkeys/reggie.keystore"
	keyStorePasswordURL="file:prebuiltkeys/reggie.password";
};
