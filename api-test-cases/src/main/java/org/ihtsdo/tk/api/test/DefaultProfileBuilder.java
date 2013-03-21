package org.ihtsdo.tk.api.test;

import java.io.IOException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.contradiction.LastCommitWinsContradictionResolutionStrategy;
import org.intsdo.junit.bdb.BdbTestInitialiser;

/**
 * Creates and activates a new default profile using a popular user configuration.
 */
public class DefaultProfileBuilder implements BdbTestInitialiser {

    @Override
    public void init() {
        try {
            
            System.out.println("Initialising Default Profile...");
            
            String aceUsername = "username.ace";
            I_ConfigAceFrame profile = NewDefaultProfile.newProfile(aceUsername, aceUsername, null, "admin", "visit.bend");
            profile.setConflictResolutionStrategy(new LastCommitWinsContradictionResolutionStrategy());
            profile.setPrecedence(Precedence.TIME);
            profile.getViewPositionSet().clear();
            profile.setLanguageSortPref(I_ConfigAceFrame.LANGUAGE_SORT_PREF.LANG_REFEX);
            profile.getLanguagePreferenceList().clear();
            
            I_TermFactory termFactory = Terms.get();
            PathBI editPath = termFactory.getPath(Concept.ARCHITECTONIC_BRANCH.getUids());
            profile.addEditingPath(editPath);
            
            termFactory.setActiveAceFrameConfig(profile);
            
        } catch (TerminologyException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
