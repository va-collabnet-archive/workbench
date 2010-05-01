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
package org.dwfa.maven;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StreamTokenizer;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.dwfa.cement.PrimordialId;
import org.dwfa.util.io.FileIO;

/**
 * Goal which transforms source files and puts them in generated resources.
 * TODO Remove setMavenParameter() method, and replace with individual setters.
 * Problem occured when adding the method setOutputSpecs() - causes maven to
 * ignore the value specified in the pom and override the parameter with null.
 * 
 * @goal transform
 * @phase generate-resources
 */
public class Transform extends AbstractMojo {

    private static final int PROGRESS_LOGGING_SIZE = 10000;
    /**
     * @parameter
     * @required
     */
    private OutputSpec[] outputSpecs;
    /**
     * @parameter
     * @required
     */
    private String idFileLoc;
    /**
     * @parameter
     * 
     */
    private boolean appendIdFiles = false;
    /**
     * @parameter
     */
    private String idEncoding = "UTF-8";
    /**
     * @parameter
     */
    private Character outputColumnDelimiter = '\t';
    /**
     * @parameter
     */
    private Character outputCharacterDelimiter = '"';
    /**
     * List of source roots containing non-test code.
     * 
     * @parameter default-value="${project.compileSourceRoots}"
     * @required
     * @readonly
     */
    private List sourceRoots;
    /**
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
    private File buildDirectory;
    /**
     * Location of the source directory.
     * 
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private File sourceDirectory;

    /**
     * Whether to use the DB mechanism for mapping SCT Id's. This replaces the
     * flat file mechanism, which is prone to memory issues with a large number
     * of Id's. Unless otherwise specified, the DB mechanism will be used
     * 
     * @parameter
     */
    boolean useDbSctMap = Boolean.TRUE;

    /**
     * Namespace for sctid generation
     * 
     * @parameter
     */
    private String namespace;

    private boolean includeHeader = false;
    private Map<UUID, Integer> uuidToNativeMap;
    private Map<Integer, UUID> nativeToUuidMap;
    private Map<String, Map<String, UUID>> sourceToUuidMapMap = new HashMap<String, Map<String, UUID>>();
    private Map<String, Map<UUID, String>> uuidToSourceMapMap = new HashMap<String, Map<UUID, String>>();
    private int nextColumnId = 0;

    public int getNextColumnId() {
        int id = nextColumnId;
        nextColumnId++;
        return id;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        Log logger = getLog();
        logger.info("starting transform: " + Arrays.asList(outputSpecs));

        // calculate the SHA-1 hashcode for this mojo based on input
        Sha1HashCodeGenerator generator;
        String hashCode = "";
        try {
            generator = new Sha1HashCodeGenerator();

            for (int i = 0; i < outputSpecs.length; i++) {
                generator.add(outputSpecs[i]);
            }

            generator.add(idFileLoc);
            generator.add(appendIdFiles);
            generator.add(idEncoding);
            generator.add(outputColumnDelimiter);
            generator.add(outputCharacterDelimiter);

            Iterator iter = sourceRoots.iterator();
            while (iter.hasNext()) {
                generator.add(iter.next());
            }

            hashCode = generator.getHashCode();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }

        File goalFileDirectory = new File(buildDirectory, "completed-mojos");
        File goalFile = new File(goalFileDirectory, hashCode);

        // check to see if this goal has been executed previously
        if (!goalFile.exists()) {

            logger.info("goal has not run before");
            // hasn't been executed previously
            try {
                for (OutputSpec outSpec : outputSpecs) {

                    logger.info("processing " + outSpec);

                    I_TransformAndWrite[] writers = outSpec.getWriters();
                    for (I_TransformAndWrite tw : writers) {
                        File outputFile = new File(tw.getFileName());
                        outputFile.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(outputFile, tw.append());
                        OutputStreamWriter osw = new OutputStreamWriter(fos, tw.getOutputEncoding());
                        BufferedWriter bw = new BufferedWriter(osw);
                        tw.init(bw, this);
                    }
                    if (outSpec.getConstantSpecs() != null) {
                        for (I_ReadAndTransform constantTransform : outSpec.getConstantSpecs()) {
                            constantTransform.setup(this);
                            constantTransform.transform("test");
                            for (I_TransformAndWrite tw : writers) {
                                tw.addTransform(constantTransform);
                            }
                        }
                    }
                    InputFileSpec[] inputSpecs = outSpec.getInputSpecs();
                    for (InputFileSpec spec : inputSpecs) {
                        nextColumnId = 0;
                        Map<Integer, Set<I_ReadAndTransform>> columnTransformerMap = new HashMap<Integer, Set<I_ReadAndTransform>>();
                        logger.info("Now processing file spec:\n\n" + spec);

                        for (I_ReadAndTransform t : spec.getColumnSpecs()) {
                            t.setup(this);
                            Set<I_ReadAndTransform> transformerSet = (Set<I_ReadAndTransform>) columnTransformerMap.get((Integer) t.getColumnId());
                            if (transformerSet == null) {
                                transformerSet = new HashSet<I_ReadAndTransform>();
                                columnTransformerMap.put((Integer) t.getColumnId(), transformerSet);
                            }
                            transformerSet.add(t);

                            for (I_TransformAndWrite tw : writers) {
                                tw.addTransform(t);
                            }
                        }

                        File inputFile = normalize(spec);
                        if (inputFile != null) {
                            if (inputFile.length() == 0) {
                                logger.warn("skipping 0 length file " + inputFile);
                                continue;
                            }
                        } else {
                            throw new MojoFailureException("Spec cannot be normalized. Does the input file exist?");
                        }
                        FileInputStream fs = new FileInputStream(inputFile);
                        InputStreamReader isr = new InputStreamReader(fs, spec.getInputEncoding());
                        BufferedReader br = new BufferedReader(isr);
                        StreamTokenizer st = new StreamTokenizer(br);
                        st.resetSyntax();
                        st.wordChars('\u001F', '\u00FF');
                        st.ordinaryChar(spec.getInputColumnDelimiter());
                        st.eolIsSignificant(true);
                        if (spec.skipFirstLine()) {
                            skipLine(st);
                        }
                        int tokenType = st.nextToken();
                        int rowCount = 0;
                        while (tokenType != StreamTokenizer.TT_EOF) {
                            int currentColumn = 0;
                            while (tokenType != '\r' && tokenType != '\n' && tokenType != StreamTokenizer.TT_EOF) {
                                /*
                                 * if (rowCount >= spec.getDebugRowStart() &&
                                 * rowCount <= spec.getDebugRowEnd()) {
                                 * getLog().info("Transforming column: " +
                                 * currentColumn + " string token: " + st.sval);
                                 * getLog().info("Current row:" + rowCount);
                                 * }
                                 */

                                if (columnTransformerMap.get((Integer) currentColumn) == null) {
                                } else {
                                    for (Object tObj : (Set) columnTransformerMap.get((Integer) currentColumn)) {
                                        I_ReadAndTransform t = (I_ReadAndTransform) tObj;
                                        /*
                                         * if (rowCount >=
                                         * spec.getDebugRowStart() && rowCount
                                         * <= spec.getDebugRowEnd()) {
                                         * 
                                         * getLog().info("Transform for column: "
                                         * + currentColumn + " is: " + t);
                                         * }
                                         */
                                        if (tokenType == spec.getInputColumnDelimiter().charValue()) {
                                            t.transform(null);
                                        } else {
                                            t.transform(st.sval);
                                        }
                                        /*
                                         * if (rowCount >=
                                         * spec.getDebugRowStart() && rowCount
                                         * <= spec.getDebugRowEnd()) {
                                         * getLog().info("Transform: " + t +
                                         * " result: " + result);
                                         * }
                                         */
                                    }
                                }
                                tokenType = st.nextToken();
                                if (spec.getInputColumnDelimiter().charValue() == tokenType) {
                                    // CR or LF
                                    tokenType = st.nextToken();
                                    if (spec.getInputColumnDelimiter().charValue() == tokenType) {
                                        st.pushBack();
                                    }
                                }
                                currentColumn++;
                            }

                            for (I_TransformAndWrite tw : writers) {
                                tw.processRec();
                            }

                            switch (tokenType) {
                            case '\r': // is CR
                                // LF
                                tokenType = st.nextToken();
                                break;
                            case '\n': // LF
                                break;
                            case StreamTokenizer.TT_EOF: // End of file
                                break;
                            default:
                                throw new Exception("There are more columns than transformers. Tokentype: " + tokenType);
                            }
                            rowCount++;
                            if (rowCount % PROGRESS_LOGGING_SIZE == 0) {
                                logger.info("processed " + rowCount + " rows of file " + inputFile.getAbsolutePath());
                            }
                            // Beginning of loop
                            tokenType = st.nextToken();
                        }
                        fs.close();
                        logger.info("Processed: " + rowCount + " rows.");
                    }
                    logger.info("closing writers");
                    int count = 0;
                    for (I_TransformAndWrite tw : writers) {
                        logger.info("closing " + ++count + " of " + writers.length);
                        tw.close();
                    }

                    logger.info("cleanup inputs");
                    count = 0;
                    for (InputFileSpec ifs : inputSpecs) {
                        logger.info("cleaning input spec " + ++count + " of " + inputSpecs.length);
                        int transformCount = 0;
                        I_ReadAndTransform[] columnSpecs = ifs.getColumnSpecs();
                        for (I_ReadAndTransform t : columnSpecs) {
                            logger.info("cleaning column spec " + ++transformCount + " of " + columnSpecs.length);
                            t.cleanup(this);
                        }
                    }

                    logger.info("cleanup inputs - done");
                }

                if (uuidToNativeMap != null) {
                    logger.info("ID map is not null.");
                    // write out id map...
                    File outputFileLoc = new File(idFileLoc);
                    outputFileLoc.getParentFile().mkdirs();

                    File file = new File(outputFileLoc, "uuidToNative.txt");
                    FileOutputStream fos = new FileOutputStream(file, appendIdFiles);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, idEncoding);
                    BufferedWriter bw = new BufferedWriter(osw);
                    if (includeHeader) {
                        bw.append("UUID");
                        bw.append(outputColumnDelimiter);
                        bw.append("NID");
                        bw.append("\n");
                    }
                    int rowcount = 0;
                    for (Iterator i = uuidToNativeMap.entrySet().iterator(); i.hasNext();) {
                        Map.Entry entry = (Entry) i.next();
                        bw.append(entry.getKey().toString());
                        bw.append(outputColumnDelimiter);
                        bw.append(entry.getValue().toString());
                        bw.append("\n");
                        rowcount++;
                        if (rowcount++ % PROGRESS_LOGGING_SIZE == 0) {
                            logger.info("processed " + rowcount + " rows of file " + file.getAbsolutePath());
                        }
                    }

                    bw.close();
                }

                logger.info("writing out the source to uuid map");
                for (Iterator keyItr = sourceToUuidMapMap.keySet().iterator(); keyItr.hasNext();) {
                    String key = (String) keyItr.next();

                    File outputFileLoc = new File(idFileLoc);
                    outputFileLoc.getParentFile().mkdirs();

                    File file = new File(outputFileLoc, key + "ToUuid.txt");
                    FileOutputStream fos = new FileOutputStream(file, appendIdFiles);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, idEncoding);
                    BufferedWriter bw = new BufferedWriter(osw);
                    if (includeHeader) {
                        bw.append(key.toUpperCase());
                        bw.append(outputColumnDelimiter);
                        bw.append("UUID");
                        bw.append("\n");
                    }

                    Map idMap = (Map) sourceToUuidMapMap.get(key);
                    int rowcount = 0;
                    for (Iterator i = idMap.entrySet().iterator(); i.hasNext();) {
                        Map.Entry entry = (Entry) i.next();
                        bw.append(entry.getKey().toString());
                        bw.append(outputColumnDelimiter);
                        bw.append(entry.getValue().toString());
                        bw.append("\n");
                        rowcount++;
                        if (rowcount++ % PROGRESS_LOGGING_SIZE == 0) {
                            logger.info("processed " + rowcount + " rows of file " + file.getAbsolutePath());
                        }
                    }
                    bw.close();
                }

                // create a new file to indicate this execution has completed
                try {
                    goalFileDirectory.mkdirs();
                    goalFile.createNewFile();

                    MojoUtil.writeClassToMojoHashFile(goalFile, this.getClass());

                } catch (IOException e) {
                    e.printStackTrace();
                }

                logger.info("execution complete");

            } catch (FileNotFoundException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            } catch (UnsupportedEncodingException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        } else {
            logger.info("Skipping goal - executed previously.");
        }
    }

    private File normalize(InputFileSpec spec) {
        String s = spec.getInputFile();
        File f = FileIO.normalizeFileStr(s);
        return f;
    }

    public Map<UUID, Integer> getUuidToNativeMap() {
        if (uuidToNativeMap == null) {
            setupUuidMaps();
        }
        return uuidToNativeMap;
    }

    private void setupUuidMaps() {
        uuidToNativeMap = new HashMap<UUID, Integer>();
        nativeToUuidMap = new HashMap<Integer, UUID>();
        for (PrimordialId pid : PrimordialId.values()) {
            for (UUID uid : pid.getUids()) {
                uuidToNativeMap.put(uid, pid.getNativeId(Integer.MIN_VALUE));
                nativeToUuidMap.put(pid.getNativeId(Integer.MIN_VALUE), uid);
            }
        }
    }

    public Map<Integer, UUID> getNativeToUuidMap() {
        if (nativeToUuidMap == null) {
            setupUuidMaps();
        }
        return nativeToUuidMap;
    }

    public Map<String, UUID> getSourceToUuidMap(String source) {
        if (sourceToUuidMapMap.get(source) == null) {
            sourceToUuidMapMap.put(source, new HashMap<String, UUID>());
        }
        return sourceToUuidMapMap.get(source);
    }

    public Map<UUID, String> getUuidToSourceMap(String source) {
        if (uuidToSourceMapMap.get(source) == null) {
            uuidToSourceMapMap.put(source, new HashMap<UUID, String>());
        }
        return uuidToSourceMapMap.get(source);
    }

    private void skipLine(StreamTokenizer st) throws IOException {
        int tokenType = st.nextToken();
        while (tokenType != StreamTokenizer.TT_EOL) {
            tokenType = st.nextToken();
        }
    }

    public int uuidToNid(Object source) throws Exception {
        if (nativeToUuidMap == null) {
            setupUuidMaps();
        }
        if (Collection.class.isAssignableFrom(source.getClass())) {
            Collection c = (Collection) source;
            source = c.iterator().next();
        }
        UUID sourceUuid = (UUID) source;
        Integer nativeId = (Integer) uuidToNativeMap.get(sourceUuid);
        if (nativeId == null) {
            nativeId = Integer.MIN_VALUE + nativeToUuidMap.size();
            uuidToNativeMap.put(sourceUuid, nativeId);
            nativeToUuidMap.put(nativeId, sourceUuid);
        }
        return nativeId;
    }

    public List getSourceRoots() {
        return sourceRoots;
    }

    public void setMavenParameters(OutputSpec[] outputSpecs, String idFileLoc, boolean appendIdFiles,
            String idEncoding, Character outputColumnDelimiter, Character outputCharacterDelimiter, List sourceRoots) {
        this.outputSpecs = outputSpecs;
        this.idFileLoc = idFileLoc;
        this.appendIdFiles = appendIdFiles;
        this.idEncoding = idEncoding;
        this.outputCharacterDelimiter = outputCharacterDelimiter;
        this.outputColumnDelimiter = outputColumnDelimiter;
        this.sourceRoots = sourceRoots;
    }

    public void setMavenParameters(OutputSpec[] outputSpecs, String idFileLoc, boolean appendIdFiles,
            String idEncoding, Character outputColumnDelimiter, Character outputCharacterDelimiter, List sourceRoots,
            File buildDirectory) {
        this.setMavenParameters(outputSpecs, idFileLoc, appendIdFiles, idEncoding, outputColumnDelimiter,
            outputCharacterDelimiter, sourceRoots);
        this.buildDirectory = buildDirectory;
    }

    public File getBuildDirectory() {
        return buildDirectory;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public boolean getUseDbSctMap() {
        return useDbSctMap;
    }

    /**
     * @return the namespace
     */
    public final String getNamespace() {
        return namespace;
    }
}
