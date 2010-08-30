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
package org.dwfa.ace.api;

import java.io.File;
import java.util.List;

import org.dwfa.bpa.process.TaskFailedException;
import org.tigris.subversion.javahl.PromptUserPassword3;

public interface I_HandleSubversion {

    public void svnCheckout(SubversionData svd) throws TaskFailedException;

    public void svnCleanup(SubversionData svd) throws TaskFailedException;

    public void svnCommit(SubversionData svd) throws TaskFailedException;

    public void svnCommitNoPrompt(SubversionData svd) throws TaskFailedException;

    public void svnImport(SubversionData svd) throws TaskFailedException;

    public void svnPurge(SubversionData svd) throws TaskFailedException;

    public void svnRevert(SubversionData svd) throws TaskFailedException;

    public void svnStatus(SubversionData svd) throws TaskFailedException;

    public void svnUpdate(SubversionData svd) throws TaskFailedException;

    public void svnUpdateDatabase(SubversionData svd) throws TaskFailedException;

    public void svnLock(SubversionData svd, File toLock) throws TaskFailedException;

    public void svnUnlock(SubversionData svd, File toUnLock) throws TaskFailedException;

    public void svnCompleteRepoInfo(SubversionData svd) throws TaskFailedException;

    public List<String> svnList(SubversionData svd) throws TaskFailedException;

    public void svnCheckout(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException;

    public void svnCleanup(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException;

    public void svnCommit(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException;

    public void svnImport(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException;

    public void svnPurge(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException;

    public void svnRevert(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException;

    public void svnStatus(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException;

    public void svnUpdate(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException;

    public void svnUpdateDatabase(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException;

    public void svnLock(SubversionData svd, File toLock, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException;

    public void svnUnlock(SubversionData svd, File toUnlock, PromptUserPassword3 authenticator, boolean interactive)
            throws TaskFailedException;

}
