/**
 * 
 */
package org.nightlabs.jfire.trade.ui.overview.account.search;

import java.util.Collection;
import java.util.Collections;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.accounting.dao.AccountDAO;
import org.nightlabs.jfire.accounting.query.AccountQuery;
import org.nightlabs.jfire.base.ui.overview.search.AbstractQuickSearchEntry;
import org.nightlabs.jfire.base.ui.overview.search.QuickSearchEntryFactory;
import org.nightlabs.jfire.trade.ui.overview.account.AccountEntryViewer;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class AccountNameQuickSearchEntry
extends AbstractQuickSearchEntry
{
	public AccountNameQuickSearchEntry(QuickSearchEntryFactory factory) {
		super(factory);
	}

	public Object search(ProgressMonitor monitor)
	{
		AccountQuery query = new AccountQuery();
		query.setName(getSearchText());
		query.setFromInclude(getMinIncludeRange());
		query.setToExclude(getMaxExcludeRange());
		Collection<AccountQuery> queries = Collections.singleton(query);
		return AccountDAO.sharedInstance().getAccountsForQueries(
				queries,
				AccountEntryViewer.FETCH_GROUPS_ACCOUNTS,
				NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}
}
