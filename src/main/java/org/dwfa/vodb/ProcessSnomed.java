package org.dwfa.vodb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.dwfa.ace.api.I_IntSet;
import org.dwfa.vodb.ProcessConstants.FORMAT;
import org.dwfa.vodb.types.I_ProcessRelationshipEntries;
import org.dwfa.vodb.types.IntSet;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

public abstract class ProcessSnomed extends ProcessSources {
	int constantDate;
	public ProcessSnomed(int constantDate) throws DatabaseException {
		super(true);
		this.constantDate = constantDate;
	}

	static class MakeRelSet implements I_ProcessRelationshipEntries {
		List<Integer> ids = new ArrayList<Integer>();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);
		
		public MakeRelSet() {
			super();
		}

		public void processRel(DatabaseEntry key, DatabaseEntry value) throws Exception {
			ids.add((Integer) intBinder.entryToObject(key));
			
		}

		public DatabaseEntry getDataEntry() {
			return key;
		}

		public DatabaseEntry getKeyEntry() {
			return value;
		}
		
		public I_IntSet getIntSet() {
			int[] values = new int[ids.size()];
			int index = 0;
			for (Integer id: ids) {
				values[index++] = id;
			}
			Arrays.sort(values);
			return new IntSet(values);
		}
	}

	public abstract void iterateRelationships(MakeRelSet oldRelItr) throws Exception;
	public void execute(File snomedDir) throws Exception {
			MakeRelSet oldRelSet = new MakeRelSet();
			iterateRelationships(oldRelSet);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
			
			for (File releaseDir : snomedDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith(".") == false;
				}})) {
				getLog().info(releaseDir.getName());
				Date releaseDate = dateFormat.parse(releaseDir.getName());
				addReleaseDate(releaseDate);
				for (File contentFile : releaseDir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.startsWith("sct");
					}})) {
					getLog().info(contentFile.getName());
					FileInputStream fs = new FileInputStream(contentFile);
					InputStreamReader  isr = new InputStreamReader(fs, Charset.forName("UTF-8"));
					
					BufferedReader br = new BufferedReader(isr);
					if (contentFile.getName().startsWith("sct_concepts_")) {
						readConcepts(br, releaseDate, FORMAT.SNOMED);
					} else if (contentFile.getName().startsWith(
							"sct_descriptions_")) {
						readDescriptions(br, releaseDate, FORMAT.SNOMED);
					} else if (contentFile.getName().startsWith(
							"sct_relationships_")) {
						readRelationships(br, releaseDate, FORMAT.SNOMED);
					}
					br.close();
				}
			}
			
			cleanup(oldRelSet.getIntSet());
	}
	boolean processConcepts = true;
	boolean processRels = true;
	boolean processDescriptions = true;
	
	public void execute(JarFile snomedJar) throws Exception {
		MakeRelSet oldRelSet = new MakeRelSet();
		iterateRelationships(oldRelSet);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Enumeration<JarEntry> jarEnum = snomedJar.entries();
		while (jarEnum.hasMoreElements()) {
			JarEntry je = jarEnum.nextElement();
			if (je.getName().startsWith("org/snomed/") &&
					je.getName().endsWith(".txt")) {
				int startIndex = "org/snomed/".length();
				int endIndex = startIndex + "yyyy-MM-dd".length();
				Date releaseDate = dateFormat.parse(je.getName().substring(startIndex, endIndex));
				addReleaseDate(releaseDate);
				getLog().info(" processing: " + je.getName());
				if (processConcepts && je.getName().contains("concepts")) {
					InputStreamReader isr = new InputStreamReader(
							snomedJar.getInputStream(je), Charset.forName("UTF-8"));
					readConcepts(isr, releaseDate, FORMAT.SNOMED);
					isr.close();
				} else if (processDescriptions && je.getName().contains("descriptions")) {
					InputStream is = snomedJar.getInputStream(je);
					InputStreamReader isr = new InputStreamReader(
							is, Charset.forName("UTF-8"));
					readDescriptions(new InputStreamReader(
							snomedJar.getInputStream(je)), releaseDate, FORMAT.SNOMED);
					isr.close();
				} else if (processRels && je.getName().contains("relationships")) {
					InputStreamReader isr = new InputStreamReader(
							snomedJar.getInputStream(je), Charset.forName("UTF-8"));
					readRelationships(new InputStreamReader(
							snomedJar.getInputStream(je)), releaseDate, FORMAT.SNOMED);
					isr.close();
				}
			}
		}
		cleanup(oldRelSet.getIntSet());
	}

	protected Object getId(StreamTokenizer st) {
		return new Long(st.sval);
	}
	protected Object getDescType(StreamTokenizer st) {
		return new Integer(st.sval);
	}
	protected Object getStatus(StreamTokenizer st) {
		return new Integer(st.sval);
	}
	protected Object getRefinability(StreamTokenizer st) {
		return new Integer(st.sval);
	}
	protected Object getCharacteristic(StreamTokenizer st) {
		return new Integer(st.sval);
	}
	protected boolean parseBoolean(StreamTokenizer st) {
		return Integer.parseInt(st.sval) == 1;
	}

}
