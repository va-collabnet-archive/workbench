package org.ihtsdo.translation.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.JDialog;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.BdbTermFactory;
import org.ihtsdo.project.FileLink;
import org.ihtsdo.project.FileLinkAPI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.config.FileLinkConfigurationPanel;

public class TestFileLinkView extends TestCase {
	/** The vodb directory. */
	File vodbDirectory;

	/** The read only. */
	boolean readOnly = false;

	/** The cache size. */
	Long cacheSize = Long.getLong("600000000");

	/** The db setup config. */
	DatabaseSetupConfig dbSetupConfig;

	/** The config. */
	I_ConfigAceFrame config;

	/** The tf. */
	I_TermFactory tf;

	/** The new project concept. */
	I_GetConceptData newProjectConcept;

	/** The allowed statuses with retired. */
	I_IntSet allowedStatusesWithRetired;

	protected void setUp() throws Exception {
		super.setUp();
		System.out.println("Deleting test fixture");
		deleteDirectory(new File("berkeley-db"));
		System.out.println("Creating test fixture");
		copyDirectory(new File(
				"src/test/java/org/ihtsdo/translation/berkeley-db"), new File(
				"berkeley-db"));
		vodbDirectory = new File("berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}

	private void testAPI() throws Exception {
		FileLinkAPI flApi = new FileLinkAPI(config);

		try {
			// I_GetConceptData categoriesRoot =
			// tf.getConcept(ArchitectonicAuxiliary.Concept.FILE_LINK_CATEGORY.getUids());
			I_GetConceptData categoriesRoot = tf
					.getConcept(ArchitectonicAuxiliary.Concept.STATUS.getUids());
			Iterator<I_GetConceptData> s = flApi.getCategories(categoriesRoot)
					.iterator();
			while (s.hasNext()) {
				I_GetConceptData iGetConceptData = (I_GetConceptData) s.next();
				for (FileLink fileLink : flApi
						.getLinksForCategory(iGetConceptData)) {
					System.out.println("\t" + fileLink.getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void testConfigDialog() {

		JDialog dialog = new JDialog();
		ConfigTranslationModule confTrans;
		try {
			confTrans = LanguageUtil.getTranslationConfig(config);
			FileLinkConfigurationPanel panle = new FileLinkConfigurationPanel(
					config, confTrans);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

			// dialog.setPreferredSize(new Dimension(600, 400));
			dialog.setContentPane(panle);
			dialog.setResizable(false);
			dialog.pack();
			dialog.setVisible(true);
			Thread th = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			th.start();
			try {
				th.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			dialog.setVisible(false);
			dialog.dispose();
			LanguageUtil.setTranslationConfig(config, confTrans);

			JDialog dialog1 = new JDialog();

			FileLinkConfigurationPanel panle1 = new FileLinkConfigurationPanel(
					config, confTrans);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

			// dialog.setPreferredSize(new Dimension(600, 400));
			dialog1.setContentPane(panle1);
			dialog1.setResizable(false);
			dialog1.pack();
			dialog1.setVisible(true);
			Thread th1 = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			th1.start();
			try {
				th1.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private I_ConfigAceFrame getTestConfig() {
		I_ConfigAceFrame config = null;
		try {
			config = tf.newAceFrameConfig();
			config.addViewPosition(tf.newPosition(tf.getPath(new UUID[] { UUID
					.fromString("2faa9260-8fb2-11db-b606-0800200c9a66") }),
					Integer.MAX_VALUE));
			config.addViewPosition(tf.newPosition(tf.getPath(new UUID[] { UUID
					.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2") }),
					Integer.MAX_VALUE));
			config.addEditingPath(tf.getPath(new UUID[] { UUID
					.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2") }));
			config
					.getDescTypes()
					.add(
							ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
									.localize().getNid());
			config.getDescTypes().add(
					ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
							.localize().getNid());
			config.getDescTypes().add(
					ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE
							.localize().getNid());
			config.setDefaultStatus(tf
					.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE
							.localize().getNid())));
			config.getAllowedStatus().add(
					ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(
					ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());

			BdbTermFactory bdbFactory = (BdbTermFactory) tf;
			I_ConfigAceDb newDbProfile = bdbFactory.newAceDbConfig();
			newDbProfile.setUsername("username");
			newDbProfile
					.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			newDbProfile
					.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
			newDbProfile
					.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
			config.setDbConfig(newDbProfile);

			config.setPrecedence(Precedence.TIME);

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return config;
	}

	// If targetLocation does not exist, it will be created.
	public void copyDirectory(File sourceLocation, File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(
						targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

}
