package org.nightlabs.jfire.trade.overview.deliverynote.action;

import org.eclipse.ui.IEditorInput;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.trade.articlecontainer.detail.GeneralEditor;
import org.nightlabs.jfire.trade.articlecontainer.detail.deliverynote.GeneralEditorInputDeliveryNote;
import org.nightlabs.jfire.trade.overview.action.AbstractEditArticleContainerAction;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class EditDeliveryNoteAction 
extends AbstractEditArticleContainerAction 
{
	public String getEditorID() {
		return GeneralEditor.ID_EDITOR;
	}

	public IEditorInput getEditorInput() {
		DeliveryNoteID deliveryNoteID = (DeliveryNoteID) getArticleContainerID();
		return new GeneralEditorInputDeliveryNote(deliveryNoteID);
	}

}
