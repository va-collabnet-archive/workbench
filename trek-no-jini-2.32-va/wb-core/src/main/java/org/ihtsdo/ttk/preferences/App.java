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
        final String userName = "ocarlsen";    // TODO: Get from application.
        final String version = "1.0-SNAPHOT";  // TODO: Use ${version} from Maven during build.
        final String appPrefix = appId + ":" + version + ":" + userName;

        // Check args to try different scenarios.
        int argCount = args.length;
        if (argCount == 0) {
            getEmptyPrefsGetQueueListExportToFileThenRemove(appPrefix, "Test.xml");
        } else if (argCount == 1) {
            String action = args[0];
            if (action.equals("clean")) {
                EnumBasedPreferences prefs = getSyncedPrefs(appPrefix);
                removeFromBackingStore(prefs);
            } else {
                throw new UnsupportedOperationException("Unsupported action: " + action);
            }
        } else if (argCount == 2) {
            String action = args[0];
            String fileName = args[1];
            if (action.equals("import")) {
                EnumBasedPreferences prefs = importFromFile(fileName, appPrefix);
                QueueList queueList = new QueueList(prefs);
                System.out.println(queueList);
            } else if (action.equals("export")) {
                EnumBasedPreferences prefs = exportToFile(fileName, appPrefix);
            } else {
                throw new UnsupportedOperationException("Unsupported action: " + action);
            }
        }
    }

    private static void getEmptyPrefsGetQueueListExportToFileThenRemove(String appPrefix, String fileName)
            throws FileNotFoundException, BackingStoreException, IOException {
        EnumBasedPreferences prefs = getEmptyPrefs(appPrefix);
        QueueList queueList = getQueueList(prefs);

        queueList.exportFields(prefs);
        prefs.exportSubtree(new FileOutputStream(fileName));

        // Clean up.
        removeFromBackingStore(prefs);
    }

    private static void removeFromBackingStore(EnumBasedPreferences prefs)
            throws BackingStoreException {
        prefs.removeNode();
        prefs.flush();
    }

    private static EnumBasedPreferences importFromFile(String fileName, String appPrefix)
            throws FileNotFoundException, IOException,
            InvalidPreferencesFormatException, BackingStoreException {
        Preferences.importPreferences(new FileInputStream(fileName));
        return getSyncedPrefs(appPrefix);
    }

    private static EnumBasedPreferences exportToFile(String fileName, String appPrefix)
            throws FileNotFoundException, IOException,
            InvalidPreferencesFormatException, BackingStoreException {
        EnumBasedPreferences prefs = new EnumBasedPreferences(appPrefix);
        prefs.exportSubtree(new FileOutputStream(fileName));
        return prefs;
    }

    private static EnumBasedPreferences getSyncedPrefs(String appPrefix)
            throws BackingStoreException {
        EnumBasedPreferences prefs = new EnumBasedPreferences(appPrefix);
        prefs.sync();  // Bring up-to-date with backing store.
        return prefs;
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
