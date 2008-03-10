package org.nightlabs.jfire.voucher.ui.quicklist;

import java.util.Collection;

import javax.jdo.FetchPlan;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jfire.base.ui.login.Login;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.trade.ui.producttype.quicklist.AbstractProductTypeQuickListFilter;
import org.nightlabs.jfire.voucher.store.VoucherTypeSearchFilter;
import org.nightlabs.jfire.voucher.ui.resource.Messages;
import org.nightlabs.progress.ProgressMonitor;

public class VoucherTypeQuickListFilter
extends AbstractProductTypeQuickListFilter
{
	public static String[] DEFAULT_FETCH_GROUP = new String[] {
		FetchPlan.DEFAULT,
		ProductType.FETCH_GROUP_NAME};

	private VoucherTypeTable voucherTypeTable;

	protected Control doCreateResultViewerControl(Composite parent)
	{
		voucherTypeTable = new VoucherTypeTable(parent);
		return voucherTypeTable;
	}
	
	public Control getResultViewerControl()
	{
		return voucherTypeTable;
	}
		
	public String getDisplayName()
	{
		return Messages.getString("org.nightlabs.jfire.voucher.ui.quicklist.VoucherTypeQuickListFilter.displayName"); //$NON-NLS-1$
	}

	public void search(ProgressMonitor monitor) {
		final VoucherTypeSearchFilter searchFilter = new VoucherTypeSearchFilter(SearchFilter.CONJUNCTION_DEFAULT);

		new Job(Messages.getString("org.nightlabs.jfire.voucher.ui.quicklist.VoucherTypeQuickListFilter.loadVoucherTypesJob.name")) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					StoreManager storeManager = StoreManagerUtil.getHome(
							Login.getLogin().getInitialContextProperties()).create();
					final Collection voucherTypes = storeManager.searchProductTypes(
							searchFilter, DEFAULT_FETCH_GROUP, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							voucherTypeTable.setInput(voucherTypes);
						}
					});
					return Status.OK_STATUS;
				} catch (Exception x) {
					throw new RuntimeException(x);
				}
			}
		}.schedule();
	}

}
