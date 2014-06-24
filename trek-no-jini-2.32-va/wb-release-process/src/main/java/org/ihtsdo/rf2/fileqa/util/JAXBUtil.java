package org.ihtsdo.rf2.fileqa.util;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.ihtsdo.rf2.fileqa.model.MessageType;
import org.ihtsdo.rf2.fileqa.model.Metadata;

public class JAXBUtil {

	public static Metadata getMetadata(String metaDataFile, WriteExcel writeExcel) {

		Metadata qa = null;
		try {

			JAXBContext context = JAXBContext.newInstance(Metadata.class);
			Unmarshaller u = context.createUnmarshaller();

			InputStream is = JAXBUtil.class.getResourceAsStream(metaDataFile);

			qa = (Metadata) u.unmarshal(is);

		} catch (JAXBException e) {
			writeExcel.addRow(MessageType.FAILURE,
					"MetadataTest,Current,Failed, ," + metaDataFile + " :"
							+ e.getMessage());
		} catch (Exception e) {
			writeExcel.addRow(MessageType.FAILURE,
					"MetadataTest,Current,Failed, ," + metaDataFile + " :"
							+ "MetaData file missing or error :" + e.getMessage());
		}
		return qa;
	}
}
