/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.trade.ui.articlecontainer.detail.action.addtodeliverynote;

import org.nightlabs.base.ui.wizard.DynamicPathWizardDialog;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ui.articlecontainer.detail.IGeneralEditor;
import org.nightlabs.jfire.trade.ui.articlecontainer.detail.action.GenericArticleEditAction;

public class AddToDeliveryNoteAction
extends GenericArticleEditAction
{
	@Override
	public boolean calculateVisible()
	{
		IGeneralEditor editor = getArticleEditActionRegistry().getActiveGeneralEditorActionBarContributor().getActiveGeneralEditor();
		return editor != null &&
				!(editor.getGeneralEditorComposite().getArticleContainerID() instanceof DeliveryNoteID);
	}
	
	@Override
	protected boolean excludeArticle(Article article)
	{
		// Exclude if the article is already in a delivery note
		if (article.getDeliveryNoteID() != null)
			return true;
		
		// Exclude if the article is a reversing article and the corresponding reversed article is not in a delivery note
		if (article.isReversing() && article.getReversedArticle() != null && article.getReversedArticle().getDeliveryNoteID() == null)
			return true;
		
		return false;
	}

	@Override
	public void run()
	{
		AddToDeliveryNoteWizard addToDeliveryNoteWizard = new AddToDeliveryNoteWizard(getArticles());
		DynamicPathWizardDialog dialog = new DynamicPathWizardDialog(addToDeliveryNoteWizard);
		dialog.open();
	}
}
