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
package org.dwfa.mojo.epicexport.kp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.mojo.epicexport.I_RefsetUsageInterpreter;
import org.dwfa.tapi.TerminologyException;

public class RegionalHibernationBuilder {
	// Order of regions is important, used to determine an integer identifier for Epic hibernation
	public static final String[] ALL_REGIONS = {"NCAL", "SCAL", "CO", "GA", "HI", "MAS", "NW", "OH"};
	public static final String NATIONAL_REGION = "National";
	
	private String outputDirectory = null;
	private HashSet<I_Position> positions;
	private I_IntSet statusValues;
	private List<MasterFileHibernater> masterfiles; 
	private I_RefsetUsageInterpreter interpreter;

	public RegionalHibernationBuilder(String dir, HashSet<I_Position> positions, I_IntSet statusValues) {
		this.outputDirectory = dir;
		this.positions = positions;
		this.statusValues = statusValues;
		this.interpreter = new RefsetUsageInterpreter();
		this.masterfiles = new ArrayList<MasterFileHibernater>();
	}
	
	public void buildAnyHibernationForConcept(I_GetConceptData concept, String cid) throws IOException, TerminologyException {
		List<? extends I_ThinExtByRefVersioned> extensions =
			Terms.get().getAllExtensionsForComponent(concept.getConceptId());
				
    	for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
    		// getLog().info("Processing extension: " + thinExtByRefVersioned );
        	if (Terms.get().hasConcept(thinExtByRefVersioned.getRefsetId())) {
                for (I_ThinExtByRefTuple thinExtByRefTuple : thinExtByRefVersioned.getTuples(statusValues,
	                    positions, false, false)) {
	                	// export(thinExtByRefTuple, concept);
                		I_GetConceptData refsetConcept = Terms.get().
                			getConcept(thinExtByRefTuple.getRefsetId());
                		 getOwningRegionsFor(refsetConcept, cid);

	                }
    		
        	}
    	}
    	for (MasterFileHibernater m: this.masterfiles) {
    		// System.out.print("Regions not using: " + m.getRegionsNotUsing());
    		m.writeHibernation(this.outputDirectory, cid);
    		m.clear();
    	}

	}
	
	public void getOwningRegionsFor(I_GetConceptData refsetConcept, String cid) throws IOException {
		String refsetName = refsetConcept.getInitialText();
    	List<I_RefsetUsageInterpreter.I_RefsetApplication> applications = 
    		this.interpreter.getApplications(refsetName);
    	for (I_RefsetUsageInterpreter.I_RefsetApplication app: applications) {
    		if(app.getItemNumber().equals("2")) {
    			MasterFileHibernater m = getMasterfile(app.getMasterfile());
    			m.addRegion(app.getRegion());
    		}
    	}
	}
	
	private MasterFileHibernater getMasterfile(String masterfile) {
		MasterFileHibernater ret = null;
		boolean found = false;
		for (MasterFileHibernater m: this.masterfiles) {
			if (m.getMasterFile().equals(masterfile)) {
				ret = m;
				found = true;
				break;
			}
		}
		if (!found) {
			ret = new MasterFileHibernater(masterfile);
			this.masterfiles.add(ret);
		}
		return ret;
	}
	
	public void close() throws IOException {
    	for (MasterFileHibernater m: this.masterfiles) {
    		m.close();
    	}

	}
	private class MasterFileHibernater {
		private String masterFile;
		private BufferedWriter bw;
		private String outputDir;
		private String filename;
		private List<String> regions = new ArrayList<String>();
		private int cidsHibernated = 0;
		private int regionsHibernated = 0;

		public MasterFileHibernater(String masterfile) {
			this.masterFile = masterfile;
		}
		
		public String getMasterFile() {
			return masterFile;
		}

		@SuppressWarnings("unused")
		public void setMasterFile(String masterFile) {
			this.masterFile = masterFile;
		}
		
		public void addRegion(String region) {
			if (region.equals("CAL")) {
				this.regions.add("NCAL");
				this.regions.add("SCAL");
			}
			else
				this.regions.add(region);
		}
		
		public void setOutputDir(String outputDir) {
			this.outputDir = outputDir;
		}

		public void clear() {
			this.regions = new ArrayList<String>();
		}
		
		public List<String> getRegionsNotUsing() {
			List<String> ret = new ArrayList<String>();
			for (String m: RegionalHibernationBuilder.ALL_REGIONS) {
				boolean found = false;
				for (String t : this.regions) {
					found = found || t.equals(m) || t.equals(RegionalHibernationBuilder.NATIONAL_REGION);
				}
				if (!found)
					ret.add(m);
			}
			return ret;
		}
		
		public void writeHibernation(String dir, String cid) throws IOException {
			this.setOutputDir(dir);
			List<String> regions = this.getRegionsNotUsing();
			if (regions.size() > 0)
				++this.cidsHibernated;
			for (String r: regions) {
				StringBuffer s = new StringBuffer();
				s.append(this.getMasterFile().substring(0, 3).toUpperCase());
				s.append("^CID.");
				s.append(cid);
				s.append('^');
				s.append(getRegionNumber(r));
				s.append("^3^Hibernated in ");
				s.append(r);
				writeLine(s.toString());
				this.regionsHibernated++;
			}
		}
		
		public void writeLine(String str) throws IOException {
			if (this.bw == null) {
				if (!this.outputDir.endsWith("/"))
					this.outputDir = this.outputDir.concat("/");
				File d = new File(outputDir);
				d.mkdirs();
				this.filename = this.getMasterFile().concat("_hibernate.txt");
				File f = new File(outputDir, this.filename);
				bw = new BufferedWriter(new FileWriter(f));
			}
			bw.write(str);
			bw.write("\r\n");
		}
	
		public void close() throws IOException {
			if (this.bw != null) {
				bw.close();
				AceLog.getAppLog().info("Wrote " + this.regionsHibernated + 
						" hibernations for " + this.cidsHibernated + " cid's in file " + this.filename);
			}
		}
		
		public int getRegionNumber(String region) {
			int i = 0;
			for (String s: RegionalHibernationBuilder.ALL_REGIONS) {
				i++;
				if (s.equals(region))
					return i;
			}
			return 0;
		}
	}

}
