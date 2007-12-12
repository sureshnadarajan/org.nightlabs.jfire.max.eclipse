package org.nightlabs.jfire.issuetracking.admin.ui.overview.issueproperty;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IProgressMonitor;
import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.issue.IssueType;
import org.nightlabs.jfire.issue.dao.IssueTypeDAO;
import org.nightlabs.jfire.issue.id.IssueTypeID;
import org.nightlabs.progress.NullProgressMonitor;

public class IssueTypeEditorPageController extends EntityEditorPageController{

	/**
	 * The fetch groups of issue data.
	 */
	public static final String[] FETCH_GROUPS = new String[] {
		FetchPlan.DEFAULT,
		IssueType.FETCH_GROUP_THIS,
		IssueSeverityType.FETCH_GROUP_THIS,
		IssuePriority.FETCH_GROUP_THIS};
	
	
	private IssueTypeID issueTypeID;
	private IssueType issueType;
	
	public IssueTypeEditorPageController(EntityEditor editor)
	{
		super(editor);
		
//		JDOLifecycleManager.sharedInstance().addLifecycleListener(myLifecycleListener);
	}
	
//	@Override
//	public void dispose() {
//		JDOLifecycleManager.sharedInstance().removeLifecycleListener(myLifecycleListener);
//	}
//	
//	private JDOLifecycleListener myLifecycleListener = new JDOLifecycleAdapterJob("Loading Xyz") {
//	    private IJDOLifecycleListenerFilter filter = new SimpleLifecycleListenerFilter(
//	      IssueType.class,
//	      true,
//	      JDOLifecycleState.DIRTY);
//
//	    public IJDOLifecycleListenerFilter getJDOLifecycleListenerFilter()
//	    {
//	      return filter;
//	    }
//
//	    public void notify(JDOLifecycleEvent event)
//	    {
//	      loadIssueTypes();
//	    }
//	};

	public void doLoad(IProgressMonitor monitor) {
		monitor.beginTask("Loading Issue Types....", 100);
		
		IssueTypeEditorInput input = (IssueTypeEditorInput)getEntityEditor().getEditorInput();
		this.issueTypeID = input.getJDOObjectID();

		issueType = 
			IssueTypeDAO.sharedInstance().getIssueType(issueTypeID, 
					FETCH_GROUPS,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
					new NullProgressMonitor());
		
		monitor.done();
		fireModifyEvent(null, issueType);
	}

	public void doSave(IProgressMonitor monitor) {
		IssueTypeDAO.sharedInstance().storeIssueTypes(issueType, FETCH_GROUPS,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
					new NullProgressMonitor());
	}
	
	public IssueTypeID getIssueTypeID() {
		return issueTypeID;
	}
	
	public IssueType getIssueType() {
		return issueType;
	}
}
