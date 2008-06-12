/**
 *
 */
package org.nightlabs.jfire.scripting.ui;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.scripting.ScriptManager;
import org.nightlabs.jfire.scripting.ScriptManagerUtil;
import org.nightlabs.jfire.scripting.ScriptParameterSet;
import org.nightlabs.jfire.scripting.id.ScriptParameterSetID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Accessor for ScriptParameterSets.
 *
 * @author Alexander Bieber <alex[AT]nightlabs[ÐOT]de>
 * @author Daniel Mazurek <daniel[AT]nightlabs[ÐOT]de>
 */
public class ScriptParameterSetDAO 
extends BaseJDOObjectDAO<ScriptParameterSetID, ScriptParameterSet>
{
	private static ScriptParameterSetDAO sharedInstance;

	public static ScriptParameterSetDAO sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new ScriptParameterSetDAO();
		return sharedInstance;
	}
	
	private ScriptParameterSetDAO() {}
	
	/**
	 * Returns the collection of all Parametersets
	 * for the organisation of the currently logged in
	 * user.
	 */
	public Collection<ScriptParameterSet> getScriptParameterSets(String organisationID, String[] fetchGroups, int maxFetchDepth, 
			ProgressMonitor monitor) 
	{
		try {
			ScriptManager sm = ScriptManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			Set<ScriptParameterSetID> ids = sm.getAllScriptParameterSetIDs(organisationID);
			return getJDOObjects(null, ids, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

//	public ScriptParameterSet getScriptParameterSet(String organisationID, long parameterSetID, String[] fetchGroups, int maxFetchDepth,
//			ProgressMonitor monitor) 
//	{
//		return getJDOObject(null, ScriptParameterSetID.create(organisationID, parameterSetID), fetchGroups, maxFetchDepth, monitor);
//	}

	public ScriptParameterSet getScriptParameterSet(ScriptParameterSetID scriptParameterSetID, String[] fetchGroups, int maxFetchDepth,
			ProgressMonitor monitor) 
	{
		return getJDOObject(null, scriptParameterSetID, fetchGroups, maxFetchDepth, monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<ScriptParameterSet> retrieveJDOObjects(
			Set<ScriptParameterSetID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception 
	{
		monitor.beginTask("Loading ScriptParameterSets", 100);
		try {
			ScriptManager sm = ScriptManagerUtil.getHome(Login.getLogin().getInitialContextProperties()).create();
			monitor.worked(50);
			Collection<ScriptParameterSet> result = sm.getScriptParameterSets(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(50);
			return result;
		}
		finally {
			monitor.done();
		}
	}

}
