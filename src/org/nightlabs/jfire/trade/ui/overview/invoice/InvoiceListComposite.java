package org.nightlabs.jfire.trade.ui.overview.invoice;

import javax.jdo.FetchPlan;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.jbpm.graph.def.StatableLocal;
import org.nightlabs.jfire.jbpm.graph.def.State;
import org.nightlabs.jfire.jbpm.graph.def.StateDefinition;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.ui.overview.AbstractArticleContainerListComposite;
import org.nightlabs.jfire.trade.ui.resource.Messages;
import org.nightlabs.l10n.DateFormatter;
import org.nightlabs.l10n.NumberFormatter;

public class InvoiceListComposite
extends AbstractArticleContainerListComposite<Invoice>
{

	/**
	 * The fetch-groups this list composite needs to display invoices.
	 */
	public static final String[] FETCH_GROUPS_INVOICES = new String[] {
		FetchPlan.DEFAULT,
		Invoice.FETCH_GROUP_THIS_INVOICE,
		Invoice.FETCH_GROUP_INVOICE_LOCAL,
		StatableLocal.FETCH_GROUP_STATE,
		State.FETCH_GROUP_STATE_DEFINITION,
		StateDefinition.FETCH_GROUP_NAME,
		LegalEntity.FETCH_GROUP_PERSON
	};

	public InvoiceListComposite(Composite parent, int style) {
		super(parent, style);
	}

//	@Override
//	protected void createArticleContainerIDPrefixTableColumn(
//			TableViewer tableViewer, Table table)
//	{
//		TableColumn tc = new TableColumn(table, SWT.LEFT);
//		tc.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.invoice.InvoiceListComposite.invoiceIDPrefixTableColumn.text")); //$NON-NLS-1$
////		tableLayout.setColumnData(tc, new ColumnWeightData(10));
//		addWeightedColumn(10);
//	}
//	@Override
//	protected void createArticleContainerIDTableColumn(TableViewer tableViewer,
//			Table table)
//	{
//		TableColumn tc = new TableColumn(table, SWT.RIGHT);
//		tc.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.invoice.InvoiceListComposite.invoiceIDTableColumn.text")); //$NON-NLS-1$
////		tableLayout.setColumnData(tc, new ColumnWeightData(10));
//		addWeightedColumn(10);
//	}

	@Override
	protected void createAdditionalTableColumns(TableViewer tableViewer,
			Table table)
	{
		TableColumn tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.invoice.InvoiceListComposite.finalizeDateTableColumn.text")); //$NON-NLS-1$
//		tableLayout.setColumnData(tc, new ColumnWeightData(10));
		addWeightedColumn(10);

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.invoice.InvoiceListComposite.finalizeUserTableColumn.text")); //$NON-NLS-1$
//		tableLayout.setColumnData(tc, new ColumnWeightData(10));
		addWeightedColumn(10);

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.invoice.InvoiceListComposite.priceTableColumn.text")); //$NON-NLS-1$
//		tableLayout.setColumnData(tc, new ColumnWeightData(10));
		addWeightedColumn(10);

		tc = new TableColumn(table, SWT.LEFT);
		tc.setText(Messages.getString("org.nightlabs.jfire.trade.ui.overview.invoice.InvoiceListComposite.amountToPayTableColumn.text")); //$NON-NLS-1$
//		tableLayout.setColumnData(tc, new ColumnWeightData(10));
		addWeightedColumn(10);
	}

	@Override
	protected String getAdditionalColumnText(Object element,
			int additionalColumnIndex, int firstAdditionalColumnIndex, int columnIndex)
	{
		if (!(element instanceof Invoice))
			return ""; //$NON-NLS-1$

		Invoice invoice = (Invoice) element;
		switch (additionalColumnIndex) {
			case 0:
				if (invoice.getFinalizeDT() != null)
					return DateFormatter.formatDateShort(invoice.getFinalizeDT(), false);
			break;
			case 1:
				if (invoice.getFinalizeUser() != null)
					return invoice.getFinalizeUser().getName();
			break;
			case 2:
				if (invoice.getPrice() != null && invoice.getCurrency() != null)
					return NumberFormatter.formatCurrency(invoice.getPrice().getAmount(), invoice.getCurrency());
			break;
			case 3:
				if (invoice.getPrice() != null && invoice.getCurrency() != null)
					return NumberFormatter.formatCurrency(invoice.getInvoiceLocal().getAmountToPay(), invoice.getCurrency());
			break;
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	protected Class<? extends ArticleContainer> getArticleContainerClass() {
		return Invoice.class;
	}

}
