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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.PrimordialId;
import org.dwfa.util.io.FileIO;

/**
 * Goal which transforms source files and puts them in generated resources.
 * 
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
	
	private boolean includeHeader = false;


	private Map uuidToNativeMap;

	private Map nativeToUuidMap;
	
	private Map sourceToUuidMapMap = new HashMap();
	private Map uuidToSourceMapMap = new HashMap();

	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println("sourceRoots: " + sourceRoots);
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
				for (InputFileSpec spec : outSpec.getInputSpecs()) {
					getLog().info("Now processing file spec:\n\n" + spec);
					for (I_ReadAndTransform t : spec.getColumnSpecs()) {
						t.setup(this);
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
						for (I_ReadAndTransform t : spec.getColumnSpecs()) {
							switch (tokenType) {
							case '\r': // is CR
								throw new Exception("There are more transformers than columns. ('\\r' encountered)");
							case '\n':  //LF
								throw new Exception("There are more transformers than columns. ('\\n' encountered)");
							default:
							}
							String result = t.transform(st.sval);
							if (rowCount >= spec.getDebugRowStart() && rowCount <= spec.getDebugRowEnd()) {
								getLog().info("Transform: " + t + " result: " + result);
							}
							// CR or LF
							tokenType = st.nextToken();
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
			
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private File normalize(InputFileSpec spec) {
		String s = spec.getInputFile();
		File f = FileIO.normalizeFileStr(s);
		return f;
	}

	

	public Map getUuidToNativeMap() {
		if (uuidToNativeMap == null) {
			setupUuidMaps();
		}
		return uuidToNativeMap;
	}

	private void setupUuidMaps() {
		uuidToNativeMap = new HashMap();
		nativeToUuidMap = new HashMap();
		for (PrimordialId pid: PrimordialId.values()) {
			for (UUID uid: pid.getUids()) {
				uuidToNativeMap.put(uid, pid.getNativeId(Integer.MIN_VALUE));
				nativeToUuidMap.put(pid.getNativeId(Integer.MIN_VALUE), uid);
			}
		}
	}

	public Map getNativeToUuidMap() {
		if (nativeToUuidMap == null) {
			setupUuidMaps();
		}
		return nativeToUuidMap;
	}

	public Map getSourceToUuidMap(String source) {
		if (sourceToUuidMapMap.get(source) == null) {
			sourceToUuidMapMap.put(source, new HashMap());
		}
		return (Map) sourceToUuidMapMap.get(source);
	}
	public Map getUuidToSourceMap(String source) {
		if (uuidToSourceMapMap.get(source) == null) {
			uuidToSourceMapMap.put(source, new HashMap());
		}
		return (Map) uuidToSourceMapMap.get(source);
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

}
