package org.dwfa.ace.task.refset.members;

import java.io.File;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.select.DescriptionSelector;
import org.dwfa.ace.task.refset.members.export.CommonAPIParameterObjectImpl;
import org.dwfa.ace.task.refset.members.export.ProgressLoggerImpl;
import org.dwfa.ace.task.refset.members.export.RefsetExportValidatorImpl;
import org.dwfa.ace.task.refset.members.export.RefsetTextWriterImpl;
import org.dwfa.ace.task.refset.members.export.RefsetWriterImpl;
import org.dwfa.ace.task.refset.members.export.RefsetWriterParameterObjectImpl;
import org.dwfa.ace.task.refset.members.export.WriterFactoryImpl;
import org.dwfa.ace.task.util.Logger;

public final class WriteRefsetDescriptionsProcessExtByRefBuilder implements CleanableProcessExtByRefBuilder {
    
    private Logger logger;
    private File selectedDirectory;
    private I_TermFactory termFactory;
    private DescriptionSelector descSelector;

    public CleanableProcessExtByRefBuilder withSelectedDir(final File selectedDirectory) {
        this.selectedDirectory = selectedDirectory;
        return this;
    }

    public CleanableProcessExtByRefBuilder withLogger(final Logger logger) {
        this.logger = logger;
        return this;
    }

    public CleanableProcessExtByRefBuilder withTermFactory(final I_TermFactory termFactory) {
        this.termFactory = termFactory;
        return this;
    }

    public CleanableProcessExtByRefBuilder withLanguagePreference(DescriptionSelector descSelector) {
        this.descSelector = descSelector;
        return this;
    }
    
    public CleanableProcessExtByRef build() {
        RefsetUtil refsetUtil = new RefsetUtilImpl();        
        return new WriteRefsetDescriptionsProcessExtByRef(
        		new RefsetExportValidatorImpl(),
                new RefsetWriterImpl(
                		new RefsetWriterParameterObjectImpl(
	                        new ProgressLoggerImpl(logger),
	                        new RefsetTextWriterImpl(refsetUtil, termFactory),
	                        new WriterFactoryImpl(selectedDirectory, logger, termFactory, refsetUtil)
	                    ),
	                    new CommonAPIParameterObjectImpl(refsetUtil, termFactory, logger),
	                    descSelector
                ), 
	            refsetUtil, termFactory, logger);
    }

}
