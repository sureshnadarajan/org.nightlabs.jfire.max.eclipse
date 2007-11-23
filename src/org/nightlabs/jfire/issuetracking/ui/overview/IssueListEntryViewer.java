/**
 * 
 */
package org.nightlabs.jfire.issuetracking.ui.overview;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.base.ui.overview.Entry;
import org.nightlabs.jfire.base.ui.overview.search.AbstractQueryFilterComposite;
import org.nightlabs.jfire.base.ui.overview.search.JDOQuerySearchEntryViewer;
import org.nightlabs.jfire.issuetracking.ui.issue.IssueTable;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Chairat Kongarayawetchakun 
 *
 */
public class IssueListEntryViewer 
extends JDOQuerySearchEntryViewer {

	public IssueListEntryViewer(Entry entry) {
		super(entry);
	}

	private IssueTable issueTable;
	
	@Override
	public AbstractQueryFilterComposite createFilterComposite(Composite parent) {
		return new IssueFilterComposite(parent, SWT.NONE);
	}

	@Override
	public AbstractTableComposite createListComposite(Composite parent) {
		issueTable = new IssueTable(parent, SWT.NONE);
		return issueTable;
	}

	@Override
	protected Object getQueryResult(Collection<JDOQuery> queries,
			ProgressMonitor monitor) {
		return null;
	}

	
}
