package org.dwfa.ace.api;

import java.io.File;
import java.util.List;

import org.dwfa.bpa.process.TaskFailedException;
import org.tigris.subversion.javahl.PromptUserPassword3;

public interface I_HandleSubversion {

    public void svnCheckout(SubversionData svd) throws TaskFailedException;
    public void svnCleanup(SubversionData svd) throws TaskFailedException;
    public void svnCommit(SubversionData svd) throws TaskFailedException;
    public void svnImport(SubversionData svd) throws TaskFailedException;
    public void svnPurge(SubversionData svd) throws TaskFailedException;
    public void svnStatus(SubversionData svd) throws TaskFailedException;
    public void svnUpdate(SubversionData svd) throws TaskFailedException;
	public void svnUpdateDatabase(SubversionData svd) throws TaskFailedException;
    public void svnLock(SubversionData svd, File toLock) throws TaskFailedException;
    public void svnUnlock(SubversionData svd, File toUnLock) throws TaskFailedException;

    public void svnCompleteRepoInfo(SubversionData svd) throws TaskFailedException;
    public List<String> svnList(SubversionData svd) throws TaskFailedException;

	public void svnCheckout(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException;
	public void svnCleanup(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException;
	public void svnCommit(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException;
	public void svnImport(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException;
	public void svnPurge(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException;
	public void svnStatus(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException;
	public void svnUpdate(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException;
	public void svnUpdateDatabase(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException;
	public void svnLock(SubversionData svd, File toLock, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException;
    public void svnUnlock(SubversionData svd, File toUnlock, PromptUserPassword3 authenticator, boolean interactive) throws TaskFailedException;
	
}
