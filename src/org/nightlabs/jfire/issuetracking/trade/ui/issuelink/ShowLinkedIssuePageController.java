package org.nightlabs.jfire.issuetracking.trade.ui.issuelink;

import java.util.Collection;
import java.util.HashSet;

import javax.jdo.FetchPlan;

import org.nightlabs.base.ui.entity.editor.EntityEditor;
import org.nightlabs.base.ui.entity.editor.EntityEditorPageController;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueLink;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.issue.IssueType;
import org.nightlabs.jfire.issue.dao.IssueLinkDAO;
import org.nightlabs.jfire.issue.issuemarker.IssueMarker;
import org.nightlabs.jfire.issuetracking.trade.ui.resource.Messages;
import org.nightlabs.jfire.jbpm.graph.def.Statable;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.dao.ArticleContainerDAO;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.ui.articlecontainer.detail.ArticleContainerEditorInput;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Chairat Kongarayawetchakun - chairat at nightlabs dot de
 *
 */
public class ShowLinkedIssuePageController
extends EntityEditorPageController
{
	private ArticleContainerID articleContainerID;
	private Collection<Issue> linkedIssues;

	/**
	 * The fetch groups of linked issue data.
	 */
	public static final String[] FETCH_GROUPS = new String[] {
		FetchPlan.DEFAULT,
		IssueLink.FETCH_GROUP_ISSUE,
		Issue.FETCH_GROUP_ISSUE_TYPE,
		Issue.FETCH_GROUP_SUBJECT,
		Issue.FETCH_GROUP_DESCRIPTION,
		Issue.FETCH_GROUP_ISSUE_SEVERITY_TYPE,
		Issue.FETCH_GROUP_ISSUE_PRIORITY,
		Statable.FETCH_GROUP_STATE,
		Issue.FETCH_GROUP_ISSUE_LOCAL,
		StatableLocal.FETCH_GROUP_STATE,
		State.FETCH_GROUP_STATE_DEFINITION,
		IssueType.FETCH_GROUP_NAME,
		IssueSeverityType.FETCH_GROUP_NAME,
		IssuePriority.FETCH_GROUP_NAME,
		StateDefinition.FETCH_GROUP_NAME,
		Issue.FETCH_GROUP_ISSUE_MARKERS,          // <-- Since 14.05.2009
		IssueMarker.FETCH_GROUP_NAME,             // <-- Since 14.05.2009
		IssueMarker.FETCH_GROUP_ICON_16X16_DATA,  // <-- Since 14.05.2009
	};

	/**
	 * @param editor
	 */
	public ShowLinkedIssuePageController(EntityEditor editor) {
		super(editor);
		this.articleContainerID = ((ArticleContainerEditorInput) editor.getEditorInput()).getArticleContainerID();
		linkedIssues = new HashSet<Issue>();
	}

	/**
	 * @param editor
	 * @param startBackgroundLoading
	 */
	public ShowLinkedIssuePageController(EntityEditor editor, boolean startBackgroundLoading) {
		super(editor, startBackgroundLoading);
	}

	@Override
	public void doLoad(ProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.issuetracking.trade.ui.issuelink.ShowLinkedIssuePageController.monitor.loadIssues.text"), 10); //$NON-NLS-1$

		this.articleContainer =
			ArticleContainerDAO.sharedInstance().getArticleContainer(articleContainerID, FETCH_GROUPS, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);

		Collection<IssueLink> issueLinks = IssueLinkDAO.sharedInstance().getIssueLinksByOrganisationIDAndLinkedObjectID(
				null, // This must be the local organisationID! The backend now chooses this automatically, when passing null. Marco.
				articleContainerID,
				FETCH_GROUPS,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
				monitor);

		for (IssueLink issueLink : issueLinks) {
			linkedIssues.add(issueLink.getIssue());
		}

		fireModifyEvent(null, null);
	}

	@Override
	public boolean doSave(ProgressMonitor monitor) {
		return false;
	}

	public Collection<Issue> getLinkedIssues() {
		return linkedIssues;
	}

	public ArticleContainerID getArticleContainerID() {
		return articleContainerID;
	}

	private ArticleContainer articleContainer;
	public ArticleContainer getArticleContainer() {
		return articleContainer;
	}
}