package org.dwfa.mojo.memrefset.mojo;

import com.thoughtworks.xstream.XStream;

public final class CmrscsResultToXMLConverterImpl implements CmrscsResultToXMLConverter {

    private final XStream xStream;

    public CmrscsResultToXMLConverterImpl() {
        xStream = new CmrscsResultXStreamConfigImpl().configure();
    }

    public String convert(final CmrscsResult cmrscsResult) {
        return xStream.toXML(cmrscsResult);        
    }
}
