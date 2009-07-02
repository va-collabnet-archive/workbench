package org.dwfa.ace.file;

import java.util.logging.Logger;

import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.refset.members.ImportRefsetFromLanguageSubsetFile;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.TerminologyRuntimeException;

/**
 * Implements IterableFileReader to convert each line of the file to a concept.<br>
 * Expected columns in the tab delimited file are:
 * <ul>
 * 	<li>1. Identifier (String) - may be any type of identifier, SCTID, UUID, etc.
 * 	<li>2. Description (String) - any valid description for the concept. Used to validate the ID is correct.
 * </ul>
 */
public class LanguageSubsetMemberReader extends IterableFileReader<LanguageSubsetMemberReader.LanguageSubsetMemberLine> {

    protected I_TermFactory termFactory = LocalVersionedTerminology.get();

    private Logger logger = Logger.getLogger(ImportRefsetFromLanguageSubsetFile.class.getName());

    private String languageSpecificateId;

    /**
     * @throws TerminologyRuntimeException if processing fails during iteration
     */
    @Override
    protected LanguageSubsetMemberLine processLine(String line) {
        LanguageSubsetMemberLine conceptDescription;

        try {
            String[] columns = line.split( "\t" );
            String currentLanguageSpecificateId = columns[0];
            String descriptionId = columns[1];
            String descriptionStatus = columns[2];

            if (currentLanguageSpecificateId.length() == 0 || descriptionId.length() == 0 || descriptionStatus.length() == 0) {
                throw new TerminologyException("Invalid file format");
            }

            validateLanguageSpecificateIdStr(currentLanguageSpecificateId);

            conceptDescription = new LanguageSubsetMemberLine(termFactory.getDescription(descriptionId), Integer.parseInt(descriptionStatus));
        } catch (IndexOutOfBoundsException ex) {
            logger.info("Invalid file format");
            throw new TerminologyRuntimeException("Invalid file format");
        } catch (Exception ex) {
            logger.info("Cannot process line:" + ex);
            throw new TerminologyRuntimeException(ex);
        }

        return conceptDescription;
    }

    /**
     * Validate that the Language Specification Id is the same as the last processed id.
     *
     * @param languageSpecificateIdStrToValidate String
     * @throws TerminologyRuntimeException if the Language Specification Id changes.
     */
    private void validateLanguageSpecificateIdStr(String languageSpecificateIdStrToValidate) throws TerminologyRuntimeException {
        if(languageSpecificateId == null){
            languageSpecificateId = languageSpecificateIdStrToValidate;
        }

        if (!languageSpecificateIdStrToValidate.equals(languageSpecificateId)) {
            throw new TerminologyRuntimeException("Language spcification file cannot contain multiple Language Specifications.");
        }
    }

    public class LanguageSubsetMemberLine {
        private I_DescriptionVersioned descriptionVersioned;
        private int descriptionStatusId;

        public LanguageSubsetMemberLine(I_DescriptionVersioned descriptionVersionedToSet, int descriptionStatusIdToSet) {
            descriptionVersioned = descriptionVersionedToSet;
            descriptionStatusId = descriptionStatusIdToSet;
        }

        public I_DescriptionVersioned getDescriptionVersioned() {
            return descriptionVersioned;
        }

        public int getDescriptionStatusId() {
            return descriptionStatusId;
        }

    }

}
