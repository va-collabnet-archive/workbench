package org.ihtsdo.ttk.preferences;

//~--- non-JDK imports --------------------------------------------------------

import java.io.File;
import org.ihtsdo.ttk.queue.QueuePreferences;

//~--- JDK imports ------------------------------------------------------------

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import org.ihtsdo.ttk.queue.QueueAddress;
import org.ihtsdo.ttk.queue.QueueList;
import org.ihtsdo.ttk.queue.QueueType;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        try {
            EnumBasedPreferences pref      = new EnumBasedPreferences("com.ihtsdo.test");
            pref.removeNode();
            pref.flush();
            pref      = new EnumBasedPreferences("com.ihtsdo.test");
            EnumBasedPreferences prefChild = pref.node("child");

            QueuePreferences     qp        = new QueuePreferences();
            EnumBasedPreferences queueNode = pref.node("queue");

            qp.exportFields(queueNode);
            pref.exportSubtree(new FileOutputStream("Test.xml"));
            pref.removeNode();
            pref.flush();
            Preferences.importPreferences(new FileInputStream("Test.xml"));
            pref = new EnumBasedPreferences("com.ihtsdo.test");
            qp   = new QueuePreferences(pref.node("queue"));
            System.out.println("Queue preferences: " + qp);
            
            pref.removeNode();
            pref.flush();
            pref = new EnumBasedPreferences("com.ihtsdo.test");

            QueueList ql = new QueueList();
            ql.exportFields(pref);
            pref.exportSubtree(new FileOutputStream("Test.xml"));
            
            ql = new QueueList(pref);
            System.out.println(ql);
            
            ql.getQueueList().add(new QueuePreferences());
            
            QueuePreferences qp2 = new QueuePreferences();
            qp2.setQueueDirectory(new File("some other location"));
            qp2.getServiceItemProperties().clear();
            qp2.getServiceItemProperties().add(new QueueAddress("qp2 address"));
            qp2.getServiceItemProperties().add(new QueueType(QueueType.Types.OUTBOX));
            
            ql.getQueueList().add(qp2);
            ql.exportFields(pref);
            pref.exportSubtree(new FileOutputStream("Test.xml"));

            QueueList ql3 = new QueueList(pref);
            System.out.println(ql3);
        } catch (InvalidPreferencesFormatException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | BackingStoreException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
            
     }
}
