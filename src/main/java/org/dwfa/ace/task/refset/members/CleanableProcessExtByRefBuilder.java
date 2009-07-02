package org.dwfa.ace.task.refset.members;

import java.io.File;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.select.DescriptionSelector;
import org.dwfa.ace.task.util.Logger;

public interface CleanableProcessExtByRefBuilder {

    CleanableProcessExtByRefBuilder withSelectedDir(File selectedDirectory);

    CleanableProcessExtByRefBuilder withLogger(Logger logger);

    CleanableProcessExtByRef build();

    CleanableProcessExtByRefBuilder withTermFactory(I_TermFactory termFactory);

    CleanableProcessExtByRefBuilder withLanguagePreference(DescriptionSelector descriptionSelector);
    
}
