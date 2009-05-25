package org.dwfa.ace.task.refset.members;

import org.dwfa.ace.task.util.Logger;
import org.dwfa.ace.api.I_TermFactory;

import java.io.File;

public interface CleanableProcessExtByRefBuilder {

    CleanableProcessExtByRefBuilder withSelectedDir(File selectedDirectory);

    CleanableProcessExtByRefBuilder withLogger(Logger logger);

    CleanableProcessExtByRef build();

    CleanableProcessExtByRefBuilder withTermFactory(I_TermFactory termFactory);
}
