package org.ihtsdo.tk.api.test.path;

import static junit.framework.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import junit.framework.Assert;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.PathCB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.test.ConceptHelper;
import org.ihtsdo.tk.api.test.DefaultProfileBuilder;
import org.ihtsdo.tk.api.test.NewConceptBuilder;
import org.ihtsdo.tk.api.test.TestArtefact;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.intsdo.junit.bdb.BdbTestRunner;
import org.intsdo.junit.bdb.BdbTestRunnerConfig;
import org.intsdo.junit.bdb.DependsOn;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig(init = { DefaultProfileBuilder.class, NewConceptBuilder.class })
public class NewPathTest {

    @Test
    public void createPath() throws Exception {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
        I_TermFactory termFactory = Terms.get();

        ConceptChronicleBI storedConcept = TestArtefact.get(NewConceptBuilder.ARTEFACT_KEY, ConceptChronicleBI.class);

        PathBI workbenchPath = termFactory.getPath(Concept.ARCHITECTONIC_BRANCH.getUids());
        ArrayList<PositionBI> origins = new ArrayList<>();
        PositionBI workbenchPosition = termFactory.newPosition(workbenchPath, Long.MAX_VALUE);
        origins.add(workbenchPosition);
        termFactory.newPath(origins, termFactory.getConcept(storedConcept.getNid()), config);
    }

    @Test
    @Ignore // Path Blueprint not yet supported
    public void createPathByBlueprints() throws Exception {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
        TerminologyStoreDI termStore = Ts.get();
        TerminologyBuilderBI builder = 
                termStore.getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());
        
        int parentConceptNid = termStore.getComponent(UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d")).getNid();
        ConceptChronicleBI parentConcept = termStore.getConcept(parentConceptNid);
        ConceptChronicleBI relTypeConcept = Snomed.IS_A.getLenient();
        LANG_CODE langCode = LANG_CODE.EN; // EN_AU not supported!
        String fsnTerm = String.format("New Path %d (test concept)", System.currentTimeMillis());
        String prefTerm = "New Path";

        ConceptCB conceptBp = 
                new ConceptCB(fsnTerm, prefTerm, langCode, relTypeConcept.getPrimUuid(), parentConcept.getPrimUuid());
        
        conceptBp.setComponentUuid(UUID.randomUUID());
        
        UUID originPathUuid = Concept.ARCHITECTONIC_BRANCH.getPrimoridalUid();
        
        RefexCAB pathRefexBp = new RefexCAB(TK_REFEX_TYPE.CID, TermAux.PATH.getLenient().getConceptNid(),
            RefsetAux.PATH_REFSET.getLenient().getNid());
        pathRefexBp.put(RefexCAB.RefexProperty.UUID1, conceptBp.getComponentUuid());
        pathRefexBp.setMemberUuid(UUID.randomUUID());

        RefexCAB pathOriginRefexBp = new RefexCAB(TK_REFEX_TYPE.CID_INT, conceptBp.getComponentUuid(),
            RefsetAux.PATH_ORIGIN_REFEST.getLenient().getNid(), null, null);
        pathOriginRefexBp.put(RefexCAB.RefexProperty.UUID1, originPathUuid);
        pathOriginRefexBp.put(RefexCAB.RefexProperty.INTEGER1, Integer.MAX_VALUE);
        pathRefexBp.setMemberUuid(UUID.randomUUID());

        // FIXME
        UUID originFromDevPathUuid = UUID.fromString("unknown origin path"); 
        
        RefexCAB pathOriginRefexOtherBp = new RefexCAB(TK_REFEX_TYPE.CID_INT, originFromDevPathUuid,
            RefsetAux.PATH_ORIGIN_REFEST.getLenient().getNid(), null, null);
        pathOriginRefexOtherBp.put(RefexCAB.RefexProperty.UUID1, conceptBp.getComponentUuid());
        pathOriginRefexOtherBp.put(RefexCAB.RefexProperty.INTEGER1, Integer.MAX_VALUE);
        pathOriginRefexOtherBp.setMemberUuid(UUID.randomUUID());

        PathCB pathBp = new PathCB(conceptBp, pathRefexBp, pathOriginRefexBp, pathOriginRefexOtherBp, Ts.get().getConcept(originPathUuid));
        PathBI editPath = builder.construct(pathBp);
    }

    @Test
    @DependsOn("createPath")
    public void usePath() throws Exception {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
        I_TermFactory termFactory = Terms.get();

        PathBI path = termFactory.getPath(TestArtefact.get("conceptUuid", UUID.class));
        PathBI path2 = Ts.get().getPath(path.getConceptNid());
        Assert.assertEquals(path, path2);

        EditCoordinate editCoordinate = config.getEditCoordinate();
        NidSetBI editPaths = editCoordinate.getEditPathsSet();
        editPaths.clear();
        editPaths.add(path.getConceptNid());
        
        int parentConceptNid = Ts.get().getComponent(UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d")).getNid();
        ConceptChronicleBI newConceptOnPath = ConceptHelper.createNewConcept("Path Test Concept" + System.currentTimeMillis(), "Path Test Concept", parentConceptNid);
        Collection<? extends ConceptVersionBI> versions = newConceptOnPath.getVersions();
        assertEquals(1, versions.size());
        for (ConceptVersionBI version : versions) {
            assertEquals(path.getConceptNid(), version.getPathNid());
        }
    }
}
