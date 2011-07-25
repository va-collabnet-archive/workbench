package org.ihtsdo.rf2.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

public class WriteUtil {

	private static Logger logger = Logger.getLogger(WriteUtil.class);

	private static int writeCount = 0;

	public static void init() {
		setWriteCount(0);
	}

	public static BufferedWriter createWriter(String fileName) throws UnsupportedEncodingException, FileNotFoundException {

		FileOutputStream os = new FileOutputStream(new File(fileName));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF8"), (1 * 1024));

		return bw;
	}

	public static void closeWriter(BufferedWriter bw) {
		if (bw != null)
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public static void write(Config config, String str) throws IOException {

		if (str.equals("\\r\\n"))
			writeNewLine(config, str);
		else if (str.equals("\\t"))
			writeTab(config, str);
		else
			config.getBw().write(str);
	}

	public static void writeTab(Config config, String str) throws IOException {
		config.getBw().write("\t");
	}

	public static void writeNewLine(Config config, String str) throws IOException {

		// config.getBw().write("\r\n");
		config.getBw().newLine();

		int count = getWriteCount();
		setWriteCount(++count);

		if (getWriteCount() % config.getFlushCount() == 0) {
			config.getBw().flush();

			if (logger.isDebugEnabled())
				logger.debug("Flushing line no. " + getWriteCount());
		}
	}

	public static int getWriteCount() {
		return writeCount;
	}

	public static void setWriteCount(int writeCount) {
		WriteUtil.writeCount = writeCount;
	}
}
