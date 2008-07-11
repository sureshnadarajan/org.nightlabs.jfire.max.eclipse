package org.nightlabs.jfire.issuetracking.ui.issue.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.nightlabs.base.ui.editor.RestorableSectionPart;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.history.IssueHistoryDAO;
import org.nightlabs.jfire.issuetracking.ui.issuehistory.IssueHistoryTable;

/**
 * @author Chairat Kongarayawetchakun <!-- chairat [AT] nightlabs [DOT] de -->
 *
 */
public class IssueHistoryListSection extends RestorableSectionPart{

	private IssueHistoryTable issueHistoryTable;
	private IssueEditorPageController controller;
	
	public IssueHistoryListSection(FormPage page, Composite parent, IssueEditorPageController controller) {
		super(parent, page.getEditor().getToolkit(), ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
		this.controller = controller;
		getSection().setText("Issue History");
		getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
		getSection().setLayout(new GridLayout());
		
		issueHistoryTable = new IssueHistoryTable(getSection(), SWT.NONE);
		issueHistoryTable.getGridData().grabExcessHorizontalSpace = true;
		
		getSection().setClient(issueHistoryTable);
	}
	
	public void setIssue(Issue issue) {
		issueHistoryTable.setInput(IssueHistoryDAO.sharedInstance().getIssueHistoryByIssue(issue));
	}
}
