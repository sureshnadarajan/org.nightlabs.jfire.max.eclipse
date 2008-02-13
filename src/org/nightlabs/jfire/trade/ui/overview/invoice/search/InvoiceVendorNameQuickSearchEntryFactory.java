/**
 * 
 */
package org.nightlabs.jfire.trade.ui.overview.invoice.search;

import org.nightlabs.jfire.base.ui.overview.search.QuickSearchEntry;
import org.nightlabs.jfire.trade.ui.overview.search.AbstractVendorNameQuickSearchEntryFactory;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class InvoiceVendorNameQuickSearchEntryFactory
extends AbstractVendorNameQuickSearchEntryFactory
{
	public QuickSearchEntry createQuickSearchEntry() {
		return new InvoiceVendorNameQuickSearchEntry(this);
	}
}
