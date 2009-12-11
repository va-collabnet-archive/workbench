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
package org.dwfa.vodb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.sleepycat.je.DatabaseException;

public abstract class ProcessConstants extends ProcessSources {

	public ProcessConstants() throws DatabaseException {
		super(false);
	}

	public void execute(File constantDir) throws Exception {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		for (File releaseDir : constantDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(".") == false;
			}
		})) {
			getLog().info("Release directory: " + releaseDir.getName());
			Date releaseDate = dateFormat.parse(releaseDir.getName());

			addReleaseDate(releaseDate);
			for (File contentFile : releaseDir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".txt");
				}
			})) {
				getLog().info("Content file: " + contentFile.getName());
				FileReader fr;
				fr = new FileReader(contentFile);
				BufferedReader br = new BufferedReader(fr);
				if (contentFile.getName().startsWith("concepts")) {
					readConcepts(br, releaseDate, FORMAT.SNOMED);
				} else if (contentFile.getName().startsWith("descriptions")) {
					readDescriptions(br, releaseDate, FORMAT.SNOMED);
				} else if (contentFile.getName().startsWith("relationships")) {
					readRelationships(br, releaseDate, FORMAT.SNOMED);
				} else if (contentFile.getName().startsWith("illicit_words")) {
					getLog().info(
							"Found illicit_words file: "
									+ contentFile.getName());
					readIllicitWords(br);
				} else if (contentFile.getName().startsWith("licit_words")) {
					getLog().info(
							"Found licit_words file: " + contentFile.getName());
					readLicitWords(br);
				} else {
					getLog().info(
							"Don't know what to do with file: "
									+ contentFile.getName());
				}
				br.close();
			}
		}
		cleanup(null);
	}

	public void execute(JarFile constantJar) throws Exception {
		execute(constantJar, "org/jehri/cement/", FORMAT.SNOMED);
	}

	public static enum FORMAT {
		SNOMED, ACE
	};

   public void executeFromDir(File dataDir) throws Exception {
      FORMAT format = FORMAT.ACE;
      File[] dataFiles = dataDir.listFiles(new FileFilter() {
         public boolean accept(File f) {
            return f.getName().endsWith(".txt");
         }});
      for (File dataFile: dataFiles) {
            getLog().info(dataFile.getName());
            if (dataFile.getName().contains("concepts")) {
               Reader isr = new BufferedReader(new FileReader(dataFile));
               readConcepts(isr, null, format);
               isr.close();
            } else if (dataFile.getName().contains("descriptions")) {
               Reader isr = new BufferedReader(new FileReader(dataFile));
               readDescriptions(isr, null, format);
               isr.close();
            } else if (dataFile.getName().contains("relationships")) {
               Reader isr = new BufferedReader(new FileReader(dataFile));
               readRelationships(isr, null, format);
               isr.close();
            } else if (dataFile.getName().contains("illicit_words")) {
               getLog().info(
                     "Found illicit_words jar entry: " + dataFile.getName());
               Reader isr = new BufferedReader(new FileReader(dataFile));
               readIllicitWords(isr);
            } else if (dataFile.getName().contains("licit_words")) {
               getLog().info(
                     "Found licit_words jar entry: " + dataFile.getName());
               Reader isr = new BufferedReader(new FileReader(dataFile));
               readLicitWords(isr);
            } else {
               getLog().info(
                     "Don't know what to do with file: "
                           + dataFile.getName());
            }
      }
      cleanup(null);
   }

	public void execute(JarFile constantJar, String dataDir, FORMAT format)
			throws Exception {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Enumeration<JarEntry> jarEnum = constantJar.entries();
		while (jarEnum.hasMoreElements()) {
			JarEntry je = jarEnum.nextElement();
			if (je.getName().startsWith(dataDir)
					&& je.getName().endsWith(".txt")) {
				int startIndex = dataDir.length();
				int endIndex = startIndex + "yyyy-MM-dd".length();
				Date releaseDate = dateFormat.parse(je.getName().substring(
						startIndex, endIndex));
				addReleaseDate(releaseDate);
				getLog().info(je.getName());
				if (je.getName().contains("concepts")) {
					InputStreamReader isr = new InputStreamReader(constantJar
							.getInputStream(je));
					readConcepts(isr, releaseDate, format);
					isr.close();
				} else if (je.getName().contains("descriptions")) {
					InputStreamReader isr = new InputStreamReader(constantJar
							.getInputStream(je));
					readDescriptions(isr, releaseDate, format);
					isr.close();
				} else if (je.getName().contains("relationships")) {
					InputStreamReader isr = new InputStreamReader(constantJar
							.getInputStream(je));
					readRelationships(isr, releaseDate, format);
					isr.close();
				} else if (je.getName().contains("illicit_words")) {
					getLog().info(
							"Found illicit_words jar entry: " + je.getName());
					InputStreamReader isr = new InputStreamReader(constantJar
							.getInputStream(je));
					readIllicitWords(isr);
				} else if (je.getName().contains("licit_words")) {
					getLog().info(
							"Found licit_words jar entry: " + je.getName());
					InputStreamReader isr = new InputStreamReader(constantJar
							.getInputStream(je));
					readLicitWords(isr);
				} else {
					getLog().info(
							"Don't know what to do with jar entry: "
									+ je.getName());
				}
			}
			cleanup(null);
		}
	}

	protected Object getId(StreamTokenizer st) {
		return UUID.fromString(st.sval);
	}

	protected Object getDescType(StreamTokenizer st) {
		return UUID.fromString(st.sval);
	}

	protected Object getStatus(StreamTokenizer st) {
		return UUID.fromString(st.sval);
	}

	protected Object getRefinability(StreamTokenizer st) {
		return UUID.fromString(st.sval);
	}

	protected Object getCharacteristic(StreamTokenizer st) {
		return UUID.fromString(st.sval);
	}

}
