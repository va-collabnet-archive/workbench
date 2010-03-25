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
package org.dwfa.vodb.jar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.zip.ZipEntry;

import javax.swing.Timer;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.fd.FileDialogUtil;
import org.dwfa.vodb.bind.ThinConVersionedBinding;
import org.dwfa.vodb.bind.ThinDescVersionedBinding;
import org.dwfa.vodb.bind.ThinIdVersionedBinding;
import org.dwfa.vodb.bind.ThinRelVersionedBinding;
import org.dwfa.vodb.bind.ThinVersionHelper;

import com.sleepycat.je.DatabaseEntry;

/*
 * @todo put reader and writer on different threads...
 */
public class JarWriter implements ActionListener {

    public ExecutorService threadPool = Executors.newFixedThreadPool(5);

    ThinConVersionedBinding conBinding = new ThinConVersionedBinding();

    ThinDescVersionedBinding descBinding = new ThinDescVersionedBinding();

    ThinRelVersionedBinding relBinding = new ThinRelVersionedBinding();

    ThinIdVersionedBinding idBinding = new ThinIdVersionedBinding();

    Exception writerException = null;

    boolean continueWork = true;

    String upperProgressMessage = "Writing Jar File";
    int progressValue = 0;
    int images = 0;
    int max = -1;

    private ConceptWriter conceptWriter;
    private DescritionWriter descWriter;
    private RelationshipWriter relWriter;
    private IdWriter idWriter;
    private ImageWriter imageWriter;
    private ConceptCounter conceptCounter;
    private DescriptionCounter descCounter;
    private RelationshipCounter relCounter;
    private IdCounter idCounter;
    private ImageCounter imageCounter;
    private TimePathCounter timePathCounter;

    private PathWriter pathWriter;

    private PathCounter pathCounter;

    private AceConfig aceConfig;

    private TimePathWriter timePathWriter;

    private class ProgressUpdator implements I_UpdateProgress {
        Timer updateTimer;
        boolean firstUpdate = true;
        ActivityPanel activity = new ActivityPanel(true, null, null);

        public ProgressUpdator() {
            super();
            updateTimer = new Timer(200, this);
            updateTimer.start();
        }

        public void actionPerformed(ActionEvent e) {
            if (firstUpdate) {
                firstUpdate = false;
                try {
                    ActivityViewer.addActivity(activity);
                } catch (Exception e1) {
                    AceLog.getAppLog().alertAndLogException(e1);
                }
            }
            activity.setIndeterminate(max == -1);
            activity.setValue(getTotalProcessed());
            activity.setMaximum(max);
            activity.setProgressInfoUpper(upperProgressMessage);
            activity.setProgressInfoLower("Counted: " + getTotalCount() + " Written: " + getTotalProcessed());
            if (!continueWork) {
                activity.complete();
                updateTimer.stop();
            }
            if (writerException != null) {
                activity.setProgressInfoLower(writerException.getMessage());
            }
        }

        public void normalCompletion() {
            activity.complete();
            updateTimer.stop();
            if (writerException != null) {
                activity.setProgressInfoLower(writerException.getMessage());
            }
        }
    }

    public JarWriter(AceConfig aceConfig) {
        this.aceConfig = aceConfig;
        ACE.threadPool.execute(new Runnable() {
            public void run() {
                try {
                    exportJar();
                } catch (TaskFailedException ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            }
        });
    }

    public DatabaseEntry getDataEntry() {
        return new DatabaseEntry();
    }

    public DatabaseEntry getKeyEntry() {
        return new DatabaseEntry();
    }

    /*
     * @todo add a digital signature capability.
     * http://www.onjava.com/pub/a/onjava/2001/04/12/signing_jar.html?page=1
     */
    private void exportJar() throws TaskFailedException {
        try {
            File jarFile = FileDialogUtil.getNewFile("Export all to new jar file", null, aceConfig.getActiveFrame());
            File tempDir = new File(UUID.randomUUID().toString());
            tempDir.mkdirs();
            File conceptFile = new File(tempDir, "concepts.ace");
            File descFile = new File(tempDir, "descriptions.ace");
            File relFile = new File(tempDir, "relationships.ace");
            File idFile = new File(tempDir, "ids.ace");
            File imageFile = new File(tempDir, "images.ace");
            File pathFile = new File(tempDir, "paths.ace");
            File timeZeroFile = new File(tempDir, "timeZero.ace");
            File timePathFile = new File(tempDir, "timePath.ace");
            DataOutputStream tzdos = new DataOutputStream(new FileOutputStream(timeZeroFile));
            tzdos.writeLong(ThinVersionHelper.getTimeZero());
            tzdos.writeInt(ThinVersionHelper.getTimeZeroInt());
            tzdos.close();

            File aceConfigFile = new File(tempDir, "config.ace");
            ObjectOutputStream acoos = new ObjectOutputStream(new FileOutputStream(aceConfigFile));
            acoos.writeObject(aceConfig);
            acoos.close();

            conceptWriter = new ConceptWriter(new FileOutputStream(conceptFile));
            descWriter = new DescritionWriter(new FileOutputStream(descFile));
            relWriter = new RelationshipWriter(new FileOutputStream(relFile));
            idWriter = new IdWriter(new FileOutputStream(idFile));
            imageWriter = new ImageWriter(new FileOutputStream(imageFile));
            pathWriter = new PathWriter(new FileOutputStream(pathFile));
            timePathWriter = new TimePathWriter(new FileOutputStream(timePathFile));

            String prefix = "Writing jar file: ";
            Stopwatch allTime = new Stopwatch();
            allTime.start();
            upperProgressMessage = prefix + "Counting components";

            conceptCounter = new ConceptCounter();
            descCounter = new DescriptionCounter();
            relCounter = new RelationshipCounter();
            idCounter = new IdCounter();
            imageCounter = new ImageCounter();
            pathCounter = new PathCounter();
            timePathCounter = new TimePathCounter();

            ProgressUpdator updater = new ProgressUpdator();
            updater.activity.addActionListener(this);

            FutureTask<Object> conceptWriterTask = new FutureTask<Object>(conceptWriter);
            FutureTask<Object> descWriterTask = new FutureTask<Object>(descWriter);
            FutureTask<Object> relWriterTask = new FutureTask<Object>(relWriter);
            FutureTask<Object> idWriterTask = new FutureTask<Object>(idWriter);
            FutureTask<Object> imageWriterTask = new FutureTask<Object>(imageWriter);
            FutureTask<Object> pathWriterTask = new FutureTask<Object>(pathWriter);
            FutureTask<Object> conceptCounterTask = new FutureTask<Object>(conceptCounter);
            FutureTask<Object> descCounterTask = new FutureTask<Object>(descCounter);
            FutureTask<Object> relCounterTask = new FutureTask<Object>(relCounter);
            FutureTask<Object> idCounterTask = new FutureTask<Object>(idCounter);
            FutureTask<Object> imageCounterTask = new FutureTask<Object>(imageCounter);
            FutureTask<Object> pathCounterTask = new FutureTask<Object>(pathCounter);
            FutureTask<Object> timePathCounterTask = new FutureTask<Object>(timePathCounter);
            FutureTask<Object> timePathWriterTask = new FutureTask<Object>(timePathWriter);

            threadPool.submit(conceptCounterTask);
            threadPool.submit(descCounterTask);
            threadPool.submit(relCounterTask);
            threadPool.submit(idCounterTask);
            threadPool.submit(imageCounterTask);
            threadPool.submit(timePathCounterTask);
            threadPool.submit(pathCounterTask);
            threadPool.submit(conceptWriterTask);
            threadPool.submit(descWriterTask);
            threadPool.submit(relWriterTask);
            threadPool.submit(idWriterTask);
            threadPool.submit(imageWriterTask);
            threadPool.submit(pathWriterTask);
            threadPool.submit(timePathWriterTask);

            conceptCounterTask.get();
            upperProgressMessage = prefix + "Concepts count complete";
            descCounterTask.get();
            upperProgressMessage = prefix + "Descriptions count complete";
            relCounterTask.get();
            upperProgressMessage = prefix + "Rel count complete";
            imageCounterTask.get();
            upperProgressMessage = prefix + "Image count complete";
            pathCounterTask.get();
            upperProgressMessage = prefix + "Path count complete";
            idCounterTask.get();
            upperProgressMessage = prefix + "Id count complete";
            timePathCounterTask.get();
            upperProgressMessage = prefix + "Time path count complete";

            max = getTotalCount();

            upperProgressMessage = prefix + "Writing";

            conceptWriterTask.get();
            upperProgressMessage = prefix + "Concepts complete";
            descWriterTask.get();
            upperProgressMessage = prefix + "Descriptions complete";
            imageWriterTask.get();
            upperProgressMessage = prefix + "Images complete";
            pathWriterTask.get();
            upperProgressMessage = prefix + "Paths complete";
            relWriterTask.get();
            upperProgressMessage = prefix + "Relationships complete";

            idWriterTask.get();
            upperProgressMessage = prefix + "Ids complete";

            timePathWriterTask.get();
            upperProgressMessage = prefix + "Time path file complete";

            upperProgressMessage = prefix + "Assembling jar file";

            Manifest manifest = new Manifest();
            Attributes a = manifest.getMainAttributes();
            a.putValue(Name.MANIFEST_VERSION.toString(), "1.0");
            a.putValue(Name.MAIN_CLASS.toString(), JarExtractor.class.getCanonicalName());

            Map<String, Attributes> entries = manifest.getEntries();
            setAttributesForEntry(entries, conceptFile.getName(), conceptCounter.getCount());
            setAttributesForEntry(entries, descFile.getName(), descCounter.getCount());
            setAttributesForEntry(entries, relFile.getName(), relCounter.getCount());

            int minId = AceConfig.getVodb().getMinId();
            int maxId = AceConfig.getVodb().getMaxId();
            setAttributesForEntry(entries, idFile.getName(), idCounter.getCount(), true, minId, maxId);
            setAttributesForEntry(entries, imageFile.getName(), imageCounter.getCount());
            setAttributesForEntry(entries, pathFile.getName(), pathCounter.getCount());
            setAttributesForEntry(entries, timePathFile.getName(), timePathCounter.getCount());
            setAttributesForEntry(entries, timeZeroFile.getName(), 1);
            setAttributesForEntry(entries, aceConfigFile.getName(), 1);

            FileOutputStream fos = new FileOutputStream(jarFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            JarOutputStream output = new JarOutputStream(bos, manifest);

            addToZip(Class.forName(JarExtractor.class.getCanonicalName()), output);

            addToZip(conceptFile, output, "items: " + Integer.toString(conceptCounter.getCount()));
            addToZip(descFile, output, "items: " + Integer.toString(descCounter.getCount()));
            addToZip(relFile, output, "items: " + Integer.toString(relCounter.getCount()));
            addToZip(idFile, output, "items: " + Integer.toString(idCounter.getCount()));
            addToZip(imageFile, output, "items: " + Integer.toString(imageCounter.getCount()));
            addToZip(pathFile, output, "items: " + Integer.toString(pathCounter.getCount()));
            addToZip(timePathFile, output, "items: " + Integer.toString(timePathCounter.getCount()));
            addToZip(timeZeroFile, output, "items: 1");
            addToZip(aceConfigFile, output, "items: 1");

            output.close();
            upperProgressMessage = prefix + "Complete. Total time: " + allTime.getElapsedTime() / (1000 * 60) + " min.";
            AceLog.getAppLog().info("Total time: " + allTime.getElapsedTime());
            continueWork = false;

            conceptFile.delete();
            descFile.delete();
            relFile.delete();
            idFile.delete();
            imageFile.delete();
            pathFile.delete();
            timeZeroFile.delete();
            aceConfigFile.delete();
            timePathFile.delete();
            tempDir.delete();

        } catch (TaskFailedException ex) {
            this.writerException = ex;
            cancel();
            throw ex;
        } catch (Exception ex) {
            this.writerException = ex;
            cancel();
            throw new TaskFailedException(ex);
        }
    }

    private int getTotalCount() {
        return conceptCounter.count + descCounter.count + relCounter.count + idCounter.count + imageCounter.count
            + timePathCounter.count;
    }

    private int getTotalProcessed() {
        return conceptWriter.count + descWriter.count + relWriter.count + idWriter.count + imageWriter.count
            + timePathWriter.count;
    }

    private void setAttributesForEntry(Map<String, Attributes> entries, String nameStr, int count) {
        Attributes attributes = new Attributes();
        attributes.put(new Attributes.Name("count"), Integer.toString(count));
        entries.put(nameStr, attributes);
    }

    private void setAttributesForEntry(Map<String, Attributes> entries, String nameStr, int count, boolean continuous,
            int min, int max) {
        Attributes attributes = new Attributes();
        attributes.put(new Attributes.Name("count"), Integer.toString(count));
        attributes.put(new Attributes.Name("continuous"), Boolean.toString(continuous));
        attributes.put(new Attributes.Name("min"), Integer.toString(min));
        attributes.put(new Attributes.Name("max"), Integer.toString(max));
        entries.put(nameStr, attributes);
    }

    /*
     * TODO Move this method to foundation
     */
    private void addToZip(Class<?> theClass, JarOutputStream output) throws IOException, ClassNotFoundException {
        String classFileName = theClass.getName().replace('.', '/') + ".class";
        ZipEntry entry = new ZipEntry(classFileName);
        output.putNextEntry(entry);

        URL classUrl = theClass.getResource("/" + classFileName);
        AceLog.getAppLog().info(classUrl.toString());
        AceLog.getAppLog().info(classUrl.getContent().toString());

        InputStream classInputStream = classUrl.openStream();
        int size = classInputStream.available();
        byte[] data = new byte[size];
        classInputStream.read(data, 0, size);
        output.write(data, 0, size);
        output.closeEntry();
    }

    /*
     * TODO Move this method to foundation
     */
    private void addToZip(File f, JarOutputStream output, String comment) throws IOException {
        ZipEntry entry = new ZipEntry(f.getName());
        entry.setSize(f.length());
        entry.setTime(f.lastModified());
        entry.setComment(comment);
        output.putNextEntry(entry);
        FileInputStream fis = new FileInputStream(f);
        byte[] buf = new byte[10240];
        for (int i = 0;; i++) {
            int len = fis.read(buf);
            if (len < 0)
                break;
            output.write(buf, 0, len);
        }
        output.closeEntry();

    }

    public void actionPerformed(ActionEvent e) {
        writerException = new Exception("User Canceled Operation.");
        cancel();
    }

    private void cancel() {
        continueWork = false;
        conceptCounter.cancel();
        conceptWriter.cancel();
        descCounter.cancel();
        descWriter.cancel();
        relCounter.cancel();
        relWriter.cancel();
        idCounter.cancel();
        idWriter.cancel();
        imageCounter.cancel();
        imageWriter.cancel();
    }

}
