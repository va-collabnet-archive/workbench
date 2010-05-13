/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.edit;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.fd.FileDialogUtil;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.ThinImagePart;
import org.dwfa.vodb.types.ThinImageVersioned;

import com.sleepycat.je.DatabaseException;

public class AddImage extends AddComponent {

    public AddImage(I_ContainTermComponent termContainer, I_ConfigAceFrame config) {
        super(termContainer, config);
    }

    protected void doEdit(I_ContainTermComponent termContainer, ActionEvent e, I_ConfigAceFrame config)
            throws Exception {
        final File imageFile = FileDialogUtil.getExistingFile("Select image file to associate with concept", null,
            null, ((AceFrameConfig) config).getAceFrame());
        ConceptBean cb = (ConceptBean) termContainer.getTermComponent();
        int dotLoc = imageFile.getName().lastIndexOf('.');
        String format = imageFile.getName().substring(dotLoc + 1);
        if (format.length() > 5) {
            throw new Exception("Illegal format extension");
        }
        int idSource = LocalVersionedTerminology.get().uuidToNative(
            ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
        int nativeId = LocalVersionedTerminology.get().uuidToNativeWithGeneration(UUID.randomUUID(), idSource,
            config.getEditingPathSet(), Integer.MAX_VALUE);
        FileInputStream fis = new FileInputStream(imageFile);
        int size = (int) imageFile.length();
        byte[] image = new byte[size];
        int read = fis.read(image, 0, image.length);
        while (read != size) {
            size = size - read;
            read = fis.read(image, read, size);
        }
        List<I_ImagePart> parts = new ArrayList<I_ImagePart>(1);
        for (I_Path p : termContainer.getConfig().getEditingPathSet()) {
            ThinImagePart imagePart = new ThinImagePart();
            imagePart.setStatusId(config.getDefaultStatus().getConceptId());
            imagePart.setPathId(p.getConceptId());
            imagePart.setVersion(Integer.MAX_VALUE);
            imagePart.setTextDescription("");
            imagePart.setTypeId(config.getDefaultImageType().getConceptId());
            parts.add(imagePart);
        }

        ThinImageVersioned imageCore = new ThinImageVersioned(nativeId, image, parts, format, cb.getConceptId());

        cb.getUncommittedImages().add(imageCore);
        cb.getUncommittedIds().add(imageCore.getImageId());
        ACE.addUncommitted(cb);
        termContainer.setTermComponent(cb);
    }

    public static void addStockImages(VodbEnv vodb) throws DatabaseException, IOException, TerminologyException {

        I_Path aceAuxPath = new Path(Integer.MIN_VALUE + 1, new ArrayList<I_Position>());

        addStockImage(vodb, "1c4214ec-147a-11db-ac5d-0800200c9a66", "Semiotic Triangle with Circle",
            ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT, ArchitectonicAuxiliary.Concept.AUXILLARY_IMAGE,
            ".gif", "/Informatics-Circle-Small.gif", aceAuxPath);

        try {
            addStockImage(vodb, "70e86440-7f31-11dc-8314-0800200c9a66", "icon for included individual",
                RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL, ArchitectonicAuxiliary.Concept.VIEWER_IMAGE, ".png",
                "/16x16/plain/add.png", aceAuxPath);
            addStockImage(vodb, "70e86441-7f31-11dc-8314-0800200c9a66", "icon for included lineage",
                RefsetAuxiliary.Concept.INCLUDE_LINEAGE, ArchitectonicAuxiliary.Concept.VIEWER_IMAGE, ".png",
                "/16x16/plain/add2.png", aceAuxPath);
            addStockImage(vodb, "70e86442-7f31-11dc-8314-0800200c9a66", "icon for excluded individual",
                RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL, ArchitectonicAuxiliary.Concept.VIEWER_IMAGE, ".png",
                "/16x16/plain/delete.png", aceAuxPath);
            addStockImage(vodb, "70e86443-7f31-11dc-8314-0800200c9a66", "icon for excluded lineage",
                RefsetAuxiliary.Concept.EXCLUDE_LINEAGE, ArchitectonicAuxiliary.Concept.VIEWER_IMAGE, ".png",
                "/16x16/plain/delete2.png", aceAuxPath);

            addStockImage(vodb, "5b7f3f12-8034-11dc-8314-0800200c9a66", "icon for false",
                RefsetAuxiliary.Concept.BOOLEAN_CHECK_CROSS_ICONS_FALSE, ArchitectonicAuxiliary.Concept.VIEWER_IMAGE,
                ".png", "/16x16/plain/navigate_cross.png", aceAuxPath);
            addStockImage(vodb, "5b7f3f13-8034-11dc-8314-0800200c9a66", "icon for true",
                RefsetAuxiliary.Concept.BOOLEAN_CHECK_CROSS_ICONS_TRUE, ArchitectonicAuxiliary.Concept.VIEWER_IMAGE,
                ".png", "/16x16/plain/navigate_check.png", aceAuxPath);
            addStockImage(vodb, "5b7f3f14-8034-11dc-8314-0800200c9a66", "icon for false",
                RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_FALSE, ArchitectonicAuxiliary.Concept.VIEWER_IMAGE,
                ".png", "/16x16/plain/forbidden.png", aceAuxPath);
            addStockImage(vodb, "5b7f3f15-8034-11dc-8314-0800200c9a66", "icon for true",
                RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE, ArchitectonicAuxiliary.Concept.VIEWER_IMAGE, ".png",
                "/16x16/plain/check.png", aceAuxPath);
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
        }

    }

    private static void addStockImage(VodbEnv vodb, String uuidStr, String textDesc,
            I_ConceptualizeUniversally conceptForImage, I_ConceptualizeUniversally imageType, String imageFormat,
            String imageResource, I_Path aceAuxPath) throws TerminologyException, IOException, DatabaseException {
        int idSource = LocalVersionedTerminology.get().uuidToNative(
            ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.getUids());
        int nativeImageId = vodb.uuidToNativeWithGeneration(UUID.fromString(uuidStr), idSource, aceAuxPath,
            Integer.MIN_VALUE);

        int nativeConceptId = vodb.uuidToNative(conceptForImage.getUids());
        URL imageURL = AddImage.class.getResource(imageResource);
        InputStream fis = imageURL.openStream();
        int size = (int) fis.available();

        byte[] image = new byte[size];
        int read = fis.read(image, 0, image.length);
        while (read != size) {
            size = size - read;
            read = fis.read(image, read, size);
        }

        ThinImagePart imagePart = new ThinImagePart();
        imagePart.setStatusId(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
        imagePart.setPathId(vodb.uuidToNative(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids()));
        imagePart.setVersion(Integer.MIN_VALUE);
        imagePart.setTextDescription(textDesc);
        imagePart.setTypeId(vodb.uuidToNative(imageType.getUids()));
        List<I_ImagePart> parts = new ArrayList<I_ImagePart>(1);
        parts.add(imagePart);

        I_ImageVersioned imageCore = new ThinImageVersioned(nativeImageId, image, parts, imageFormat, nativeConceptId);
        vodb.writeImage(imageCore);
        AceLog.getAppLog().info("added image: " + textDesc);
    }
}
