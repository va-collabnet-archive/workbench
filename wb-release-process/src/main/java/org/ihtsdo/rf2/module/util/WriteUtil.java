package org.ihtsdo.rf2.module.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class WriteUtil.
 */
public class WriteUtil {

	/** The logger. */
	private static Logger logger = Logger.getLogger(WriteUtil.class);

	/** The write count. */
	private static int writeCount = 0;

	/**
	 * Inits the.
	 */
	public static void init() {
		setWriteCount(0);
	}

	/**
	 * Creates the writer.
	 *
	 * @param fileName the file name
	 * @return the buffered writer
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws FileNotFoundException the file not found exception
	 */
	public static BufferedWriter createWriter(String fileName) throws UnsupportedEncodingException, FileNotFoundException {

		FileOutputStream os = new FileOutputStream(new File(fileName));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF8"), (1 * 1024));

		return bw;
	}

	/**
	 * Close writer.
	 *
	 * @param bw the bw
	 */
	public static void closeWriter(BufferedWriter bw) {
		if (bw != null)
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * Write.
	 *
	 * @param config the config
	 * @param str the str
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void write(Config config, String str) throws IOException {

		if (str.equals("\\r\\n"))
			writeNewLine(config, str);
		else if (str.equals("\\t"))
			writeTab(config, str);
		else
			config.getBw().write(str);
	}

	/**
	 * Write tab.
	 *
	 * @param config the config
	 * @param str the str
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeTab(Config config, String str) throws IOException {
		config.getBw().write("\t");
	}

	/**
	 * Write new line.
	 *
	 * @param config the config
	 * @param str the str
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeNewLine(Config config, String str) throws IOException {

		 config.getBw().write("\r\n");
//		config.getBw().newLine();

		int count = getWriteCount();
		setWriteCount(++count);

		if (getWriteCount() % config.getFlushCount() == 0) {
			config.getBw().flush();

			if (logger.isDebugEnabled())
				logger.debug("Flushing line no. " + getWriteCount());
		}
	}

	/**
	 * Gets the write count.
	 *
	 * @return the write count
	 */
	public static int getWriteCount() {
		return writeCount;
	}

	/**
	 * Sets the write count.
	 *
	 * @param writeCount the new write count
	 */
	public static void setWriteCount(int writeCount) {
		WriteUtil.writeCount = writeCount;
	}
}
