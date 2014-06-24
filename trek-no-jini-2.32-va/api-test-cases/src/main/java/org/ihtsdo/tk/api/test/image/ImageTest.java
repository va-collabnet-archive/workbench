package org.ihtsdo.tk.api.test.image;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.blueprint.MediaCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.media.MediaAnalogBI;
import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.test.ConceptHelper;
import org.ihtsdo.tk.api.test.DefaultProfileBuilder;
import org.ihtsdo.tk.api.test.NewConceptBuilder;
import org.intsdo.junit.bdb.BdbTestRunner;
import org.intsdo.junit.bdb.BdbTestRunnerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig(init = { DefaultProfileBuilder.class, NewConceptBuilder.class })
public class ImageTest {

    private static final String UUID_VIEWER_IMAGE = "5f5be40f-24c1-374f-bd04-4a5003e366ea";
    private static final String UUID_REFERENCE_SET = "7e38cd2d-6f1a-3a81-be0b-21e6090573c2";

    private static boolean propertyChangeListenerFired = false;

    @Test
    public void createImage() throws Exception {
        ConceptChronicleBI viewerImage = Ts.get().getConcept(UUID.fromString(UUID_VIEWER_IMAGE));
        ConceptChronicleBI referenceSet = Ts.get().getConcept(UUID.fromString(UUID_REFERENCE_SET));

        ConceptChronicleBI concept = ConceptHelper.createNewConcept("concept (test concept)", "refset",
            referenceSet.getNid());

        MediaCAB imageCab = new MediaCAB(concept.getConceptNid(), viewerImage.getNid(), "image",
            "a textual description", "a pretend image".getBytes());

        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
        TerminologyBuilderBI ammender = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
            config.getViewCoordinate());
        MediaChronicleBI mediaChronicle = ammender.constructIfNotCurrent(imageCab);

        Ts.get().addUncommitted(concept);

        Collection<? extends MediaChronicleBI> members = concept.getMedia();
        assertEquals(1, members.size());
        MediaAnalogBI mediaAnalog = MediaAnalogBI.class.cast(members.iterator().next());

        exerciseMediaAnalogInterface(mediaAnalog);

        assertTrue(mediaChronicle.getUUIDs().get(0).equals(mediaAnalog.getUUIDs().get(0)));

        Set<Integer> nids = mediaAnalog.getAllNidsForVersion();
        assertTrue(nids.contains(mediaAnalog.getNid()));
        for (PathBI path : config.getEditingPathSet()) {
            assertTrue(nids.contains(path.getConceptNid()));
        }
        assertTrue(nids.contains(mediaAnalog.getAuthorNid()));
        assertTrue(nids.contains(mediaAnalog.getPathNid()));

        Collection<? extends RefexVersionBI> annotations = mediaAnalog.getAnnotationsActive(config.getViewCoordinate());
        assertEquals(0, annotations.size());
        assertEquals(0, mediaAnalog.getAnnotations().size());

        ComponentChronicleBI<?> chronicle = mediaAnalog.getChronicle();
        assertEquals(chronicle.getNid(), mediaAnalog.getNid());
        assertEquals(chronicle.getPrimUuid(), mediaAnalog.getUUIDs().get(0));
        assertEquals(1, chronicle.getVersions(config.getViewCoordinate()).size());

        assertTrue(mediaAnalog.getEnclosingConcept().equals(concept));
        
        modifyMedia(mediaChronicle);
    }

    private static void exerciseMediaAnalogInterface(MediaAnalogBI mediaAnalog) throws Exception {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        PositionBI pos = mediaAnalog.getPosition();
        assertEquals(mediaAnalog.getPathNid(), pos.getPath().getConceptNid());
        assertEquals(1, mediaAnalog.getPositions().size());
        assertTrue(mediaAnalog.getPositions().contains(pos));

        Collection<? extends IdBI> additionalIds = mediaAnalog.getAdditionalIds();
        // TODO FIX assertTrue(additionalIds != null);

        Collection<? extends IdBI> allIds = mediaAnalog.getAllIds();
        assertEquals(1, allIds.size());
        assertEquals(allIds.iterator().next().getDenotation(), mediaAnalog.getUUIDs().get(0));

        Set<Integer> stampNids = mediaAnalog.getAllStampNids();
        assertEquals(1, stampNids.size());
        assertTrue(stampNids.contains(mediaAnalog.getStampNid()));

        assertEquals(0, mediaAnalog.getRefexes().size());
        assertEquals(0, mediaAnalog.getRefexesActive(config.getViewCoordinate()).size());
        assertEquals(0, mediaAnalog.getRefexesInactive(config.getViewCoordinate()).size());
    }

    private static void modifyMedia(MediaChronicleBI media) throws Exception {
        I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

        CreateOrAmendBlueprint cab = media.getVersion(config.getViewCoordinate()).makeBlueprint(
            config.getViewCoordinate());
        if (!MediaCAB.class.isAssignableFrom(cab.getClass())) {
            assertTrue(false);
        }

        MediaCAB bp = MediaCAB.class.cast(cab);
        int authorityNid = media.getAllIds().iterator().next().getAuthorityNid();
        bp.addExtraUuid(UUID.randomUUID(), authorityNid);
        bp.addLongId(new Random().nextLong(), authorityNid);

        bp.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                propertyChangeListenerFired = true;
            }
        });
        propertyChangeListenerFired = false;
        bp.setComponentUuid(UUID.randomUUID());
        assertTrue(propertyChangeListenerFired);

        bp.addStringId("string-id", authorityNid);

        TerminologyBuilderBI ammender = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
            config.getViewCoordinate());
        MediaChronicleBI modifiedRefex = ammender.construct(bp);

    }

}
