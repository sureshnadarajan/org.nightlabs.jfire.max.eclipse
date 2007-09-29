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

package org.nightlabs.jfire.trade.articlecontainer.detail.action;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import org.nightlabs.jfire.trade.articlecontainer.detail.ArticleEdit;
import org.nightlabs.jfire.trade.articlecontainer.detail.ArticleEditFactory;
import org.nightlabs.jfire.trade.articlecontainer.detail.ArticleSelection;
import org.nightlabs.jfire.trade.articlecontainer.detail.SegmentEdit;

public class ArticleEditAction extends Action implements IArticleEditAction
{
	public ArticleEditAction()
	{
		super("", SWT.PUSH | SWT.FLAT); //$NON-NLS-1$
	}

	private ArticleEditActionRegistry articleEditActionRegistry;

	public void init(
			ArticleEditActionRegistry articleEditActionRegistry)
	{
		this.articleEditActionRegistry = articleEditActionRegistry;
	}

	public ArticleEditActionRegistry getArticleEditActionRegistry()
	{
		return articleEditActionRegistry;
	}

	public boolean calculateVisible()
	{
		// Find out whether there is any delegate for this action in the active SegmentEdit

		SegmentEdit activeSegmentEdit = getArticleEditActionRegistry().getActiveGeneralEditorActionBarContributor().getActiveSegmentEdit();
		boolean visible = false;
		for (Iterator itAE = activeSegmentEdit.getArticleEdits().iterator(); itAE.hasNext(); ) {
			ArticleEdit articleEdit = (ArticleEdit) itAE.next();
			if (articleEdit.getArticleEditFactory().getArticleEditActionDelegate(this.getId()) != null)
				visible = true;
		}
		return visible;
	}

	public boolean calculateEnabled(Set<ArticleSelection> articleSelections)
	{
		boolean enabled = true;

		// Iterate all ArticleSelections.
		for (Iterator itAS = articleSelections.iterator(); itAS.hasNext(); ) {
			ArticleSelection articleSelection = (ArticleSelection) itAS.next();
			ArticleEditFactory articleEditFactory = articleSelection.getArticleEdit().getArticleEditFactory();

			// get the ActionDelegate for the ArticleEdit
			IArticleEditActionDelegate delegate = articleEditFactory.getArticleEditActionDelegate(this.getId());
			if (delegate == null) // if there's no delegate existing, we disable the action
				enabled = false;
			else {
				if (!delegate.calculateEnabled(articleSelection, articleSelections))
					enabled = false;
			}

			if (!enabled)
				break;
		} // for (Iterator itAS = articleSelections.iterator(); itAS.hasNext(); ) {

		return enabled;
	}

	/**
	 * This implementation calls
	 * {@link GeneralEditorActionBarContributor#articleEditActionDelegatesRun(IArticleEditAction)}.
	 *
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run()
	{
		GeneralEditorActionBarContributor contributor = getArticleEditActionRegistry().getActiveGeneralEditorActionBarContributor();
		if (contributor == null)
			throw new IllegalStateException("No activeGeneralEditorActionBarContributor set in ArticleEditActionRegistry!"); //$NON-NLS-1$

		contributor.articleEditActionDelegatesRun(this);
	}
}
