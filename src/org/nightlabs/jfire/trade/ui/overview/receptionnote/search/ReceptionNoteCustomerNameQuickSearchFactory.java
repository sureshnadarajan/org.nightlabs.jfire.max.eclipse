package org.nightlabs.jfire.trade.ui.overview.receptionnote.search;

import org.nightlabs.jfire.base.ui.overview.search.QuickSearchEntry;
import org.nightlabs.jfire.store.ReceptionNote;
import org.nightlabs.jfire.trade.query.ReceptionNoteQuery;
import org.nightlabs.jfire.trade.ui.overview.search.AbstractCustomerNameQuickSearchEntryFactory;

/**
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class ReceptionNoteCustomerNameQuickSearchFactory
	extends AbstractCustomerNameQuickSearchEntryFactory<ReceptionNote, ReceptionNoteQuery>
{

	@Override
	public QuickSearchEntry<ReceptionNote, ReceptionNoteQuery> createQuickSearchEntry()
	{
		return new ReceptionNoteCustomerNameQuickSearchEntry(this);
	}

	@Override
	public Class<? extends ReceptionNoteQuery> getQueryType()
	{
		return ReceptionNoteQuery.class;
	}

}
