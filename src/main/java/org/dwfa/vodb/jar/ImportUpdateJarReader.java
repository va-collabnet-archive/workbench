package org.dwfa.vodb.jar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.dwfa.ace.ACE;
import org.dwfa.ace.AceLog;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.fd.FileDialogUtil;
import org.dwfa.vodb.bind.PathBinder;
import org.dwfa.vodb.bind.ThinConVersionedBinding;
import org.dwfa.vodb.bind.ThinDescVersionedBinding;
import org.dwfa.vodb.bind.ThinIdVersionedBinding;
import org.dwfa.vodb.bind.ThinImageBinder;
import org.dwfa.vodb.bind.ThinRelVersionedBinding;
import org.dwfa.vodb.bind.TimePathIdBinder;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinImageVersioned;
import org.dwfa.vodb.types.ThinRelVersioned;
import org.dwfa.vodb.types.TimePathId;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.je.DatabaseException;

public class ImportUpdateJarReader implements ActionListener {
	
	private class IdHashMap implements I_MapNativeToNative {
		private Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		
		private IdHashMap(int size) {
			map = new HashMap<Integer, Integer>(size);
		}
		public void add(int jarId, int dbId) throws DatabaseException {
			map.put(jarId, dbId);
		}

		public int get(int jarId) {
			return map.get(jarId);
		}
	}

	I_MapNativeToNative jarToDbNativeMap;
	
	Set<TimePathId> timePathSet = Collections.synchronizedSet(new HashSet<TimePathId>());

	JarInputStream input;

	ThinConVersionedBinding conBinding = new ThinConVersionedBinding();

	ThinDescVersionedBinding descBinding = new ThinDescVersionedBinding();

	ThinRelVersionedBinding relBinding = new ThinRelVersionedBinding();

	ThinIdVersionedBinding idBinding = new ThinIdVersionedBinding();

	ThinImageBinder imageBinder = new ThinImageBinder();

	PathBinder pathBinder = new PathBinder();

	boolean continueWork = true;

	String upperProgressMessage = "Reading Jar File";

	String lowerProgressMessage = "counting";

	int max = -1;

	int concepts = -1;

	int descriptions = -1;

	int relationships = -1;

	int ids = -1;

	int images = -1;

	int total = -1;

	int processed = 0;

	private CountDownLatch latch;

	private AceConfig config;

	private int timePathEntries;

	private class ProgressUpdator implements I_UpdateProgress {
		Timer updateTimer;

		boolean firstUpdate = true;

		ActivityPanel activity = new ActivityPanel(true, true);

		public ProgressUpdator() {
			super();
			updateTimer = new Timer(300, this);
			updateTimer.start();
		}

		public void actionPerformed(ActionEvent e) {
			if (firstUpdate) {
				firstUpdate = false;
				ActivityViewer.addActivity(activity);
			}
			activity.setIndeterminate(total == -1);
			activity.setValue(processed);
			activity.setMaximum(total);
			activity.setProgressInfoUpper(upperProgressMessage);
			if (latch != null) {
				activity.setProgressInfoLower(lowerProgressMessage + processed +
						" latch: " + latch.getCount());				
			} else {
				activity.setProgressInfoLower(lowerProgressMessage + processed);
			}
			if (!continueWork) {
				activity.complete();
				updateTimer.stop();
			}
		}

		public void normalCompletion() {
			activity.complete();
			updateTimer.stop();
		}

	}

	public ImportUpdateJarReader() {
		try {
			final File jarFile = FileDialogUtil
					.getExistingFile("Select update jar file to import");
			ProgressUpdator updater = new ProgressUpdator();
			updater.activity.addActionListener(this);
			ACE.threadPool.execute(new Runnable() {
				public void run() {
					try {
						importJar(jarFile);
					} catch (TaskFailedException e1) {
						e1.printStackTrace();
					}
				}

			});
		} catch (TaskFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void importJar(File jarFile) throws TaskFailedException {
		try {
			JarFile jf = new JarFile(jarFile);
			Manifest mf = jf.getManifest();
			Map<String, Attributes> attributeMap = mf.getEntries();

			for (String entry : attributeMap.keySet()) {
				if (entry.equals("concepts.ace")) {
					Attributes a = attributeMap.get(entry);
					concepts = Integer.parseInt(a.getValue("count"));
					System.out
							.println(entry + " count: " + a.getValue("count"));
				} else if (entry.equals("descriptions.ace")) {
					Attributes a = attributeMap.get(entry);
					descriptions = Integer.parseInt(a.getValue("count"));
					System.out
							.println(entry + " count: " + a.getValue("count"));
				} else if (entry.equals("relationships.ace")) {
					Attributes a = attributeMap.get(entry);
					relationships = Integer.parseInt(a.getValue("count"));
					System.out
							.println(entry + " count: " + a.getValue("count"));
				} else if (entry.equals("ids.ace")) {
					Attributes a = attributeMap.get(entry);
					ids = Integer.parseInt(a.getValue("count"));
					System.out
							.println(entry + " count: " + a.getValue("count"));
				} else if (entry.equals("images.ace")) {
					Attributes a = attributeMap.get(entry);
					images = Integer.parseInt(a.getValue("count"));
					System.out
							.println(entry + " count: " + a.getValue("count"));
				} else if (entry.equals("timePath.ace")) {
					Attributes a = attributeMap.get(entry);
					timePathEntries = Integer.parseInt(a.getValue("count"));
					System.out
							.println(entry + " count: " + a.getValue("count"));
				}
			}

			total = concepts + descriptions + relationships + ids + images + timePathEntries;

			jarToDbNativeMap = new IdHashMap(ids);

			JarEntry je = jf.getJarEntry("ids.ace");
			lowerProgressMessage = "Processing ids. Total items: ";
			processIds(jf.getInputStream(je));

			for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements();) {
				je = e.nextElement();
				System.out.println("Jar entry: " + je.getName()
						+ " compressed: " + je.getCompressedSize() + " size: "
						+ je.getSize() + " time: " + new Date(je.getTime())
						+ " comment: " + je.getComment());

				if (je.getName().equals("concepts.ace")) {
					lowerProgressMessage = "Processing concepts. Total items: ";
					processConcepts(jf.getInputStream(je));
				} else if (je.getName().equals("descriptions.ace")) {
					lowerProgressMessage = "Processing descriptions. Total items: ";
					processDescriptions(jf.getInputStream(je));
				} else if (je.getName().equals("relationships.ace")) {
					lowerProgressMessage = "Processing relationships. Total items: ";
					processRelationships(jf.getInputStream(je));
				} else if (je.getName().equals("ids.ace")) {
					// already processed above...
				} else if (je.getName().equals("images.ace")) {
					lowerProgressMessage = "Processing images. Total items: ";
					processImages(jf.getInputStream(je));
				} else if (je.getName().equals("paths.ace")) {
					lowerProgressMessage = "Processing paths. Total items: ";
					processPaths(jf.getInputStream(je));
				}  else if (je.getName().equals("config.ace")) {
					ObjectInputStream ois = new ObjectInputStream(jf.getInputStream(je));
					config = (AceConfig) ois.readObject();
				} else if (je.getName().equals("timePath.ace")) {
					lowerProgressMessage = "Processing time/path entries. Total items: ";
					processTimePaths(jf.getInputStream(je));
				} 
			}
			AceConfig.vodb.addTimeBranchValues(timePathSet);
			
			lowerProgressMessage = "Starting c1RelMap.";
			AceConfig.vodb.createC1RelMap();
			lowerProgressMessage = "Starting c2RelMap.";
			AceConfig.vodb.createC2RelMap();
			lowerProgressMessage = "Starting createIdMaps.";
			AceConfig.vodb.createIdMaps();
			lowerProgressMessage = "Starting createConceptImageMap.";
			AceConfig.vodb.createConceptImageMap();
			lowerProgressMessage = "Starting sync.";
			AceConfig.vodb.sync();

			continueWork = false;
			if (config != null) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						for (AceFrameConfig ace: config.aceFrames) {
							if (ace.isActive()) {
								ACE cdePanel;
								try {
									cdePanel = new ACE();
									cdePanel.setup(ace);
									JFrame cdeFrame = new JFrame(ace.getFrameName());
									cdeFrame.setContentPane(cdePanel);
									cdeFrame.setJMenuBar(cdePanel.createMenuBar());

									cdeFrame.setBounds(ace.getBounds());
									cdeFrame.setVisible(true);
								} catch (Exception e) {
									AceLog.alertAndLog(Level.SEVERE, e.getLocalizedMessage(), e);
								}
							}
						}
					}
					
				});
			}
		} catch (Exception e) {
			continueWork = false;
			throw new TaskFailedException(e);
		}
		
	}

	private void processIds(InputStream inputStream) throws IOException,
			InterruptedException {
		ExecutorService threadPool = Executors.newFixedThreadPool(1);
		ThinIdVersionedBinding binding = new ThinIdVersionedBinding();
		DataInputStream dis = new DataInputStream(inputStream);
		byte[] buffer = new byte[1024];
		latch = new CountDownLatch(ids);
		while (dis.available() > 0) {
			try {
				int size = dis.readInt();
				if (size > buffer.length) {
					buffer = new byte[size];
					System.out.println("Increasing id buffer: " + size);
				}
				int read = dis.read(buffer, 0, size);
				while (read != size) {
					size = size - read;
					read = dis.read(buffer, read, size);
				}
				TupleInput input = new TupleInput(buffer);
				I_IdVersioned jarId = binding.entryToObject(input);
				threadPool.execute(new SyncIdWithDb(jarId, latch));
				processed++;
			} catch (Throwable e) {
				System.out.println("processed: " + processed);
				dis.close();
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		dis.close();
		System.out.println("Awaiting latch");
		latch.await();
		latch = null;
		System.out.println("Latch released");
		threadPool.shutdown();
	}

	private class SyncIdWithDb implements Runnable {
		I_IdVersioned jarId;

		CountDownLatch idLatch;

		public SyncIdWithDb(I_IdVersioned jarId, CountDownLatch idLatch) {
			super();
			this.jarId = jarId;
			this.idLatch = idLatch;
		}

		public void run() {
			try {
				if (AceConfig.vodb.hasId(jarId.getUIDs())) {
					I_IdVersioned dbId = AceConfig.vodb.getId(jarId.getUIDs());
					boolean changed = false;
					for (I_IdPart p : jarId.getVersions()) {
						if (!dbId.hasVersion(p)) {
							dbId.addVersion(p);
							changed = true;
						}
					}
					if (changed) {
						AceConfig.vodb.writeId(dbId);
						timePathSet.addAll(jarId.getTimePathSet());
					}
					jarToDbNativeMap.add(jarId.getNativeId(), dbId.getNativeId());
				} else {
					int jarNativeId = jarId.getNativeId();
					I_IdVersioned dbId = jarId;
		        	int idSource = AceConfig.vodb.uuidToNative(ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
					dbId.setNativeId(AceConfig.vodb.uuidToNativeWithGeneration(jarId.getUIDs(), idSource,
							new Path(Integer.MIN_VALUE + 1,
									new ArrayList<Position>()), Integer.MAX_VALUE));
					AceConfig.vodb.writeId(dbId);
					jarToDbNativeMap.add(jarNativeId, dbId.getNativeId());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			idLatch.countDown();
		}

	}

	private void processImages(InputStream inputStream) throws Exception {
		ThinImageBinder binding = new ThinImageBinder();
		DataInputStream dis = new DataInputStream(inputStream);
		byte[] buffer = new byte[1024];
		while (dis.available() > 0) {
			int size = dis.readInt();
			if (size > buffer.length) {
				buffer = new byte[size];
				System.out.println("Increasing image buffer: " + size);
			}
			int read = dis.read(buffer, 0, size);
			while (read != size) {
				size = size - read;
				read = dis.read(buffer, read, size);
			}
			TupleInput input = new TupleInput(buffer);
			ThinImageVersioned jarImage = binding.entryToObject(input);
			jarImage.convertIds(jarToDbNativeMap);
			if (AceConfig.vodb.hasImage(jarImage
					.getImageId())) {
				I_ImageVersioned dbImage = AceConfig.vodb.getImage(jarImage.getImageId());
				if (dbImage.merge(jarImage)) {
					AceConfig.vodb.writeImage(dbImage);
					timePathSet.addAll(jarImage.getTimePathSet());
				}
			} else {
				AceConfig.vodb.writeImage(jarImage);
			}
			processed++;
		}
		dis.close();
	}

	private void processRelationships(InputStream inputStream)
			throws Exception {
		ThinRelVersionedBinding binding = new ThinRelVersionedBinding();
		DataInputStream dis = new DataInputStream(inputStream);
		byte[] buffer = new byte[1024];
		while (dis.available() > 0) {
			int size = dis.readInt();
			if (size > buffer.length) {
				buffer = new byte[size];
				System.out.println("Increasing relationship buffer: " + size);
			}
			int read = dis.read(buffer, 0, size);
			while (read != size) {
				size = size - read;
				read = dis.read(buffer, read, size);
			}
			TupleInput input = new TupleInput(buffer);
			ThinRelVersioned jarRel = binding.entryToObject(input);
			jarRel.convertIds(jarToDbNativeMap);
			if (AceConfig.vodb.hasRel(jarRel
					.getRelId())) {
				I_RelVersioned dbRel = AceConfig.vodb.getRel(jarRel.getRelId());
				if (dbRel.merge(jarRel)) {
					AceConfig.vodb.writeRel(dbRel);
					timePathSet.addAll(jarRel.getTimePathSet());
				}
			} else {
				AceConfig.vodb.writeRel(jarRel);
			}
			processed++;
		}
		dis.close();
	}

	private void processDescriptions(InputStream inputStream)
			throws Exception {
		ThinDescVersionedBinding binding = new ThinDescVersionedBinding();
		DataInputStream dis = new DataInputStream(inputStream);
		byte[] buffer = new byte[1024];
		while (dis.available() > 0) {
			int size = dis.readInt();
			if (size > buffer.length) {
				buffer = new byte[size];
				System.out.println("Increasing description buffer: " + size);
			}
			int read = dis.read(buffer, 0, size);
			while (read != size) {
				size = size - read;
				read = dis.read(buffer, read, size);
			}
			TupleInput input = new TupleInput(buffer);
			ThinDescVersioned jarDesc = binding.entryToObject(input);
			jarDesc.convertIds(jarToDbNativeMap);
			if (AceConfig.vodb.hasDescription(jarDesc
					.getDescId())) {
				I_DescriptionVersioned dbDesc = AceConfig.vodb.getDescription(jarDesc.getDescId());
				if (dbDesc.merge(jarDesc)) {
					AceConfig.vodb.writeDescription(dbDesc);
					timePathSet.addAll(jarDesc.getTimePathSet());
				}
			} else {
				AceConfig.vodb.writeDescription(jarDesc);
			}
			processed++;
		}
		dis.close();
	}

	private void processConcepts(InputStream inputStream) throws Exception {
		ThinConVersionedBinding binding = new ThinConVersionedBinding();
		DataInputStream dis = new DataInputStream(inputStream);
		byte[] buffer = new byte[1024];
		while (dis.available() > 0) {
			int size = dis.readInt();
			if (size > buffer.length) {
				buffer = new byte[size];
				System.out.println("Setting concept buffer size to: " + size);
			}
			int read = dis.read(buffer, 0, size);
			while (read != size) {
				size = size - read;
				read = dis.read(buffer, read, size);
			}
			TupleInput input = new TupleInput(buffer);
			ThinConVersioned jarCon = binding.entryToObject(input);
			jarCon.convertIds(jarToDbNativeMap);
			if (AceConfig.vodb.hasConcept(jarCon
					.getConId())) {
				I_ConceptAttributeVersioned dbCon = AceConfig.vodb.getConcept(jarCon
						.getConId());
				if (dbCon.merge(jarCon)) {
					AceConfig.vodb.writeConcept(dbCon);
					timePathSet.addAll(jarCon.getTimePathSet());
				}
				
			} else {
				AceConfig.vodb.writeConcept(jarCon);
			}
			processed++;
		}
		dis.close();
	}

	private void processPaths(InputStream inputStream) throws Exception {
		DataInputStream dis = new DataInputStream(inputStream);
		byte[] buffer = new byte[1024];
		while (dis.available() > 0) {
			int size = dis.readInt();
			if (size > buffer.length) {
				buffer = new byte[size];
				System.out.println("Setting path buffer size to: " + size);
			}
			int read = dis.read(buffer, 0, size);
			while (read != size) {
				size = size - read;
				read = dis.read(buffer, read, size);
			}
			TupleInput input = new TupleInput(buffer);
			try {
				Path jarPath = pathBinder.entryToObject(input);
				jarPath.convertIds(jarToDbNativeMap);
				if (AceConfig.vodb.hasPath(jarPath
						.getConceptId())) {
					//Nothing to do, paths don't have versioned parts
				} else {
					AceConfig.vodb.writePath(jarPath);
				}
			} catch (RuntimeException e) {
				System.out.println("processing paths: " + processed);
				throw e;
			}
			processed++;
		}
		dis.close();
	}
	private void processTimePaths(InputStream inputStream) throws Exception {
		TimePathIdBinder timePathIdBinder = new TimePathIdBinder();
		DataInputStream dis = new DataInputStream(inputStream);
		byte[] buffer = new byte[1024];
		while (dis.available() > 0) {
			int size = dis.readInt();
			if (size > buffer.length) {
				buffer = new byte[size];
				System.out.println("Setting path buffer size to: " + size);
			}
			int read = dis.read(buffer, 0, size);
			while (read != size) {
				size = size - read;
				read = dis.read(buffer, read, size);
			}
			TupleInput input = new TupleInput(buffer);
			try {
				TimePathId jarTimePath = (TimePathId) timePathIdBinder.entryToObject(input);
				jarTimePath.convertIds(jarToDbNativeMap);
				AceConfig.vodb.writeTimePath(jarTimePath);
			} catch (RuntimeException e) {
				System.out.println("processing paths: " + processed);
				throw e;
			}
			processed++;
		}
		dis.close();
	}

	public void actionPerformed(ActionEvent e) {
		continueWork = false;
		lowerProgressMessage = "User stopped action";
	}

}
