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
package org.dwfa.mojo;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;
import java.net.PasswordAuthentication;
import java.net.Authenticator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.LinkedList;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;

import org.dwfa.util.io.FileIO;
import org.dwfa.util.io.JarExtractor;


/**
 * @goal update-from-repository
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class UpdateFromRepository extends AbstractMojo {

    /**
     * The remote repository URL.
     * @parameter
     * @required
     */
    private URL remoteRepositoryUrl;

    /**
     * Location for extracted / copied files.
     * @parameter expression="${project.build.directory}/generated-resources/extracted"
     */
    private File extractedOutputFolder;

    /**
     * Location for downloaded files.
     * @parameter expression="${project.build.directory}/generated-resources/unextracted"
     */
    private File unextractedOutputFolder;

    /**
     * The remote artifact's id.
     * @required
     * @parameter
     */
    private String remoteArtifactId;

    /**
     * Version of the remote artifact.
     * @required
     * @parameter
     */
    private String remoteVersion;

    /**
     * Group id of the remote artifact.
     * @required
     * @parameter
     */
    private String remoteGroupId;

    /**
     * Remote username.
     * @required
     * @parameter
     */
    private String remoteUsername;

    /**
     * Remote password.
     * @required
     * @parameter
     */
    private String remotePassword;

    /**
     * Location of local repository.
     * @parameter expression="${settings.localRepository}"
     */
    private String localRepository;

    /**
     * A list of files that will be extracted (or copied).
     */
    private LinkedList<File> filesToExtract = new LinkedList<File>();

    /**
     * Executes update-from-repository goal.
     * @throws MojoExecutionException if execution causes an exception.
     * @throws MojoFailureException if the mojo fails to complete execution.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Remote repository: " + remoteRepositoryUrl);

            remoteGroupId = remoteGroupId.replace('.', '/');
            localRepository += "/" + remoteGroupId + "/"
                            + remoteArtifactId + "/" + remoteVersion;
            getLog().info("Local repository: " + localRepository);

            // get a listing of all the relevant files on the remote repository
            HashMap remoteFiles = getRemoteFileList();

            // get the latest remote version
            Version latestRemoteVersion = getLatestVersion(remoteFiles);
            getLog().info("Latest remote version: " + latestRemoteVersion);

            // get the latest local version
            Version latestLocalVersion = getLatestLocalVersion();
            getLog().info("Latest local version: " + latestLocalVersion);

            if (latestLocalVersion.equals(Integer.MIN_VALUE)
                    && latestRemoteVersion.equals(Integer.MIN_VALUE)) {
                throw new MojoFailureException("Remote repository does not "
                        + "contain any versions of the artifact, nor does the "
                        + "repository.");
            }

            File localArtifactDirectory = new File(localRepository);

            // check which version is the latest
            if (latestLocalVersion.after(latestRemoteVersion) ||
                    latestLocalVersion.equals(latestRemoteVersion)) {
                // no need to copy
                getLog().info("---- Local copy is later than or equal to "
                        + "remote version.");
                getLocalFilesToExtract(latestLocalVersion);
            } else {
                getLog().info("---- Downloading latest version of " +
                        "artifact (" + latestRemoteVersion.getVersion()
                        + " from remote repository.");

                LinkedList<String> urls=latestRemoteVersion.getUrls();

                // download each file
                for (String urlString : urls) {
                    URL url = new URL(remoteRepositoryUrl.toString()
                            + urlString);
                    downloadFromRemote(url, urlString.substring(
                            urlString.lastIndexOf('/'), urlString.length()));
                }

                // copy each downloaded file to the local repository
                copyToLocalRepository(localArtifactDirectory);
            }

            // extract/copy the files to specified directory
            extractToDirectory();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new MojoFailureException("Malformed URL.");
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException("IOException occurred.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoFailureException("Exception occurred.");
        }
    }

    /**
     * Generates a list of the latest version files in the local repository.
     * @param latestLocalVersion The latest version available in the local
     * repository.
     */
    private void getLocalFilesToExtract(Version latestLocalVersion) {
        File localArtifactDirectory = new File(localRepository);
        if (localArtifactDirectory.exists()) {
            if (localArtifactDirectory.isDirectory()) {
                File[] files = localArtifactDirectory.listFiles();
                for (File file: files) {
                    if (!file.isDirectory()) {
                        Version currentVersion =
                            getVersionFromString(file.getName());
                        if (currentVersion != null &&
                                currentVersion.equals(latestLocalVersion)) {
                            filesToExtract.add(file);
                        }
                    }
                }
            }
        }
    }

    /**
     * Extracts jars and copies other file types to destination directory.
     * @throws Exception May occur if the copy file or jar extraction fails.
     */
    private void extractToDirectory() throws Exception {
        getLog().info("---- Copying/extracting to "
                + extractedOutputFolder);

        for (File localFile : filesToExtract) {
            getLog().info("Local file: " + localFile);
            extractedOutputFolder.mkdirs();
            if (!localFile.exists()) {
                throw new MojoFailureException ("File: " + localFile
                        + " not found "
                        + "in local repository.");
            } else if (localFile.toString().endsWith(".jar")) {
                // extract jars
                getLog().info("Extracting jar: " + localFile.getName());
                JarExtractor.execute(localFile, extractedOutputFolder);
            } else {
                // copy other files
                getLog().info("Copying file: " + localFile.getName());
                FileIO.copyFile(localFile.toString(),
                        extractedOutputFolder + "/" + localFile.getName());
            }
        }

        getLog().info("---- Finished copying/extracting artifact to "
                + extractedOutputFolder);
    }

    /**
     * Calculates the latest version available in the local repository.
     * @return the latest version avilable in the local repository.
     */
    private Version getLatestLocalVersion() {
        Version latestLocalVersion = new Version(Integer.MIN_VALUE);
        File localArtifactDirectory = new File(localRepository);
        if (localArtifactDirectory.exists()) {
            if (localArtifactDirectory.isDirectory()) {
                File[] files = localArtifactDirectory.listFiles();
                for (File file: files) {
                    if (!file.isDirectory()) {
                        // potential earlier version, need to check
                        Version currentTimestamp =
                            getVersionFromString(file.getName());
                        if (currentTimestamp != null
                                &&
                                currentTimestamp.after(latestLocalVersion)) {
                            latestLocalVersion = currentTimestamp;
                        }
                    }
                }
            }
        } else {
            localArtifactDirectory.mkdirs();
        }
        return latestLocalVersion;
    }

    /**
     * Generates a list of the files located in the specified folder of the
     * remote repository.
     *
     * @return HashMap where unique Version strings are mapped to Version
     * objects, which maintain a list of associated URL's.
     * @throws Exception IO exception.
     */
    private HashMap getRemoteFileList() throws Exception {
        File file = downloadFromRemote(new URL(remoteRepositoryUrl.toString()
                 + remoteGroupId + "/"
                    + remoteArtifactId + "/"
                    + remoteVersion), "index.html");

        HashMap<String, Version> fileList =
            new HashMap<String, Version>();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String timestampString = reader.readLine();
        while (timestampString != null) {
            if (timestampString.contains(remoteVersion)
                    && timestampString.contains("A HREF")) {
                int startIndex = timestampString.indexOf(remoteVersion,
                        timestampString.indexOf("A HREF"));

                String lineUrl = timestampString + "";

                // re-align index as the end of the version string
                startIndex = startIndex + remoteVersion.length();
                int endIndex = timestampString.indexOf(">", startIndex);
                timestampString = timestampString.substring(startIndex,
                        endIndex);

                if (timestampString.contains("..")
                        || timestampString.contains("metadata")) {
                    // skip
                } else {
                    timestampString = timestampString.substring(
                            timestampString.indexOf(remoteArtifactId) +
                            remoteArtifactId.length());

                    timestampString = timestampString.substring(0,
                            timestampString.lastIndexOf('-'));

                    timestampString = timestampString.substring(
                            timestampString.lastIndexOf('-') + 1,
                            timestampString.length());

                    // get the associated url
                    int startQuoteIndex = lineUrl.indexOf('"');
                    lineUrl = lineUrl.substring(startQuoteIndex + 1);
                    int endQuoteIndex = lineUrl.indexOf('"');
                    lineUrl = lineUrl.substring(8, endQuoteIndex);

                    Version timestamp = new Version(timestampString);
                    if (fileList.containsKey(timestamp.getVersion())) {
                        Version existingStamp =
                            (Version)fileList.get(timestamp.getVersion());
                        existingStamp.addUrl(lineUrl);
                    } else {
                        timestamp.addUrl(lineUrl);
                        fileList.put(timestamp.getVersion(), timestamp);
                    }
                }
            }
            timestampString = reader.readLine();
        }
        reader.close();
        file.delete();
        return fileList;
    }

    /**
     * Gets the latest version available on the remote repository.
     * @param map Contains information about versions/urls on the
     * remote repository.
     * @return the latest version available on the remote repository.
     */
    private Version getLatestVersion(HashMap map) {
        Version latestTimeStamp = new Version(Integer.MIN_VALUE);
        Set keys = map.keySet();
        Iterator iterator = keys.iterator();
        while (iterator.hasNext()) {
            Version currentTimeStamp =
                (Version)map.get((String)iterator.next());
            if (currentTimeStamp.after(latestTimeStamp)) {
                latestTimeStamp = currentTimeStamp;
            }
        }
        return latestTimeStamp;
    }

    /**
     * Downloads a specified file from the remote repository.
     * @param url URL of the file to be downloaded.
     * @param fileName Name of the file being downloaded.
     * @return a File reference to the downloaded file (on local machine)
     * @throws MalformedURLException If the URL is malformed.
     * @throws IOException If the writing of the local file fails.
     */
    private File downloadFromRemote(URL url, String fileName)
        throws MalformedURLException, IOException {
        InputStream inputStream = null;
        int current;
        FileOutputStream writer;
        getLog().info("---- Downloading " + fileName
                + " from remote repository.");

        Authenticator.setDefault(new MyAuthenticator(remoteUsername,
                remotePassword));
        URLConnection con = url.openConnection();
        con.setAllowUserInteraction(true);
        inputStream = con.getInputStream();

        /*
        URLConnection con = remoteRepositoryJarUrl.openConnection();
        String headerFields = con.getHeaderField("WWW-Authenticate");
        Map headerFieldsMap = con.getHeaderFields();
        con.setRequestProperty("foundation", "nehta, nehta");
        con.connect();*/

        unextractedOutputFolder.mkdirs();
        File outputFile = new File(unextractedOutputFolder + File.separator
                + fileName);
        writer = new FileOutputStream(outputFile);

        while ((current = inputStream.read()) != -1) {
            writer.write(current);
        }
        writer.close();
        inputStream.close();

        getLog().info("---- Finished downloading " + fileName
                + " from remote repository.");
        return outputFile;
    }

    /**
     * Copies files in a certain directory to the local repository.
     * @param localArtifactDirectory
     * @throws MojoFailureException
     */
    private void copyToLocalRepository(File localArtifactDirectory)
                                throws MojoFailureException {
        getLog().info("---- Copying files to local repository.");
        File[] files = unextractedOutputFolder.listFiles();
        for (File oldFile : files) {
           if (!oldFile.isDirectory()) {
                getLog().info("Copying: " + oldFile.getName());
                File newFile = new File(localArtifactDirectory,
                        oldFile.getName());
                filesToExtract.add(newFile);
                try {
                    FileIO.copyFile(oldFile.toString(), newFile.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new MojoFailureException ("Failed to copy downloaded "
                            + "files into local "
                            + "repository. Old file: " + oldFile.toString() +
                            " New file: " + newFile.toString());
                }
            }
        }
        getLog().info("---- Finished copying downloaded files to " +
                "local repository.");
    }

    /**
     * Creates a version object based on a string input.
     * @param string The string to convert to a Version object.
     * @return Version version object created from string input.
     */
    private Version getVersionFromString(String string) {
        string.substring(string.indexOf(remoteArtifactId) +
                remoteArtifactId.length());

        string = string.substring(0,
                string.lastIndexOf('-'));

        string = string.substring(
                string.lastIndexOf('-') + 1,
                string.length());
        Version version;
        try {
            version = new Version(string);
        } catch (NumberFormatException e) {
            version = null;
        }

        return version;
    }

    /**
     * Private class to track the versions available.
     */
    private class Version {

        /**
         * A string representation of a version, in YYYYMMDDHHMMSS form.
         * No non-numeric characters allowed.
         */
        String version;

        /**
         * A list of files which are of this version.
         */
        LinkedList<String> urls = new LinkedList<String>();

        /**
         * Constructor for version class.
         * @param s The string used to create the version.
         * @throws NumberFormatException if a non-numeric string is passed.
         */
        private Version(String s) throws NumberFormatException {
            version = "";
            for (int i = 0; i < s.length(); i++) {
                if (Character.isDigit(s.charAt(i))) {
                    version = version + s.charAt(i);
                }
            }
            if (version == "") {
                throw new NumberFormatException();
            }
        }

        /**
         * Constructor for version class.
         * @param i an integer used to create the version.
         */
        private Version(int i) {
            version = i + "";
        }

        /**
         * Gets the string representation of this version.
         * @return the version string.
         */
        private String getVersion() {
            return version;
        }

        /**
         * Calculates if a version is earlier or later than another version.
         * @param v the version to compare.
         * @return true if this version is later than the specified version.
         * @throws NumberFormatException Thrown if non-numeric versions have
         * been generated.
         */
        private boolean after(Version v)
                throws NumberFormatException {
            if (Double.valueOf(version) >
                        Double.valueOf(v.getVersion())) {
                return true;
            } else {
                return false;
            }

        }

        /**
         * Calculates if another version object is equal to this one.
         * @param v The version to compare.
         * @return true if the versions are equal, false otherwise.
         */
        public boolean equals(Version v) {
            return (version.equals(v.getVersion()));
        }

        /**
         * String to display as a representation of this object.
         */
        public String toString() {
            return version;
        }

        /**
         * Hash code to use when creating a hashcode of this object.
         */
        public int hashCode() {
            return version.hashCode();
        }

        /**
         * Gets a list of urls associated with this version.
         * @return a list of urls associated with this version.
         */
        public LinkedList<String> getUrls() {
            return urls;
        }

        /**
         * Adds a url to the list of urls.
         * @param st the url string to add.
         */
        public void addUrl(String st) {
            urls.add(st);
        }
    }


    /**
     * Private class used to authenticate users when connecting to a remote
     * URL, which requires authentication.
     */
    static class MyAuthenticator extends Authenticator {
        private String username;
        private String password;

        /**
         * Constructor for authenticator class.
         * @param username The username to authenticate with.
         * @param password The password to authenticate with.
         */
        public MyAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        /**
         * Gets the password authentication object.
         */
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password.toCharArray());
        }
      }
}
