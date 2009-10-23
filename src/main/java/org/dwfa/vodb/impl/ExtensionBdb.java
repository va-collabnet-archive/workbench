package org.dwfa.vodb.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.vodb.I_StoreExtensions;
import org.dwfa.vodb.I_StoreInBdb;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.MemberAndSecondaryId;
import org.dwfa.vodb.bind.MemberAndSecondaryIdBinding;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.bind.ThinExtComponentIdSecondaryKeyCreator;
import org.dwfa.vodb.bind.ThinExtRefsetIdSecondaryKeyCreator;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;
import org.dwfa.vodb.types.I_ProcessExtByRefEntries;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;

public class ExtensionBdb implements I_StoreInBdb, I_StoreExtensions {

	private Database extensionDb;
	private SecondaryDatabase refsetToExtMap;
	private SecondaryDatabase componentToExtMap;
	private TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);
	private ThinExtBinder extBinder = new ThinExtBinder();

	ThinExtRefsetIdSecondaryKeyCreator refsetKeyCreator = new ThinExtRefsetIdSecondaryKeyCreator();
	ThinExtComponentIdSecondaryKeyCreator componentKeyCreator = new ThinExtComponentIdSecondaryKeyCreator();

	public ExtensionBdb(Environment env, DatabaseConfig dbConfig)
			throws DatabaseException {
		super();
		extensionDb = env.openDatabase(null, "extensionDb", dbConfig);

		SecondaryConfig extByRefsetIdConfig = new SecondaryConfig();
		extByRefsetIdConfig.setReadOnly(VodbEnv.isReadOnly());
		extByRefsetIdConfig.setDeferredWrite(VodbEnv.isDeferredWrite());
		extByRefsetIdConfig.setAllowCreate(!VodbEnv.isReadOnly());
		extByRefsetIdConfig.setSortedDuplicates(false);
		extByRefsetIdConfig.setKeyCreator(refsetKeyCreator);
		extByRefsetIdConfig.setAllowPopulate(true);
		extByRefsetIdConfig.setTransactional(VodbEnv.isTransactional());

		refsetToExtMap = env.openSecondaryDatabase(null, "refsetToExtMap",
				extensionDb, extByRefsetIdConfig);

		SecondaryConfig extByComponentIdConfig = new SecondaryConfig();
		extByComponentIdConfig.setReadOnly(VodbEnv.isReadOnly());
		extByComponentIdConfig.setDeferredWrite(VodbEnv.isDeferredWrite());
		extByComponentIdConfig.setAllowCreate(!VodbEnv.isReadOnly());
		extByComponentIdConfig.setSortedDuplicates(false);
		extByComponentIdConfig.setKeyCreator(componentKeyCreator);
		extByComponentIdConfig.setAllowPopulate(true);
		extByComponentIdConfig.setTransactional(VodbEnv.isTransactional());

		componentToExtMap = env.openSecondaryDatabase(null,
				"componentToExtMap", extensionDb, extByComponentIdConfig);

	}

	public void close() throws DatabaseException {
		if (extensionDb != null) {
			extensionDb.close();
		}
		if (componentToExtMap != null) {
			componentToExtMap.close();
		}

		if (refsetToExtMap != null) {
			refsetToExtMap.close();
		}
	}

	public void sync() throws DatabaseException {
		if (extensionDb != null) {
			if (!extensionDb.getConfig().getReadOnly()) {
				extensionDb.sync();
			}
		}

		if (componentToExtMap != null) {
			if (!componentToExtMap.getConfig().getReadOnly()) {
				componentToExtMap.sync();
			}
		}

		if (refsetToExtMap != null) {
			if (!refsetToExtMap.getConfig().getReadOnly()) {
				refsetToExtMap.sync();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.impl.I_StoreExtensions#writeExt(org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned)
	 */
	public void writeExt(I_ThinExtByRefVersioned ext) throws DatabaseException,
			IOException {
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry value = new DatabaseEntry();
		intBinder.objectToEntry(ext.getMemberId(), key);
		extBinder.objectToEntry(ext, value);
		extensionDb.put(BdbEnv.transaction, key, value);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.impl.I_StoreExtensions#iterateExtByRefEntries(org.dwfa.vodb.types.I_ProcessExtByRefEntries)
	 */
	public void iterateExtByRefEntries(I_ProcessExtByRefEntries processor)
			throws Exception {
		Cursor extCursor = extensionDb.openCursor(null, null);
		DatabaseEntry foundKey = processor.getKeyEntry();
		DatabaseEntry foundData = processor.getDataEntry();
		while (extCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			try {
				processor.processEbr(foundKey, foundData);
			} catch (Exception e) {
				extCursor.close();
				throw e;
			}
		}
		extCursor.close();
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.impl.I_StoreExtensions#getRefsetExtensionMembers(int)
	 */
	public List<I_ThinExtByRefVersioned> getRefsetExtensionMembers(int refsetId)
			throws IOException {
		try {
			List<I_ThinExtByRefVersioned> returnList = new ArrayList<I_ThinExtByRefVersioned>();
			Cursor extCursor = extensionDb.openCursor(null, null);
			DatabaseEntry foundKey = new DatabaseEntry();
			DatabaseEntry foundData = new DatabaseEntry();
			while (extCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				try {
					I_ThinExtByRefVersioned extension = (I_ThinExtByRefVersioned) extBinder
							.entryToObject(foundData);
					if (extension.getRefsetId() == refsetId) {
						returnList.add(extension);
					}
				} catch (Exception e) {
					extCursor.close();
					throw e;
				}
			}
			extCursor.close();
			return returnList;
		} catch (Exception e) {
			throw new ToIoException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.impl.I_StoreExtensions#getExtensionsForRefset(int)
	 */
	public List<ExtensionByReferenceBean> getExtensionsForRefset(int refsetId)
			throws DatabaseException {
		Stopwatch timer = null;
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("Getting extensions from refsetId for: " + refsetId);
			timer = new Stopwatch();
			timer.start();
		}
		DatabaseEntry secondaryKey = new DatabaseEntry();

		memberAndSecondaryIdBinding.objectToEntry(new MemberAndSecondaryId(Integer.MIN_VALUE, 
				refsetId), secondaryKey);

		DatabaseEntry foundData = new DatabaseEntry();

		SecondaryCursor mySecCursor = refsetToExtMap.openSecondaryCursor(
				null, null);
		OperationStatus retVal = mySecCursor.getSearchKeyRange(secondaryKey,
				foundData, LockMode.DEFAULT);
		List<ExtensionByReferenceBean> matches = new ArrayList<ExtensionByReferenceBean>();
		while (retVal == OperationStatus.SUCCESS) {
			I_ThinExtByRefVersioned extFromComponentId = (I_ThinExtByRefVersioned) extBinder
					.entryToObject(foundData);
			if (extFromComponentId.getRefsetId() == refsetId) {
				matches.add(ExtensionByReferenceBean.make(extFromComponentId
						.getMemberId(), extFromComponentId));
			} else {
				break;
			}
			retVal = mySecCursor.getNext(secondaryKey, foundData,
					LockMode.DEFAULT);
		}
		mySecCursor.close();
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("Extensions fetched for: " + refsetId
					+ " elapsed time: " + timer.getElapsedTime() / 1000
					+ " secs");
		}
		return matches;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.impl.I_StoreExtensions#getAllExtensionsForComponent(int)
	 */
	public List<I_ThinExtByRefVersioned> getAllExtensionsForComponent(
			int componentId) throws IOException {
		try {
			Stopwatch timer = null;
			if (AceLog.getAppLog().isLoggable(Level.FINE)) {
				AceLog.getAppLog().fine("Getting extensions from componentId for: "
						+ componentId);
				timer = new Stopwatch();
				timer.start();
			}
			DatabaseEntry secondaryKey = new DatabaseEntry();

			memberAndSecondaryIdBinding.objectToEntry(new MemberAndSecondaryId(componentId, 
					Integer.MIN_VALUE), secondaryKey);
			DatabaseEntry foundData = new DatabaseEntry();

			SecondaryCursor mySecCursor = componentToExtMap
					.openSecondaryCursor(null, null);
			OperationStatus retVal = mySecCursor.getSearchKeyRange(
					secondaryKey, foundData, LockMode.DEFAULT);
			List<I_ThinExtByRefVersioned> matches = new ArrayList<I_ThinExtByRefVersioned>();
			int count = 0;
			int rejected = 0;
			while (retVal == OperationStatus.SUCCESS) {
				I_ThinExtByRefVersioned extFromComponentId = (I_ThinExtByRefVersioned) extBinder
						.entryToObject(foundData);
				if (extFromComponentId.getComponentId() == componentId) {
					count++;
					ExtensionByReferenceBean extBean = ExtensionByReferenceBean
							.make(extFromComponentId.getMemberId(),
									extFromComponentId);
					if (extBean == null) {
            AceLog.getAppLog().severe("extBean is null for component: " + ConceptBean.get(componentId));
            AceLog.getAppLog().severe("extFromComponentId: " + extFromComponentId);
            AceLog.getAppLog().severe("extFromComponentId.getMemberId(): " + extFromComponentId.getMemberId());
					}
					I_ThinExtByRefVersioned ext = extBean.getExtension();
					matches.add(ext);
				} else {
					rejected++;
					break;
				}
				retVal = mySecCursor.getNext(secondaryKey, foundData,
						LockMode.DEFAULT);
			}
			mySecCursor.close();
			if (AceLog.getAppLog().isLoggable(Level.FINE)) {
				AceLog.getAppLog().fine(count + " extensions fetched, " + rejected
						+ " extensions rejected " + "for: " + componentId
						+ " elapsed time: " + timer.getElapsedTime() / 1000
						+ " secs");
			}
			return matches;
		} catch (DatabaseException ex) {
			throw new ToIoException(ex);
		}
	}

    MemberAndSecondaryIdBinding memberAndSecondaryIdBinding = new MemberAndSecondaryIdBinding();

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.impl.I_StoreExtensions#getExtensionsForComponent(int)
	 */
	public List<I_GetExtensionData> getExtensionsForComponent(int componentId)
			throws IOException {
		try {
			Stopwatch timer = null;
			if (AceLog.getAppLog().isLoggable(Level.FINE)) {
				AceLog.getAppLog().fine("Getting extensions from componentId for: "
						+ componentId);
				timer = new Stopwatch();
				timer.start();
			}
			DatabaseEntry secondaryKey = new DatabaseEntry();

			memberAndSecondaryIdBinding.objectToEntry(new MemberAndSecondaryId(componentId, 
					Integer.MIN_VALUE), secondaryKey);
			DatabaseEntry foundData = new DatabaseEntry();

			SecondaryCursor mySecCursor = componentToExtMap
					.openSecondaryCursor(null, null);
			OperationStatus retVal = mySecCursor.getSearchKeyRange(
					secondaryKey, foundData, LockMode.DEFAULT);
			List<I_GetExtensionData> matches = new ArrayList<I_GetExtensionData>();
			int count = 0;
			int rejected = 0;
			while (retVal == OperationStatus.SUCCESS) {
				I_ThinExtByRefVersioned extFromComponentId = (I_ThinExtByRefVersioned) extBinder
						.entryToObject(foundData);
				if (extFromComponentId.getComponentId() == componentId) {
					count++;
					matches.add(ExtensionByReferenceBean.make(
							extFromComponentId.getMemberId(),
							extFromComponentId));
				} else {
					rejected++;
					break;
				}
				retVal = mySecCursor.getNext(secondaryKey, foundData,
						LockMode.DEFAULT);
			}
			mySecCursor.close();
			if (AceLog.getAppLog().isLoggable(Level.FINE)) {
				AceLog.getAppLog().fine(count + " extensions fetched, " + rejected
						+ " extensions rejected " + "for: " + componentId
						+ " elapsed time: " + timer.getElapsedTime() / 1000
						+ " secs");
			}
			return matches;
		} catch (DatabaseException ex) {
			throw new ToIoException(ex);
		}
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.impl.I_StoreExtensions#getExtension(int)
	 */
	public I_ThinExtByRefVersioned getExtension(int memberId)
			throws IOException {
		DatabaseEntry extKey = new DatabaseEntry();
		DatabaseEntry extValue = new DatabaseEntry();
		intBinder.objectToEntry(memberId, extKey);
		try {
			if (extensionDb.get(BdbEnv.transaction, extKey, extValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
				return (I_ThinExtByRefVersioned) extBinder
						.entryToObject(extValue);
			}
		} catch (DatabaseException ex) {
			throw new ToIoException(ex);
		}
		throw new IOException("Ext: " + memberId + " not found.");
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.impl.I_StoreExtensions#hasExtension(int)
	 */
	public boolean hasExtension(int memberId) throws DatabaseException {
		if (ExtensionByReferenceBean.hasNew(memberId)) {
			return true;
		}
		DatabaseEntry extKey = new DatabaseEntry();
		DatabaseEntry extValue = new DatabaseEntry();
		intBinder.objectToEntry(memberId, extKey);
		if (extensionDb.get(BdbEnv.transaction, extKey, extValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
			return true;
		}
		return false;
	}

	public I_ThinExtByRefVersioned extEntryToObject(DatabaseEntry key,
			DatabaseEntry value) {
		return (I_ThinExtByRefVersioned) extBinder.entryToObject(value);
	}

	public void commit(ConceptBean bean, int version, Set<TimePathId> values)
			throws DatabaseException {
		// Nothing to do...
		
	}

	public void setupBean(ConceptBean cb) throws IOException {
		// nothing to do
	}

}
