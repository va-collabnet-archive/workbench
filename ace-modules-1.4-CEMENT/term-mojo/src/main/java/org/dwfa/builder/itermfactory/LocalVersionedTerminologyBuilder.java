/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.builder.itermfactory;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.builder.Builder;
import org.dwfa.builder.BuilderException;

/**
 * The {@code LocalVersionedTerminologyBuilder} implementation of {@link Builder} encapsulates the logic to instantiate
 * an instance of {@link LocalVersionedTerminology} Singelton with the specified parameters.
 * <p>
 * For Example:
 * <pre>
 * {@code Builder<I_TermFactory> builder = new LocalVersionedTerminologyBuilder(vodbDirectory, readOnly, cacheSize,
 *                                          useExistingDb, dbSetupConfig);
 *
 * I_TermFactory localVersionedTerminology = builder.build();}
 * </pre>
 * </p>
 * @author Matthew Edwards
 */
public final class LocalVersionedTerminologyBuilder implements Builder<I_TermFactory> {

    /**
     * Message for Factory status messages.
     */
    private static final String CONNECTION_LOG_MESSAGE = "%1$s to LocalVersionedTerminology... %2$s";
    /**
     * Default Database Cache Size.
     */
    public static final Long DEFAULT_CACHE_SIZE = 600000000L;
    /**
     * {@code Logger} instance for logging status messages.
     */
    private final Logger logger;
    /**
     * Location of the vodb directory.
     *
     * Only required if {@code useExistingDb} is null or set to false
     */
    private final File vodbDirectory;
    /**
     * True if the database is readonly.
     *
     */
    private final boolean readOnly;
    /**
     * Size of cache used by the database.
     * @see LocalVersionedTerminology#createFactory
     * (java.io.File, boolean, java.lang.Long, org.dwfa.ace.api.DatabaseSetupConfig, boolean)
     */
    private final Long cacheSize;
    /**
     * When {@code true} use existing LocalVersionedTerminology, otherwise create a new one.
     * @see LocalVersionedTerminology#createFactory
     * (java.io.File, boolean, java.lang.Long, org.dwfa.ace.api.DatabaseSetupConfig, boolean)
     */
    private final boolean isUseExistingDb;
    /**
     * {@link DatabaseSetupConfig} containing the database configuration to be used when instantiating the
     * {@link LocalVersionedTerminology}.
     */
    private final DatabaseSetupConfig dbSetupConfig;

    /**
     * Creates an instance of {@code LocalVersionedTerminologyBuilder} with the specified parameters.
     * @param vodbDirectory the directory where the database can be found.
     * @param readOnly whether to create a read only connection.
     * @param cacheSize the connection cache size. Default {@link #DEFAULT_CACHE_SIZE} = {@code 600000000L}.
     * If {@code cacheSize} is null, the default will be used.
     * @param isUseExistingDb whether we should reconnect to an existing db if one exists.
     * @param dbSetupConfig Database Configuration Value Object containing DB Connection Parameters.
     * If {@code dbSetupConfig} is null, default Connection parameters will be used.
     * @see DatabaseSetupConfig
     */
    public LocalVersionedTerminologyBuilder(final File vodbDirectory, final boolean readOnly, final Long cacheSize,
            final boolean isUseExistingDb, final DatabaseSetupConfig dbSetupConfig) {
        this.logger = Logger.getLogger(LocalVersionedTerminologyBuilder.class.getName());
        this.vodbDirectory = vodbDirectory;
        this.readOnly = readOnly;

        if (cacheSize == null) {
            this.cacheSize = DEFAULT_CACHE_SIZE;
        } else {
            this.cacheSize = cacheSize;
        }

        this.isUseExistingDb = isUseExistingDb;


        if (dbSetupConfig == null) {
            this.dbSetupConfig = new DatabaseSetupConfig();
        } else {
            this.dbSetupConfig = dbSetupConfig;
        }
    }

    @Override
    public I_TermFactory build() throws BuilderException {

        //If there is an existing database and we should useExistingDb, reconnect to the existing one.
        if (LocalVersionedTerminology.get() != null && isUseExistingDb) {
            reconnectToExistingLocalVersionedTerminology();
        } else {
            getNewConnectionToLocalVersionedTerminology();
        }

        I_TermFactory factory = LocalVersionedTerminology.get();

        return factory;
    }

    /**
     * Utility method to connect to an existing {@link LocalVersionedTerminology}
     * @throws BuilderException if there are any problems connecting to the LocalVersionedTerminology
     */
    private void reconnectToExistingLocalVersionedTerminology() throws BuilderException {
        logger.info(String.format(CONNECTION_LOG_MESSAGE, "Reconnecting", ""));
        try {
            LocalVersionedTerminology.createFactory(vodbDirectory,
                    readOnly,
                    cacheSize,
                    dbSetupConfig,
                    isUseExistingDb);
        } catch (Exception ex) {
            logger.severe(String.format(CONNECTION_LOG_MESSAGE, "Reconnecting", "[FAILED]"));
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            throw new BuilderException(ex.getMessage(), ex);
        }
        logger.info(String.format(CONNECTION_LOG_MESSAGE, "Reconnected", "[OK]"));
    }

    /**
     * Utility method to connect to a new instance of {@link LocalVersionedTerminology}
     * @throws BuilderException if there are any problems connecting to the LocalVersionedTerminology
     */
    private void getNewConnectionToLocalVersionedTerminology() throws BuilderException {
        logger.info(String.format(CONNECTION_LOG_MESSAGE, "Connecting", ""));
        try {
            LocalVersionedTerminology.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
        } catch (Exception ex) {
            logger.severe(String.format(CONNECTION_LOG_MESSAGE, "Connecting", "[FAILED]"));
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            throw new BuilderException(ex.getMessage(), ex);
        }
        logger.info(String.format(CONNECTION_LOG_MESSAGE, "Connected", "[OK]"));
    }
}
