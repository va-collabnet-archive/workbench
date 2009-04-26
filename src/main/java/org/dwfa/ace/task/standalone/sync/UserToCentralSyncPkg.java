package org.dwfa.ace.task.standalone.sync;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.zip.ZipEntry;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.cs.ChangeSetImporter;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.util.io.FileIO;
import org.dwfa.util.io.JarExtractor;

/**
 * 
 * @author kec
 * 
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/ssync/", type = BeanType.TASK_BEAN) })
public class UserToCentralSyncPkg extends AbstractTask {

	private static final long serialVersionUID = 1;

	private static final int dataVersion = 1;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion == 1) {
			// nothing to read...
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
	}

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
					.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG
							.name());
			int n = 1;
			String dir = "ssync";
			String prefix = config.getUsername() + "-central-";
			String suffix = ".ucs";
			File jarFile = new File(dir, prefix + n + suffix);
			while (jarFile.exists()) {
				n++;
				jarFile = new File(dir, prefix + n + suffix);
			}
			Manifest manifest = new Manifest();
			Attributes a = manifest.getMainAttributes();
			a.putValue(Name.MANIFEST_VERSION.toString(), "1.0");
			a.putValue(Name.MAIN_CLASS.toString(), JarExtractor.class
					.getCanonicalName());
			Map<String, Attributes> entries = manifest.getEntries();

			jarFile.getParentFile().mkdirs();
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(jarFile));
			JarOutputStream output = new JarOutputStream(bos, manifest);
			addToZip(Class.forName(JarExtractor.class.getCanonicalName()),
					output);

			// Inbox and outbox
			for (String queueFile : config.getDbConfig().getQueues()) {
				String queueConfigString = FileIO
						.readerToString(new FileReader(queueFile));
				if (queueConfigString.contains("getInboxQueueType")) {
					putInboxInJar(queueFile, output);
				} else if (queueConfigString.contains("getOutboxQueueType")) {
					putOutboxInJar(queueFile, output);
				}
			}

			// change sets
			Properties csProperties = new Properties();
			File csPropsFile = new File(config.getDbConfig().getProfileFile()
					.getParentFile(), ".csp");
			if (csPropsFile.exists()) {
				InputStream fis = new FileInputStream(csPropsFile);
				csProperties.loadFromXML(fis);
				fis.close();
			}

			List<File> changeSetFiles = new ArrayList<File>();
			ChangeSetImporter.addAllChangeSetFiles(new File("profiles"),
					changeSetFiles, ".jcs");
			for (File csf : changeSetFiles) {
				String lastImportSize = csProperties.getProperty(FileIO
						.getNormalizedRelativePath(csf));
				if (lastImportSize != null) {
					if (csf.length() != Long.parseLong(lastImportSize)) {
						// change set not previously sent, need to send it.
						addChangeSet(csProperties, output, csf);
					} else {
						// change set previously sent, no need to send again
					}
				} else {
					addChangeSet(csProperties, output, csf);
				}
			}
			OutputStream fos = new FileOutputStream(csPropsFile);
			csProperties.storeToXML(fos, "");
			fos.close();

			addToZip("profiles/", config.getDbConfig().getProfileFile(), output, "");
			addToZip("profiles/", csPropsFile, output, "");

			output.close();
			
			return Condition.CONTINUE;
		} catch (IOException ex) {
			throw new TaskFailedException(ex);
		} catch (ClassNotFoundException ex) {
			throw new TaskFailedException(ex);
		}
	}

	private void addChangeSet(Properties props, JarOutputStream output, File csf)
			throws IOException {
		props.setProperty(FileIO.getNormalizedRelativePath(csf),
				Long.toString(csf.length()));
		addToZip("changeSets/", csf, output, "");
	}

	private void putOutboxInJar(String queueFileStr, JarOutputStream output)
			throws IOException {
		AceLog.getAppLog().info("Processing outbox: " + queueFileStr);
		File queueDir = new File(queueFileStr).getParentFile();
		String prefix = "outboxes/";
		recursiveAddToZip(output, queueDir, prefix);
	}

	private void putInboxInJar(String queueFileStr, JarOutputStream output)
			throws IOException {
		AceLog.getAppLog().info("Processing inbox: " + queueFileStr);
		File queueDir = new File(queueFileStr).getParentFile();
		File queueLlog = new File(queueDir, ".llog");
		File[] logItems = queueLlog.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".bp");
			}
		});
		if (logItems != null) {
			String prefix = "inboxes/";
			for (File llogEntry : logItems) {
				File queueFile = new File(queueDir, llogEntry.getName());
				if (queueFile.exists() == false) {
					ZipEntry entry = new ZipEntry(prefix
							+ FileIO.getRelativePath(queueFile.getParentFile())
							+ "/.dlog/" + queueFile.getName());
					entry.setSize(0);
					entry.setTime(System.currentTimeMillis());
					entry.setComment(null);
					output.putNextEntry(entry);
					output.closeEntry();
				}
			}
		}
	}

	private void recursiveAddToZip(JarOutputStream output, File parent,
			String prefix) throws IOException {
		for (File child : parent.listFiles()) {
			if (child.isDirectory()) {
				recursiveAddToZip(output, child, prefix);
			} else {
				addToZip(prefix, child, output, null);
			}
		}
	}

	/*
	 * TODO Move this method to foundation
	 */
	private static void addToZip(String prefix, File f, JarOutputStream output,
			String comment) throws IOException {
		ZipEntry entry = new ZipEntry(prefix + FileIO.getRelativePath(f));
		entry.setSize(f.length());
		entry.setTime(f.lastModified());
		entry.setComment(comment);
		output.putNextEntry(entry);
		FileInputStream fis = new FileInputStream(f);
		byte[] buf = new byte[10240];
		for (int i = 0;; i++) {
			int len = fis.read(buf);
			if (len < 0)
				break;
			output.write(buf, 0, len);
		}
		output.closeEntry();
	}

	/*
	 * TODO Move this method to foundation
	 */
	private static void addToZip(Class<?> theClass, JarOutputStream output)
			throws IOException, ClassNotFoundException {
		String classFileName = theClass.getName().replace('.', '/') + ".class";
		ZipEntry entry = new ZipEntry(classFileName);
		output.putNextEntry(entry);

		URL classUrl = theClass.getResource("/" + classFileName);
		AceLog.getAppLog().info(classUrl.toString());
		AceLog.getAppLog().info(classUrl.getContent().toString());

		InputStream classInputStream = classUrl.openStream();
		int size = classInputStream.available();
		byte[] data = new byte[size];
		classInputStream.read(data, 0, size);
		output.write(data, 0, size);
		output.closeEntry();
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do

	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}

}
