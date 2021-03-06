package org.nightlabs.jfire.trade.ui.store.search;

import org.eclipse.swt.widgets.Composite;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;
import org.nightlabs.jfire.store.search.BaseProductTypeQuery;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class GenericProductTypeSearchComposite
extends AbstractProductTypeSearchComposite
{
	/**
	 * @param parent
	 * @param style
	 */
	public GenericProductTypeSearchComposite(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected AbstractProductTypeQuery createNewQuery() {
		return new BaseProductTypeQuery();
	}

	@Override
	protected Class<? extends AbstractProductTypeQuery> getQueryClass()
	{
		return BaseProductTypeQuery.class;
	}

	@Override
	protected Class<? extends ProductType> getResultClass()
	{
		return ProductType.class;
	}

}
