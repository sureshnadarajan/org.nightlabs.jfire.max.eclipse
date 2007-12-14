/**
 * 
 */
package org.nightlabs.jfire.issuetracking.ui.overview;

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.base.ui.overview.Entry;
import org.nightlabs.jfire.base.ui.overview.search.AbstractQueryFilterComposite;
import org.nightlabs.jfire.base.ui.overview.search.JDOQuerySearchEntryViewer;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueDescription;
import org.nightlabs.jfire.issue.IssuePriority;
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.issue.IssueSubject;
import org.nightlabs.jfire.issue.IssueType;
import org.nightlabs.jfire.issue.dao.IssueDAO;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issuetracking.ui.issue.IssueEditor;
import org.nightlabs.jfire.issuetracking.ui.issue.IssueEditorInput;
import org.nightlabs.jfire.issuetracking.ui.issue.IssueTable;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Chairat Kongarayawetchakun 
 *
 */
public class IssueEntryListViewer 
extends JDOQuerySearchEntryViewer {

	public IssueEntryListViewer(Entry entry) {
		super(entry);
	}

	private IssueTable issueTable;
	
	@Override
	public AbstractQueryFilterComposite createFilterComposite(Composite parent) {
		return new IssueFilterComposite(parent, SWT.NONE);
	}

	@Override
	public AbstractTableComposite<Issue> createListComposite(Composite parent) {
		issueTable = new IssueTable(parent, SWT.NONE);
		
		issueTable.getTableViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				StructuredSelection s = (StructuredSelection)e.getSelection();
				if (s.isEmpty())
					return;

				Issue issue = (Issue)s.getFirstElement();
				IssueEditorInput issueEditorInput = new IssueEditorInput(IssueID.create(issue.getOrganisationID(), issue.getIssueID()));
				try {
					RCPUtil.openEditor(issueEditorInput, IssueEditor.EDITOR_ID);
				} catch (PartInitException e1) {
					throw new RuntimeException(e1);
				}
			}
		});
		
		return issueTable;
	}

	private static final String[] FETCH_GROUPS_ISSUES = { Issue.FETCH_GROUP_THIS, IssueType.FETCH_GROUP_THIS, IssueSeverityType.FETCH_GROUP_THIS, IssuePriority.FETCH_GROUP_THIS, IssueDescription.FETCH_GROUP_THIS, IssueSubject.FETCH_GROUP_THIS
		, FetchPlan.DEFAULT };
	@Override
	protected Object getQueryResult(Collection<JDOQuery> queries,
			ProgressMonitor monitor) {
		try {
			return IssueDAO.sharedInstance().getIssuesForQueries(
					queries,
					FETCH_GROUPS_ISSUES, 
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, 
					monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
