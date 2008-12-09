package org.dwfa.mojo.memrefset.mojo;

import com.thoughtworks.xstream.XStream;
import org.dwfa.mojo.relformat.mojo.sql.io.util.FileUtilImpl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

public final class CmrscsXMLReaderImpl implements CmrscsXMLReader {

    private XStream xStream;
    private FileUtilImpl fileUtil;

    public CmrscsXMLReaderImpl() {
        fileUtil = new FileUtilImpl();
        xStream = new CmrscsResultXStreamConfigImpl().configure();
    }

    public CmrscsResult read(final String fileName) {
        Reader reader = open(fileName);
        CmrscsResult cmrscsResult = (CmrscsResult) xStream.fromXML(reader);
        fileUtil.closeSilently(reader);
        return cmrscsResult;
    }

    private Reader open(final String fileName) {
        try {
            return new BufferedReader(new FileReader(fileName));
        } catch (Exception e) {
            throw new CmrscsXMLReaderException(e); 
        }
    }
}
