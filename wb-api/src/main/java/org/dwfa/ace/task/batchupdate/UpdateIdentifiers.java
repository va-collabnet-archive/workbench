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
package org.dwfa.ace.task.batchupdate;

import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;

@BeanList(specs = { @Spec(directory = "tasks/ide/file", type = BeanType.TASK_BEAN) })
public class UpdateIdentifiers extends AbstractTask {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int dataVersion = 1;

	private String inputFilePropName = ProcessAttachmentKeys.PROCESS_FILENAME.getAttachmentKey();

	private int hostIndex = 3;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeInt(hostIndex);
		out.writeObject(inputFilePropName);
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == dataVersion) {
			hostIndex = in.readInt();
			inputFilePropName = (String) in.readObject();
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}

	}

	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do...

	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		int lines = 0;
		int loaded = 0;
		int skipped = 0;
		I_ConfigAceFrame config = null;
		HashSet<I_ShowActivity> activities = new HashSet<I_ShowActivity>();
		I_ShowActivity activity = null;
		long startTime = 0L;
		DataInputStream in = null;
		Map<UUID, String> sctidsMap = new HashMap<UUID, String>();
		Map<String, UUID> uuidsMap = new HashMap<String, UUID>();
		Map<UUID, String> snomedIdsMap = new HashMap<UUID, String>();
		Map<UUID, String> ctv3IdsMap = new HashMap<UUID, String>();

		// load identifiers
		try {
			//String filename = File.separator+"txt";
			//JFileChooser fc = new JFileChooser(new File(filename));
			//fc.showOpenDialog(null);
			//File inputFile = fc.getSelectedFile();
			String filename = (String) process.getProperty(inputFilePropName);
			File inputFile = new File(filename);
			I_TermFactory tf = Terms.get();
			config = tf.getActiveAceFrameConfig();

			I_HelpRefsets refsetHelper = tf.getRefsetHelper(config);
			JList conceptList = config.getBatchConceptList();
			I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
			model.clear();

			if (!DwfaEnv.isHeadless()) {
				activity = Terms.get().newActivityPanel(true, config, 
						"<html>Reading identifiers from: " + filename, true);
				activities.add(activity);
				activity.setValue(0);
				activity.setIndeterminate(true);
				activity.setProgressInfoLower("Loading data...");
				Terms.get().getActiveAceFrameConfig().setStatusMessage("Loading data...");
			}
			startTime = System.currentTimeMillis();
			
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			int linesCount = 0;
			while (reader.readLine() != null) linesCount++;
			reader.close();
			
			activity.setValue(0);
			activity.setMaximum(linesCount);
			activity.setIndeterminate(false);

			FileInputStream fstream = new FileInputStream(inputFile);
			in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;
			br.readLine(); // skip header
			while ((strLine = br.readLine()) != null)   {
				lines++;
				activity.setValue(lines);
				String columns[] = null;
				String identifierSchemeId = null;
				String alternateIdentifier = null;
				String effectiveTime = null;
				String active = null;
				String moduleId = null;
				String referencedComponentId = null;
				try{
					columns =  strLine.split("\\t",-1);
					identifierSchemeId = columns[0];
					alternateIdentifier = columns[1];
					effectiveTime = columns[2];
					active = columns[3];
					moduleId = columns[4];
					referencedComponentId = columns[5];

					if (identifierSchemeId.equals("900000000000002006") && active.equals("1") ) {
						sctidsMap.put(UUID.fromString(alternateIdentifier), referencedComponentId);
						uuidsMap.put(referencedComponentId, UUID.fromString(alternateIdentifier));
						loaded++;
					}

				} catch (StringIndexOutOfBoundsException e) {
					AceLog.getAppLog().info("Line skipped due to string index problem: " + lines);
					skipped++;
				}  catch (ArrayIndexOutOfBoundsException e) {
					AceLog.getAppLog().info("Line skipped due to array index problem: " + lines);
					skipped++;
				}
			}
			in.close();
			if (!DwfaEnv.isHeadless()) {
				long endTime = System.currentTimeMillis();
				long elapsed = endTime - startTime;
				String elapsedStr = getElapsedText(elapsed);
				activity.setProgressInfoUpper("<html>Identifiers read finished: Lines: " + lines + 
						" Loaded: " + loaded + " Skipped: " + skipped);
				activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";");
				activity.complete();
				Terms.get().getActiveAceFrameConfig().setStatusMessage("");
			}
			JOptionPane.showMessageDialog(null,
					"Identifiers read finished: Lines: " + lines + 
					" Loaded: " + loaded + " Skipped: " + skipped );
		} catch (Exception e) {
			try {
				in.close();
				if (!DwfaEnv.isHeadless()) {
					long endTime = System.currentTimeMillis();
					long elapsed = endTime - startTime;
					String elapsedStr = getElapsedText(elapsed);
					activity.setProgressInfoUpper("<html>Identifiers read finished: Lines: " + lines + 
							" Loaded: " + loaded + " Skipped: " + skipped);
					activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";");
					activity.complete();
					Terms.get().getActiveAceFrameConfig().setStatusMessage("");
				}
			} catch (HeadlessException e1) {
				e1.printStackTrace();
			} catch (ComputationCanceled e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (TerminologyException e1) {
				e1.printStackTrace();
			}
			throw new TaskFailedException(e);
		}
		
		// load Simple maps
		try {
			String filename = File.separator+"txt";
			JFileChooser fc = new JFileChooser(new File(filename));
			fc.setDialogTitle("Select Simple Maps File");
			fc.showOpenDialog(null);
			File inputFile = fc.getSelectedFile();
			//String filename = (String) process.getProperty(inputFilePropName);
			//File inputFile = new File(filename);
			filename = inputFile.getName();
			I_TermFactory tf = Terms.get();
			config = tf.getActiveAceFrameConfig();

			if (!DwfaEnv.isHeadless()) {
				activity = Terms.get().newActivityPanel(true, config, 
						"<html>Reading simplemaps from: " + filename, true);
				activities.add(activity);
				activity.setValue(0);
				activity.setIndeterminate(true);
				activity.setProgressInfoLower("Loading data...");
				Terms.get().getActiveAceFrameConfig().setStatusMessage("Loading data...");
			}
			startTime = System.currentTimeMillis();
			lines = 0;
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			int linesCount = 0;
			while (reader.readLine() != null) linesCount++;
			reader.close();
			activity.setValue(0);
			activity.setMaximum(linesCount);
			activity.setIndeterminate(false);

			FileInputStream fstream = new FileInputStream(inputFile);
			in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;
			br.readLine(); // skip header
			while ((strLine = br.readLine()) != null)   {
				lines++;
				activity.setValue(lines);
				String columns[] = null;
				String id = null;
				String effectiveTime = null;
				String active = null;
				String moduleId = null;
				String refsetId = null;
				String referencedComponentId = null;
				String mapTarget = null;
				try{
					columns =  strLine.split("\\t",-1);
					id = columns[0];
					effectiveTime = columns[1];
					active = columns[2];
					moduleId = columns[3];
					refsetId = columns[4];
					referencedComponentId = columns[5];
					mapTarget = columns[6];

					if (refsetId.equals("900000000000497000") && active.equals("1") ) {
						UUID conceptUuid = uuidsMap.get(referencedComponentId);
						ctv3IdsMap.put(conceptUuid, mapTarget);
						loaded++;
					} else if (refsetId.equals("900000000000498005") && active.equals("1") ) {
						UUID conceptUuid = uuidsMap.get(referencedComponentId);
						snomedIdsMap.put(conceptUuid, mapTarget);
						loaded++;
					}

				} catch (StringIndexOutOfBoundsException e) {
					AceLog.getAppLog().info("Line skipped due to string index problem: " + lines);
					skipped++;
				}  catch (ArrayIndexOutOfBoundsException e) {
					AceLog.getAppLog().info("Line skipped due to array index problem: " + lines);
					skipped++;
				}
			}
			in.close();
			if (!DwfaEnv.isHeadless()) {
				long endTime = System.currentTimeMillis();
				long elapsed = endTime - startTime;
				String elapsedStr = getElapsedText(elapsed);
				activity.setProgressInfoUpper("<html>Identifiers read finished: Lines: " + lines + 
						" Loaded: " + loaded + " Skipped: " + skipped);
				activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";");
				activity.complete();
				Terms.get().getActiveAceFrameConfig().setStatusMessage("");
			}
			JOptionPane.showMessageDialog(null,
					"Simple map read finished: Lines: " + lines + 
					" Loaded: " + loaded + " Skipped: " + skipped );
		} catch (Exception e) {
			try {
				in.close();
				if (!DwfaEnv.isHeadless()) {
					long endTime = System.currentTimeMillis();
					long elapsed = endTime - startTime;
					String elapsedStr = getElapsedText(elapsed);
					activity.setProgressInfoUpper("<html>Simple map read finished: Lines: " + lines + 
							" Loaded: " + loaded + " Skipped: " + skipped);
					activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";");
					activity.complete();
					Terms.get().getActiveAceFrameConfig().setStatusMessage("");
				}
			} catch (HeadlessException e1) {
				e1.printStackTrace();
			} catch (ComputationCanceled e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (TerminologyException e1) {
				e1.printStackTrace();
			}
			throw new TaskFailedException(e);
		}

		//update database
		final int updated = 0;
		skipped = 0;
		final int alreadyAssigned = 0;
		try {
			if (!DwfaEnv.isHeadless()) {
				activity = Terms.get().newActivityPanel(true, config, 
						"<html>Updating database", true);
				activities.add(activity);
				activity.setValue(0);
				activity.setIndeterminate(true);
				activity.setProgressInfoLower("Updating database...");
				Terms.get().getActiveAceFrameConfig().setStatusMessage("Updating database...");
			}
			startTime = System.currentTimeMillis();

			TerminologyStoreDI ts = Ts.get();
			
			UpdateIdProcessor proc = new UpdateIdProcessor(sctidsMap, snomedIdsMap, ctv3IdsMap, activity);

			ts.iterateConceptDataInSequence(proc);

			if (!DwfaEnv.isHeadless()) {
				long endTime = System.currentTimeMillis();
				long elapsed = endTime - startTime;
				String elapsedStr = getElapsedText(elapsed);
				activity.setProgressInfoUpper("<html>Identifiers update finished: Identifiers added: " + proc.updated);
				activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";");
				activity.complete();
				Terms.get().getActiveAceFrameConfig().setStatusMessage("");
			}
			
			if (proc.updated > 0) {
				//Ts.get().commit();
			}
			JOptionPane.showMessageDialog(null,
					"Identifiers update finished: Identifiers added: " + proc.updated);
		} catch (Exception e) {
			try {
				in.close();
				if (!DwfaEnv.isHeadless()) {
					long endTime = System.currentTimeMillis();
					long elapsed = endTime - startTime;
					String elapsedStr = getElapsedText(elapsed);
					activity.setProgressInfoUpper("<html>Identifiers update finished with error...");
					activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";");
					activity.complete();
					Terms.get().getActiveAceFrameConfig().setStatusMessage("");
				}
			} catch (HeadlessException e1) {
				e1.printStackTrace();
			} catch (ComputationCanceled e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (TerminologyException e1) {
				e1.printStackTrace();
			}
			throw new TaskFailedException(e);
		}

		return Condition.CONTINUE;
	}

	public double round1(double value) {
		return Math.round(value * 10.0) / 10.0;
	}

	public String getElapsedText(long elapsedMillis) {
		if(elapsedMillis < 60000) {
			double unit = round1(elapsedMillis / 1000.0); 
			return unit + (unit == 1 ? " second" : " seconds");
		}
		else if(elapsedMillis < 60000 * 60) {
			double unit = round1(elapsedMillis / 60000.0); 
			return unit + (unit == 1 ? " minute" : " minutes");
		}
		else if(elapsedMillis < 60000 * 60 * 24) {
			double unit = round1(elapsedMillis / (60000.0 * 60)); 
			return unit + (unit == 1 ? " hour" : " hours");
		}
		else {
			double unit = round1(elapsedMillis / (60000.0 * 60 * 24)); 
			return unit + (unit == 1 ? " day" : " days");
		}
	}

	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

	public int[] getDataContainerIds() {
		return new int[] {};
	}

	public Integer getHostIndex() {
		return hostIndex;
	}

	public void setHostIndex(Integer hostIndex) {
		this.hostIndex = hostIndex;
	}

	public String getInputFilePropName() {
		return inputFilePropName;
	}

	public void setInputFilePropName(String inputFilePropName) {
		this.inputFilePropName = inputFilePropName;
	}

}
