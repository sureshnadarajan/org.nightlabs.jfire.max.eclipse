package org.nightlabs.jfire.trade.ui.store.search;

import java.util.Locale;

import org.nightlabs.base.ui.search.AbstractSearchResultProviderFactory;
import org.nightlabs.base.ui.search.ISearchResultProvider;
import org.nightlabs.jfire.store.ProductType;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class GenericProductTypeSearchResultProviderFactory
extends AbstractSearchResultProviderFactory
{
	public GenericProductTypeSearchResultProviderFactory() {
		getName().setText(Locale.ENGLISH.getLanguage(), "Products"); //$NON-NLS-1$
		getName().setText(Locale.GERMANY.getLanguage(), "Produkte"); //$NON-NLS-1$
	}
	
	public ISearchResultProvider createSearchResultProvider() {
		return new GenericProductTypeSearchResultProvider(this);
	}

	public Class getResultTypeClass() {
		return ProductType.class;
	}
}
