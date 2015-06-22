package org.ihtsdo.rf2.module.factory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.module.util.Column;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.WriteUtil;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating RF2Abstract objects.
 */
public abstract class RF2AbstractFactory {

	/** The config. */
	private static Config config;

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2AbstractFactory.class);

	/**
	 * Instantiates a new r f2 abstract factory.
	 *
	 * @param config the config
	 */
	public RF2AbstractFactory(Config config) {
		super();

		RF2AbstractFactory.config = config;

		setBufferedWriter();
	}

	/**
	 * Instantiates a new r f2 abstract factory.
	 */
	public RF2AbstractFactory() {
		
	}
	
	/**
	 * Export.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws Exception the exception
	 */
	public abstract void export() throws IOException, Exception;

	/**
	 * Write header.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Sets the buffered writer.
	 */
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

	/**
	 * Close export file writer.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void closeExportFileWriter() throws IOException {
		WriteUtil.closeWriter(getConfig().getBw());
	}

	/**
	 * Gets the config.
	 *
	 * @return the config
	 */
	public static Config getConfig() {
		return config;
	}

	/**
	 * Sets the config.
	 *
	 * @param recConfig the new config
	 */
	public static void setConfig(Config recConfig) {
		RF2AbstractFactory.config=recConfig;
	}
}
