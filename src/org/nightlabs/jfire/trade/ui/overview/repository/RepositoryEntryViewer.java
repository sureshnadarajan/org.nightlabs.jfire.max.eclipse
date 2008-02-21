package org.nightlabs.jfire.trade.ui.overview.repository;

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nightlabs.base.ui.table.AbstractTableComposite;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.base.ui.overview.Entry;
import org.nightlabs.jfire.base.ui.overview.search.AbstractQueryFilterComposite;
import org.nightlabs.jfire.base.ui.overview.search.JDOQuerySearchEntryViewer;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.store.RepositoryType;
import org.nightlabs.jfire.store.dao.RepositoryDAO;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.progress.ProgressMonitor;

/**
 * implementation of a {@link JDOQuerySearchEntryViewer} for searching and
 * displaying {@link Repository}s
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 */
public class RepositoryEntryViewer
extends JDOQuerySearchEntryViewer
{
	public RepositoryEntryViewer(Entry entry) {
		super(entry);
	}

	@Override
	public AbstractTableComposite createListComposite(Composite parent) {
		return new RepositoryListComposite(parent, SWT.NONE);
	}

	@Override
	public AbstractQueryFilterComposite createFilterComposite(Composite parent) {
		return new RepositoryFilterComposite(parent, SWT.NONE);
	}

	public static final String[] FETCH_GROUPS_REPOSITORIES = new String[] {
//		Repository.FETCH_GROUP_THIS_REPOSITORY, // we don't know what will be added - hence in order to keep it small, we specify individually
		Repository.FETCH_GROUP_NAME,
		Repository.FETCH_GROUP_OWNER,
		Repository.FETCH_GROUP_REPOSITORY_TYPE,
		RepositoryType.FETCH_GROUP_NAME,
		FetchPlan.DEFAULT,
		LegalEntity.FETCH_GROUP_PERSON
	};

	@Override
	protected Object getQueryResult(Collection<? extends JDOQuery> queries, ProgressMonitor monitor)
	{
		try {
			Collection<? extends JDOQuery<? extends Repository>> _queries = (Collection<? extends JDOQuery<? extends Repository>>) queries;
			Collection<Repository> repositories = RepositoryDAO.sharedInstance().getRepositoriesForQueries(
					_queries,
					FETCH_GROUPS_REPOSITORIES,
					NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT,
					monitor);
			return repositories;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
