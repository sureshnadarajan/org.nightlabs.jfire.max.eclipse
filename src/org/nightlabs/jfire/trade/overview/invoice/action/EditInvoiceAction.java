package org.nightlabs.jfire.trade.overview.invoice.action;

import org.eclipse.ui.IEditorInput;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.trade.articlecontainer.detail.GeneralEditor;
import org.nightlabs.jfire.trade.articlecontainer.detail.invoice.GeneralEditorInputInvoice;
import org.nightlabs.jfire.trade.overview.action.AbstractEditArticleContainerAction;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class EditInvoiceAction 
//extends OverviewEditAction 
extends AbstractEditArticleContainerAction
{
	public EditInvoiceAction() {
		super();
	}

	public String getEditorID() {
		return GeneralEditor.ID_EDITOR;
	}

	public IEditorInput getEditorInput() {
		InvoiceID invoiceID = (InvoiceID) getArticleContainerID();
		return new GeneralEditorInputInvoice(invoiceID);
	}
	
}
