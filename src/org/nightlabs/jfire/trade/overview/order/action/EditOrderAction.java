package org.nightlabs.jfire.trade.overview.order.action;

import org.eclipse.ui.IEditorInput;
import org.nightlabs.jfire.trade.articlecontainer.detail.GeneralEditor;
import org.nightlabs.jfire.trade.articlecontainer.detail.order.GeneralEditorInputOrder;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.overview.action.AbstractEditArticleContainerAction;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class EditOrderAction 
extends AbstractEditArticleContainerAction 
{

	public EditOrderAction() {
	}

	public String getEditorID() {
		return GeneralEditor.ID_EDITOR;
	}

	public IEditorInput getEditorInput() {
		OrderID orderID = (OrderID) getArticleContainerID();
		return new GeneralEditorInputOrder(orderID);
	}

}
