package org.dwfa.mojo.memrefset.mojo;

import com.thoughtworks.xstream.XStream;

public final class CmrscsResultToXMLConverterImpl implements CmrscsResultToXMLConverter {

    private final XStream xStream;

    public CmrscsResultToXMLConverterImpl() {
        xStream = new XStream();
        xStream.alias("cmrscsResult", CmrscsResultImpl.class);
        xStream.alias("changeSet", ChangeSet.class);
        xStream.alias("refSet", RefSet.class);
    }

    public String convert(final CmrscsResult cmrscsResult) {
        return xStream.toXML(cmrscsResult);        
    }
}
