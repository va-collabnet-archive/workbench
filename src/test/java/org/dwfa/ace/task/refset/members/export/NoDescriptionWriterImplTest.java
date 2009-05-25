package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.util.Logger;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.io.Writer;
import java.util.Arrays;
import java.util.UUID;

public final class NoDescriptionWriterImplTest {

    private static final String LINE_SEPARATOR  = "\n";
    private static final String SOME_TEXT       = "Blah blalh";

    private IMocksControl mockControl;
    private Writer mockWriter;
    private Logger mockLogger;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockWriter = mockControl.createMock(Writer.class);
        mockLogger = mockControl.createMock(Logger.class);
    }
    
    @Test
    public void shouldWriteConceptsThatDontHaveADescritpion() throws Exception {
        I_GetConceptData mockConcept = mockControl.createMock(I_GetConceptData.class);
        UUID randomUUID = UUID.randomUUID();
        EasyMock.expect(mockConcept.getUids()).andReturn(Arrays.asList(randomUUID));
        mockLogger.logWarn("Concept " + randomUUID + " has no active preferred term");
        EasyMock.expect(mockWriter.append("Concept ")).andReturn(mockWriter);
        EasyMock.expect(mockWriter.append(randomUUID.toString())).andReturn(mockWriter);
        EasyMock.expect(mockWriter.append(" has no active preferred term")).andReturn(mockWriter);
        EasyMock.expect(mockWriter.append(LINE_SEPARATOR)).andReturn(mockWriter);
        mockControl.replay();

        NoDescriptionWriter writer = new NoDescriptionWriterImpl(mockWriter, LINE_SEPARATOR, mockLogger);
        writer.write(mockConcept);

        mockControl.verify();
    }

    @Test
    public void shouldAppendSuppliedText() throws Exception {
        Logger mockLogger = mockControl.createMock(Logger.class);
        EasyMock.expect(mockWriter.append(SOME_TEXT)).andReturn(mockWriter);
        mockControl.replay();

        NoDescriptionWriter writer = new NoDescriptionWriterImpl(mockWriter, LINE_SEPARATOR, mockLogger);
        writer.append(SOME_TEXT);

        mockControl.verify();
    }

    @Test
    public void shouldCloseFiles() throws Exception {
        mockWriter.flush();
        mockWriter.close();
        mockControl.replay();

        NoDescriptionWriter writer = new NoDescriptionWriterImpl(mockWriter, LINE_SEPARATOR, mockLogger);
        writer.close();

        mockControl.verify();
    }

}
