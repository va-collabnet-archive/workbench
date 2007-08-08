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
import org.dwfa.cement.PrimordialId;
import org.dwfa.util.io.FileIO;

/**
 * Goal which transforms source files and puts them in generated resources.
 * TODO Remove setMavenParameter() method, and replace with individual setters.
 * Problem occured when adding the method setOutputSpecs() - causes maven to
 * ignore the value specified in the pom and override the parameter with null.
 * @goal transform
 * @phase generate-resources
 */
public class Transform extends AbstractMojo {


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
    	getLog().info("starting transform: " + Arrays.asList(outputSpecs));
    	
        // calculate the SHA-1 hashcode for this mojo based on input
        Sha1HashCodeGenerator generator;
        String hashCode = "";
        try {
            generator = new Sha1HashCodeGenerator();

            for(int i = 0; i < outputSpecs.length; i++) {
                generator.add(outputSpecs[i]);
            }

            generator.add(idFileLoc);
            generator.add(appendIdFiles);
            generator.add(idEncoding);
            generator.add(outputColumnDelimiter);
            generator.add(outputCharacterDelimiter);

            Iterator iter = sourceRoots.iterator();
            while(iter.hasNext()) {
                generator.add(iter.next());
            }

            hashCode = generator.getHashCode();
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }

        File goalFileDirectory = new File("target" + File.separator
                + "completed-mojos");
        File goalFile = new File(goalFileDirectory, hashCode);

        // check to see if this goal has been executed previously
        if(!goalFile.exists()) {
            // hasn't been executed previously
            try {
                for (OutputSpec outSpec: outputSpecs) {
                    for (I_TransformAndWrite tw: outSpec.getWriters()) {
                        File outputFile = new File(tw.getFileName());
                        outputFile.getParentFile().mkdirs();
                        FileOutputStream fos = new FileOutputStream(outputFile, tw.append());
                        OutputStreamWriter osw = new OutputStreamWriter(fos, tw.getOutputEncoding());
                        BufferedWriter bw = new BufferedWriter(osw);
                        tw.init(bw, this);
                    }
                    if(outSpec.getConstantSpecs() != null) {
                        for (I_ReadAndTransform constantTransform: outSpec.getConstantSpecs()) {
                            constantTransform.setup(this);
                            constantTransform.transform("test");
                            for (I_TransformAndWrite tw: outSpec.getWriters()) {
                                tw.addTransform(constantTransform);
                            }
                        }
                    }
                    for (InputFileSpec spec : outSpec.getInputSpecs()) {
                        nextColumnId = 0;
                        Map<Integer, Set<I_ReadAndTransform>> columnTransformerMap = new HashMap<Integer, Set<I_ReadAndTransform>>();
                        getLog().info("Now processing file spec:\n\n" + spec);

                        for (I_ReadAndTransform t : spec.getColumnSpecs()) {
                            t.setup(this);
                            Set<I_ReadAndTransform> transformerSet = (Set<I_ReadAndTransform>) columnTransformerMap.get((Integer) t.getColumnId());
                            if (transformerSet == null) {
                                transformerSet = new HashSet<I_ReadAndTransform>();
                                columnTransformerMap.put((Integer) t.getColumnId(), transformerSet);
                            }
                            transformerSet.add(t);

                            for (I_TransformAndWrite tw: outSpec.getWriters()) {
                                tw.addTransform(t);
                            }
                        }
                        File inputFile = normalize(spec);
                        FileInputStream fs = new FileInputStream(inputFile);
                        InputStreamReader isr = new InputStreamReader(fs, spec
                                .getInputEncoding());
                        BufferedReader br = new BufferedReader(isr);
                        StreamTokenizer st = new StreamTokenizer(br);
                        st.resetSyntax();
                        st.wordChars('\u001F', '\u00FF');
                        st.whitespaceChars(spec.getInputColumnDelimiter(), spec
                                .getInputColumnDelimiter());
                        st.eolIsSignificant(true);
                        if (spec.skipFirstLine()) {
                            skipLine(st);
                        }
                        int tokenType = st.nextToken();
                        int rowCount = 0;
                        while (tokenType != StreamTokenizer.TT_EOF) {
                            int currentColumn = 0;
                            while (tokenType != '\r' && tokenType != '\n' && tokenType != StreamTokenizer.TT_EOF) {
                                /*if (rowCount >= spec.getDebugRowStart() && rowCount <= spec.getDebugRowEnd()) {
                                    getLog().info("Transforming column: " + currentColumn + " string token: " + st.sval);
                                    getLog().info("Current row:" + rowCount);
                                }*/

                                if (columnTransformerMap.get((Integer)
                                        currentColumn) == null) {
                                } else {
                                    for (Object tObj: (Set)
                                            columnTransformerMap.get((Integer)
                                            currentColumn)) {
                                        I_ReadAndTransform t = (I_ReadAndTransform) tObj;
                                        /*if (rowCount >= spec.getDebugRowStart() && rowCount <= spec.getDebugRowEnd()) {
                                            getLog().info("Transform for column: " + currentColumn + " is: " + t);
                                        }*/
                                        @SuppressWarnings("unused")
                                       String result = t.transform(st.sval);
                                        /*if (rowCount >= spec.getDebugRowStart() && rowCount <= spec.getDebugRowEnd()) {
                                            getLog().info("Transform: " + t + " result: " + result);
                                        }*/
                                    }
                                }
                                // CR or LF
                                tokenType = st.nextToken();
                                currentColumn++;
                            }


                            for (I_TransformAndWrite tw: outSpec.getWriters()) {
                                tw.processRec();
                            }


                            switch (tokenType) {
                                case '\r': // is CR
                                    // LF
                                    tokenType = st.nextToken();
                                    break;
                                case '\n':  //LF
                                    break;
                                case StreamTokenizer.TT_EOF: // End of file
                                    break;
                                default:
                                    throw new Exception("There are more columns than transformers. Tokentype: " + tokenType);
                            }
                            rowCount++;
                            // Beginning of loop
                            tokenType = st.nextToken();
                        }
                        fs.close();
                        getLog().info("Processed: " + rowCount + " rows.");
                    }
                    for (I_TransformAndWrite tw: outSpec.getWriters()) {
                        tw.close();
                    }
                    for (InputFileSpec ifs: outSpec.getInputSpecs()) {
                       for (I_ReadAndTransform t : ifs.getColumnSpecs()) {
                          t.cleanup(this);
                      }
                    }
                }



                if (uuidToNativeMap != null) {
                    getLog().info("ID map is not null.");
                    // write out id map...
                    File outputFileLoc = new File(idFileLoc);
                    outputFileLoc.getParentFile().mkdirs();

                    FileOutputStream fos = new FileOutputStream(new File(outputFileLoc, "uuidToNative.txt"), appendIdFiles);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, idEncoding);
                    BufferedWriter bw = new BufferedWriter(osw);
                    if (includeHeader) {
                        bw.append("UUID");
                        bw.append(outputColumnDelimiter);
                        bw.append("NID");
                        bw.append("\n");
                    }
                    for (Iterator i = uuidToNativeMap.entrySet().iterator(); i.hasNext();) {
                        Map.Entry entry = (Entry) i.next();
                        bw.append(entry.getKey().toString());
                        bw.append(outputColumnDelimiter);
                        bw.append(entry.getValue().toString());
                        bw.append("\n");
                    }

                    bw.close();
                }


                for (Iterator keyItr = sourceToUuidMapMap.keySet().iterator(); keyItr.hasNext();) {
                    String key = (String) keyItr.next();

                    File outputFileLoc = new File(idFileLoc);
                    outputFileLoc.getParentFile().mkdirs();

                    FileOutputStream fos = new FileOutputStream(new File(outputFileLoc, key + "ToUuid.txt"), appendIdFiles);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, idEncoding);
                    BufferedWriter bw = new BufferedWriter(osw);
                    if (includeHeader) {
                        bw.append(key.toUpperCase());
                        bw.append(outputColumnDelimiter);
                        bw.append("UUID");
                        bw.append("\n");
                    }

                    Map idMap = (Map) sourceToUuidMapMap.get(key);
                    for (Iterator i = idMap.entrySet().iterator(); i.hasNext();) {
                        Map.Entry entry = (Entry) i.next();
                        bw.append(entry.getKey().toString());
                        bw.append(outputColumnDelimiter);
                        bw.append(entry.getValue().toString());
                        bw.append("\n");
                    }
                    bw.close();
                }

                // create a new file to indicate this execution has completed
                try {
                    goalFileDirectory.mkdirs();
                    goalFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
            getLog().info("Skipping goal - executed previously.");
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
        for (PrimordialId pid: PrimordialId.values()) {
            for (UUID uid: pid.getUids()) {
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

    public void setMavenParameters(OutputSpec[] outputSpecs,
            String idFileLoc, boolean appendIdFiles,
            String idEncoding, Character outputColumnDelimiter,
            Character outputCharacterDelimiter,
            List sourceRoots) {
        this.outputSpecs = outputSpecs;
        this.idFileLoc = idFileLoc;
        this.appendIdFiles = appendIdFiles;
        this.idEncoding = idEncoding;
        this.outputCharacterDelimiter = outputCharacterDelimiter;
        this.outputColumnDelimiter = outputColumnDelimiter;
        this.sourceRoots = sourceRoots;
    }

   public File getBuildDirectory() {
      return buildDirectory;
   }

   public File getSourceDirectory() {
      return sourceDirectory;
   }

}
