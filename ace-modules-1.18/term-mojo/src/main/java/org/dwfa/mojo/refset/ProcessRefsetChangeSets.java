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
package org.dwfa.mojo.refset;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;

/**
 * Simple Mojo used to strip down change sets to express only the refset changes
 * made by a server process (another mojo). Its purpose is to make the change
 * sets smaller and faster to import which will be unnecessary when a new
 * VODB version is available.
 * 
 * @goal processRefsetChangeSets
 * @phase process-classes
 * @requiresDependencyResolution compile
 * @author Dion McMurtrie
 */
public class ProcessRefsetChangeSets extends AbstractMojo {
    /**
     * To show how dodgy it is, it is even runnable from the command line.
     * Just pass in the path to the change sets and it will recursively
     * look in the specified directory and child directories.
     * 
     * @param args
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    public static void main(String[] args) throws MojoExecutionException, MojoFailureException {
        ProcessRefsetChangeSets process = new ProcessRefsetChangeSets();
        process.changeSetDirectory = new File(args[0]);
        process.execute();
    }

    /**
     * Threshold number of changes per file, defaults to 1000.
     * 
     * @parameter
     */
    private int fileBatchSize = 1000;

    /**
     * Location of the change sets, recursively descended for .jcs files.
     * 
     * @parameter
     * @required
     */
    private File changeSetDirectory;

    private Map<String, Integer> fileNameMap = new HashMap<String, Integer>();

    public void execute() throws MojoExecutionException, MojoFailureException {
        assert changeSetDirectory.isDirectory() : "the specified file must be a directory!";

        try {
            processDirectory(changeSetDirectory);
        } catch (Exception e) {
            throw new MojoExecutionException("error processing change sets", e);
        }
    }

    private void processDirectory(File directory) throws FileNotFoundException, IOException, ClassNotFoundException {

        File[] files = directory.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory() || pathname.getName().endsWith(".jcs");
            }

        });

        for (File file : files) {
            if (file.isDirectory()) {
                processDirectory(file);
            } else {
                processChangeset(file);
            }
        }
    }

    private void processChangeset(File changeset) throws IOException, FileNotFoundException, ClassNotFoundException {
        getLog().info("processing " + changeset);
        String changeSetBaseName = changeset.getName();

        File newChangeSet = new File(changeset.getParent(), createNewFile(changeSetBaseName));
        File oldChangeSet = new File(changeset.getParent(), changeset.getName() + ".old");
        changeset.renameTo(oldChangeSet);

        ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(oldChangeSet)));
        ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(newChangeSet)));

        int refsetBeans = 0;
        try {
            Object firstObject = ois.readObject();
            getLog().info("First object - " + firstObject);

            oos.writeObject(firstObject);

            Long time = ois.readLong();

            int count = 0;
            while (time != Long.MAX_VALUE) {
                Object obj = ois.readObject();
                if (obj instanceof UniversalAceExtByRefBean) {
                    oos.writeLong(time);
                    oos.writeObject(obj);
                    refsetBeans++;
                    if (refsetBeans % fileBatchSize == 0) {
                        oos.close();
                        newChangeSet = new File(newChangeSet.getParent(), createNewFile(changeSetBaseName));
                        oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(newChangeSet)));
                        oos.writeObject(firstObject);
                        refsetBeans = 0;
                    }
                }

                if (count++ % 1000 == 0) {
                    oos.flush();
                }

                time = ois.readLong();
            }
        } catch (EOFException ex) {
            ois.close();
            oos.close();
            oldChangeSet.delete();
            if (refsetBeans == 0) {
                // delete the file if there is nothing in it.
                getLog().info("removing empty file " + newChangeSet);
                newChangeSet.delete();
            }
            getLog().info("End of change set. ");
        }

    }

    private String createNewFile(String basename) throws IOException {
        int fileNumber;
        String newFileName;

        if (fileNameMap.containsKey(basename)) {
            fileNumber = fileNameMap.get(basename);
            fileNumber++;
            getLog().info("rolled over to new file number " + fileNumber + " for basename " + basename);
            newFileName = basename.replaceAll(".jcs", "." + fileNumber + ".jcs");
            fileNameMap.put(basename, fileNumber);
        } else {
            fileNameMap.put(basename, 0);
            newFileName = basename;
        }

        return newFileName;
    }

}
