package org.dwfa.ace.task.refset.members.export;

import static junit.framework.Assert.fail;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.dwfa.ace.task.util.Logger;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class WriterFactoryImplTest {

    private IMocksControl mockControl;
    private Logger mockLogger;
    private I_TermFactory mockTermFactory;
    private RefsetUtil mockRefsetUtil;

    @Before
    public void setup() {
        mockControl = EasyMock.createControl();
        mockLogger = mockControl.createMock(Logger.class);
        mockTermFactory = mockControl.createMock(I_TermFactory.class);
        mockRefsetUtil = mockControl.createMock(RefsetUtil.class);
    }

    @Test
    public void shouldThrowAnExceptionIfTheOutputDirectoryIsNotSupplied() {
        mockControl.replay();
        try {
            new WriterFactoryImpl(null, mockLogger, mockTermFactory, mockRefsetUtil);
            fail();
        } catch (InvalidOutputDirectoryException e) {
            assertThat("The output directory supplied is null.", equalTo(e.getMessage()));
            mockControl.verify();
        }
    }
}
