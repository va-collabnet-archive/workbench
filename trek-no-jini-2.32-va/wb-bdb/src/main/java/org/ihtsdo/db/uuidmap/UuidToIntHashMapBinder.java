package org.ihtsdo.db.uuidmap;

import java.util.UUID;
import java.util.logging.Logger;

import org.ihtsdo.db.bdb.nidmaps.UuidIntConcurrentHashMapBinder.DB_TYPE;

import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * A Binder implementation for the {@link UuidToIntHashMap} that allows storage in BDB.
 * 
 * This implementation is a bit of a hack, as the BDB TupleBinding storage mechansim only allows 
 * storage of int sized byte arrays.  This becomes a problem for large databases, as the number of 
 * bytes we need to store to write out the key set for the UuidToIntHashMap is larger than Integer.MAX_VALUE.
 * 
 * This implementation picks an arbitrary chunk size (small enough to fit in the TupleBinding limits without issue)
 * and splits up the data across multiple Tuples in the DB.
 * 
 * All of the logic to handle the chunked reading and writing is handled by the {@link #read(Database)} 
 * and @{link {@link #write(Database, UuidToIntHashMap)} methods.
 * 
 *  Use caution directly accessing the entryToObject or objectToEntry methods, as they don't handle the chunking by 
 *  themselves.
 * 
 * @author kec
 * @author darmbrust
 */
public class UuidToIntHashMapBinder extends TupleBinding<UuidToIntHashMap> {

	public final static int CHUNK_SIZE = 50000000;
	private static final Logger logger = Logger.getLogger(UuidToIntHashMapBinder.class.getName());
	
	private int lastProcessedChunk_ = -1;
	private int nextChunk = 0;
	private boolean needToWriteBack = false;
	private UuidToIntHashMap result_ = null;

	/**
	 * Check and see if another round of read or write needs to be done.
	 * automatically resets the internal flags when it returns false - to prep for the next call.
	 * 
	 * If this returns false - the next call will return true due to this autoreset.
	 */
	private boolean moreLeftToProcess() {
		if (nextChunk > lastProcessedChunk_) {
			return true;
		} else {
			// reset flags
			lastProcessedChunk_ = -1;
			nextChunk = 0;
			return false;
		}
	}

	/**
	 * Read the {@link UuidToIntHashMap} from the specified DB, handling chunked data, if necessary.
	 */
	public UuidToIntHashMap read(Database database) {
		int readID = 0;
		
		while (moreLeftToProcess()) {
			DatabaseEntry theKey = new DatabaseEntry();
			IntegerBinding.intToEntry(readID++, theKey);

			DatabaseEntry theData = new DatabaseEntry();
			if (database.get(null, theKey, theData, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {
				result_ = entryToObject(theData);
			} else {
				result_ = new UuidToIntHashMap();
				break;
			}
		}

		if (needToWriteBack) {
			logger.fine("Performing writeback of desired format");
			result_.trimToSize();  //Don't write out stuff we don't need to write...
			write(database, result_);
		}

		logger.info("Finished reading the UuidToIntHashMap from the DB - read " + result_.size() + " distinct entries");
		UuidToIntHashMap temp = result_;
		result_ = null;
		return temp;
	}
	
	/**
	 * Write a {@link UuidToIntHashMap} to the specified DB, chunking the data, if necessary.
	 */
	public void write(Database database, UuidToIntHashMap map)
	{
		logger.fine("Writing to DB - " + map.size());
		int writeId = 0;
		while (moreLeftToProcess()) {
			DatabaseEntry theKey = new DatabaseEntry();
			IntegerBinding.intToEntry(writeId++, theKey);

			DatabaseEntry valueEntry = new DatabaseEntry();
			objectToEntry(map, valueEntry);
			database.put(null, theKey, valueEntry);
		}
		logger.info("Finished writing the UuidToIntHashMap to the DB - wrote " + map.size() + " distinct entries");
	}

	@Override
	public UuidToIntHashMap entryToObject(TupleInput input) {
		if (nextChunk == 0) {
			logger.finer("Reading chunk 0");
			int type = input.readInt();
			if (type == DB_TYPE.READ_ONLY.getInt()) {
				int length = input.readInt();
				int distinct = input.readInt();
				logger.finer("total expected size " + length + " total distinct " + distinct );
				UuidToIntHashMap map = new UuidToIntHashMap(length);
				map.resetMetadata(distinct);

				for (int i = 0; i < length; i++) {
					map.values[i] = input.readInt();
					map.state[i] = input.readByte();
				}

				length = input.readInt();
				logger.finer("Reading 'table' entries: " + length);
				for (int i = 0; i < length; i++) {
					map.table[i] = input.readLong();
				}
				boolean hasMoreChunks = input.readBoolean();
				lastProcessedChunk_ = nextChunk;
				if (hasMoreChunks) {
					logger.finer("More to read");
					nextChunk++;
				}
				return map;
			} else {
				// read what is actually a {@link UuidIntConcurrentHashMapBinder} format
				int length = input.readInt();
				logger.finer("Reading " + length + " items from UuidIntConcurrentHashMapBinder format");
				UuidToIntHashMap map = new UuidToIntHashMap(length);
				for (int i = 0; i < length; i++) {
					long lsb = input.readLong();
					long msb = input.readLong();
					int nid = input.readInt();
					map.put(new UUID(msb, lsb), nid);
				}
				lastProcessedChunk_ = nextChunk;
				needToWriteBack = true;
				return map;
			}
		} else {
			logger.finer("Reading chunk " + nextChunk);
			int length = input.readInt();
			logger.finer("'table' entries in this chunk: " + length);
			
			// In chunks greater than 0, we only have table entries stored... put them into the UuidToIntHashMap
			// we created upon reading chunk 0.
			int offsetIndex = nextChunk * CHUNK_SIZE;
			
			for (int i = 0; i < length; i++) {
				result_.table[offsetIndex++] = input.readLong();
			}
			boolean hasMoreChunks = input.readBoolean();
			lastProcessedChunk_ = nextChunk;
			if (hasMoreChunks) {
				logger.finer("More to read");
				nextChunk++;
			}
			return result_;
		}
	}

	@Override
	public void objectToEntry(UuidToIntHashMap map, TupleOutput output) {
		if (nextChunk == 0) {
			logger.finer("Writing chunk 0");
			output.writeInt(DB_TYPE.READ_ONLY.getInt());
			output.writeInt(map.values.length);
			output.writeInt(map.size());  //Write out 'distinct'
			for (int i = 0; i < map.values.length; i++) {
				output.writeInt(map.values[i]);
				output.writeByte(map.state[i]);
			}
			logger.finer("Total 'table' entries to write is " + map.table.length);
		}
		
		int start = CHUNK_SIZE * nextChunk;
		int end = ((map.table.length - start) > CHUNK_SIZE ? (start + CHUNK_SIZE) : map.table.length);
		logger.finer("Writing entries " + start + " - " + end + " this chunk has " + (end - start) + " items");
		output.writeInt(end - start);
		for (int i = start; i < end; i++) {
			output.writeLong(map.table[i]);
		}
		lastProcessedChunk_ = nextChunk;
		if (map.table.length > end) {
			// write a marker telling it to read another chunk
			output.writeBoolean(true);
			nextChunk++;
			logger.finer("Still more to write");
		} else {
			// write a marker telling it no more chunks
			output.writeBoolean(false);
			logger.finer("All written");
		}
	}
}