package org.ihtsdo.rf2.factory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.util.Column;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;

public abstract class RF2AbstractFactory {

	private static Config config;

	private static Logger logger = Logger.getLogger(RF2AbstractFactory.class);

	public RF2AbstractFactory(Config config) {
		super();

		RF2AbstractFactory.config = config;

		setBufferedWriter();
	}

	public abstract void export() throws IOException, Exception;

	private static void writeHeader() throws IOException {

		ArrayList<Column> columnsList = config.getColumn();

		for (int i = 0; i < columnsList.size() - 1; i++) {
			Column column = columnsList.get(i);
			WriteUtil.write(config, column.getName());
			WriteUtil.write(config, column.getDelimiter());
		}

		Column column = columnsList.get(columnsList.size() - 1);
		WriteUtil.write(config, column.getName());
		WriteUtil.write(config, column.getDelimiter());
	}

	public static void setBufferedWriter() {

		try {

			String outputFolderName = config.getOutputFolderName();

			File folder = new File(outputFolderName);

			if (!folder.exists())
				folder.mkdir();

			String exportFileName = config.getExportFileName();

			exportFileName += config.getReleaseDate() + "." + config.getFileExtension();

			BufferedWriter bw = WriteUtil.createWriter(outputFolderName + "/" + exportFileName);

			getConfig().setBw(bw);
			writeHeader();

		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void closeExportFileWriter() throws IOException {
		WriteUtil.closeWriter(getConfig().getBw());
	}

	public static Config getConfig() {
		return config;
	}
}
