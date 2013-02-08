package org.ihtsdo.ttk.preferences;

import org.ihtsdo.ttk.queue.QueueAddress;
import org.ihtsdo.ttk.queue.QueueList;
import org.ihtsdo.ttk.queue.QueuePreferences;
import org.ihtsdo.ttk.queue.QueueType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args)
            throws Exception {

        final String appId = "org.kp:cmt-pa";  // TODO: Use ${groupId}, ${artifactId} from Maven during build.
        final String userName = "ocarlsen";      // TODO: Get from application.
        final String version = "1.0-SNAPHOT";    // TODO: Use ${version} from Maven during build.
        final String appPrefix = appId + ":" + userName + "/" + version;

        // Check args to try different scenarios.
        int argCount = args.length;
        if (argCount == 0) {
            getEmptyPrefsGetQueueListExportToFileThenRemove(appPrefix, "Test.xml");
        } else if (argCount == 1) {
            String fileName = args[0];
            getEmptyPrefsImportFromFileCreateQueueList(fileName, appPrefix);
        }
    }

    private static void getEmptyPrefsGetQueueListExportToFileThenRemove(String appPrefix, String fileName)
            throws FileNotFoundException, BackingStoreException, IOException {
        EnumBasedPreferences prefs = getEmptyPrefs(appPrefix);
        QueueList queueList = getQueueList(prefs);

        queueList.exportFields(prefs);
        prefs.exportSubtree(new FileOutputStream(fileName));

        // Clean up.
        prefs = prefs.parent();  // Walk back up to appName node.
        prefs.removeNode();
        prefs.flush();
    }

    private static void getEmptyPrefsImportFromFileCreateQueueList(String fileName, String appPrefix)
            throws FileNotFoundException, IOException,
            InvalidPreferencesFormatException, BackingStoreException {
        Preferences.importPreferences(new FileInputStream(fileName));
        EnumBasedPreferences prefs = new EnumBasedPreferences(appPrefix);
        prefs.sync();  // Bring up-to-date with backing store.

        QueueList queueList = new QueueList(prefs);
        System.out.println(queueList);
    }

    private static EnumBasedPreferences getEmptyPrefs(String appPrefix)
            throws BackingStoreException {
        EnumBasedPreferences prefs = new EnumBasedPreferences(appPrefix);

        // Remove from backing store.
        prefs.removeNode();
        prefs.flush();

        return new EnumBasedPreferences(appPrefix);
    }

    private static QueueList getQueueList(EnumBasedPreferences prefs)
            throws BackingStoreException, IOException {
        QueueList queueList = new QueueList(prefs);
        System.out.println(queueList);

        QueuePreferences qp1 = new QueuePreferences();
        queueList.getQueuePreferences().add(qp1);
        System.out.println(queueList);

        QueuePreferences qp2 = new QueuePreferences();
        qp2.setQueueDirectory(new File("some other location"));
        qp2.getServiceItemProperties().clear();
        qp2.getServiceItemProperties().add(new QueueAddress("qp2 address"));
        qp2.getServiceItemProperties().add(new QueueType(QueueType.Types.OUTBOX));
        queueList.getQueuePreferences().add(qp2);
        System.out.println(queueList);

        return queueList;
    }
}
