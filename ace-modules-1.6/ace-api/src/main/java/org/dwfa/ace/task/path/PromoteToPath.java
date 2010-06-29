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
package org.dwfa.ace.task.path;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ProcessProgressWrapper;
import org.dwfa.ace.path.CopyPathToPath;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * Promote updates to a path.
 * <p>
 * This task will copy changes from a number of source paths to a target path
 * (intended to be a path used for releasing content). Source paths are defined
 * as destination relationship origins of a configurable relationship type (eg
 * "promotes to").
 * <p>
 * This task will also copy from the inherited origins of source paths. It will
 * traverse up the origin lineage until it a path is encountered which is a
 * common ancestor of the target path's origin lineage.
 * <p>
 * The immediate origin position of the target path will also be set to the
 * current time. This prevents changes being inherited without this promotion
 * task being run.
 */
@AllowDataCheckSuppression
@BeanList(specs = { @Spec(directory = "tasks/ide/path", type = BeanType.TASK_BEAN) })
public class PromoteToPath extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int DATA_VERSION = 2;

    private String pathConceptPropName = ProcessAttachmentKeys.TO_PATH_CONCEPT.getAttachmentKey();

    private String destRelTypeConceptPropName = ProcessAttachmentKeys.I_GET_CONCEPT_DATA.getAttachmentKey();

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(DATA_VERSION);
        out.writeObject(pathConceptPropName);
        out.writeObject(destRelTypeConceptPropName);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion > DATA_VERSION) {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
        pathConceptPropName = (String) in.readObject();
        if (objDataVersion >= 2) {
            destRelTypeConceptPropName = (String) in.readObject();
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        ProcessProgressWrapper progressWrapper = null;
        I_ConfigAceFrame config = null;
        try {
            I_GetConceptData targetPathConcept = getProperty(process, I_GetConceptData.class, pathConceptPropName);
            I_GetConceptData destRelType = getProperty(process, I_GetConceptData.class, destRelTypeConceptPropName);

            I_TermFactory termFactory = LocalVersionedTerminology.get();

            if (!termFactory.hasPath(targetPathConcept.getConceptId())) {
                throw new TaskFailedException("The selected concept '" + targetPathConcept.getInitialText()
                    + "' is not a path.");
            }

            config = termFactory.getActiveAceFrameConfig();
            config.setSuppressChangeEvents(true);

            I_IntSet allowedTypes = termFactory.newIntSet();
            allowedTypes.add(destRelType.getConceptId());

            Set<I_Position> positionsToCopy = getPositionsToCopy(targetPathConcept, destRelType, termFactory);

            // Log task parameters
            StringBuffer logMsg = new StringBuffer("PromoteToPath task parameters: target path='"
                + targetPathConcept.getInitialText() + "', source paths=(");
            for (I_Position srcPosition : positionsToCopy) {
                logMsg.append("'").append(srcPosition.getPath().toString()).append("'");
            }
            logMsg.append(")");
            getLogger().info(logMsg.toString());

            // Fix all target path origins to the present time
            I_Path targetPath = termFactory.getPath(targetPathConcept.getUids());
            int presentTime = termFactory.convertToThinVersion(System.currentTimeMillis());
            for (I_Position position : targetPath.getOrigins()) {
                I_Position newPosition = termFactory.newPosition(position.getPath(), presentTime);
                termFactory.writePathOrigin(targetPath, newPosition);
            }

            // Work out which positions (including origins) to source data from.
            // We do not copy from paths (including origins) that are common to
            // both a source path
            // and the target path.

            logMsg = new StringBuffer("Calculated the following normalised, inhierited source positions: ");
            for (I_Position copyPosition : positionsToCopy) {
                logMsg.append("'").append(copyPosition.toString()).append("',");
            }
            logMsg.deleteCharAt(logMsg.length() - 1);
            getLogger().info(logMsg.toString());

            CopyPathToPath copyProcessor = new CopyPathToPath();
            copyProcessor.setSourcePositions(positionsToCopy);
            copyProcessor.setTargetPathId(targetPath.getConceptId());
            copyProcessor.setCopyOnlyLatestState(true);
            copyProcessor.setReadLatestPartOnly(true);

            String processDesc = "Promoting updates to path '" + targetPathConcept.getInitialText() + "'";
            progressWrapper = new ProcessProgressWrapper(copyProcessor, processDesc);
            termFactory.iterateConcepts(progressWrapper);
            progressWrapper.complete();

            termFactory.commit();

            return Condition.CONTINUE;

        } catch (Exception e) {
            if (progressWrapper != null) {
                progressWrapper.cancel();
            }
            throw new TaskFailedException(e);
        } finally {
            if (config != null) {
                config.setSuppressChangeEvents(false);
            }
        }
    }

    /**
     * Gets the list of migrated positions to from the target (release) path concept.
     *
     * @param targetPathConcept I_GetConceptData
     * @param destRelType I_GetConceptData
     * @param termFactory I_TermFactory
     * @return Set of I_Position
     * @throws Exception
     */
    public static Set<I_Position> getPositionsToCopy(I_GetConceptData targetPathConcept,
            I_GetConceptData destRelType, I_TermFactory termFactory) throws Exception {
        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
        I_Path targetPath = termFactory.getPath(targetPathConcept.getUids());

        I_IntSet allowedTypes = termFactory.newIntSet();
        allowedTypes.add(destRelType.getConceptId());

        Set<I_GetConceptData> destRelOrigins = targetPathConcept.getDestRelOrigins(config.getAllowedStatus(), allowedTypes, null, false,
            true);

        if (destRelOrigins.size() == 0) {
            throw new Exception("No work to complete. The selected path concepts has no '"
                + destRelType.getInitialText() + "' destination relationship type origins.");
        }

        Set<I_Path> sourcePaths = new HashSet<I_Path>();
        for (I_GetConceptData srcPath : destRelOrigins) {
            if (!termFactory.hasPath(srcPath.getConceptId())) {
                throw new Exception("The destination relationship origin concept '" + srcPath.getInitialText()
                    + "' is not a path.");
            }
            sourcePaths.add(termFactory.getPath(srcPath.getUids()));
        }

        return getUniquePositions(termFactory, targetPath, sourcePaths);
    }

    /**
     * Gets the Set of Positions that don't have a common origin to the target path.
     *
     * @param termFactory I_TermFactory
     * @param targetPath I_Path
     * @param sourcePaths Set of I_Path
     * @return Set of I_Position
     * @throws TerminologyException
     * @throws IOException
     */
    private static Set<I_Position> getUniquePositions(I_TermFactory termFactory,
            I_Path targetPath, Set<I_Path> sourcePaths) throws TerminologyException, IOException {

        Set<I_Position> positionsToCopy = new HashSet<I_Position>();

        I_Path firstSourcePath = sourcePaths.iterator().next();
        Set<I_Position> sourceInheritedOriginPaths = firstSourcePath.getNormalisedOrigins(sourcePaths);
        Set<I_Position> targetInheritedOriginPositions = targetPath.getNormalisedOrigins();
        for (I_Path srcPath : sourcePaths) {
            positionsToCopy.add(termFactory.newPosition(srcPath, Integer.MAX_VALUE));
        }

        Set<Integer> validSrcPathIds = new HashSet<Integer>();
        for (I_Position originPosition : sourceInheritedOriginPaths) {
            validSrcPathIds.add(originPosition.getPath().getConceptId());
        }

        for (I_Position originPosition : sourceInheritedOriginPaths) {
            for (I_Position targetPosition : targetInheritedOriginPositions) {
                if (originPosition.getPath().getConceptId() == targetPosition.getPath().getConceptId()) {
                    validSrcPathIds.remove(originPosition.getPath().getConceptId());
                }
            }
        }

        for (I_Position originPosition : sourceInheritedOriginPaths) {
            if (validSrcPathIds.contains(originPosition.getPath().getConceptId())) {
                positionsToCopy.add(originPosition);
            }
        }

        return positionsToCopy;
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getPathConceptPropName() {
        return pathConceptPropName;
    }

    public void setPathConceptPropName(String pathConceptPropName) {
        this.pathConceptPropName = pathConceptPropName;
    }

    public String getDestRelTypeConceptPropName() {
        return destRelTypeConceptPropName;
    }

    public void setDestRelTypeConceptPropName(String destRelTypeConceptPropName) {
        this.destRelTypeConceptPropName = destRelTypeConceptPropName;
    }
}
