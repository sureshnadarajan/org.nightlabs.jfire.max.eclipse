/**
 *
 */
package org.nightlabs.jfire.trade.ui.articlecontainer.detail.action.openrelated;

import java.util.Set;

import org.eclipse.ui.PartInitException;
import org.nightlabs.base.ui.util.RCPUtil;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ui.articlecontainer.detail.ArticleContainerEdit;
import org.nightlabs.jfire.trade.ui.articlecontainer.detail.ArticleContainerEditor;
import org.nightlabs.jfire.trade.ui.articlecontainer.detail.ArticleContainerEditorInput;
import org.nightlabs.util.Util;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class OpenRelatedInvoiceAction extends OpenRelatedAction {

	@Override
	protected boolean calculateEnabledWithArticles(Set<Article> articles) {
		ArticleContainerEdit edit = getArticleEditActionRegistry().getActiveArticleContainerEdit();
//		setText(Messages.getString("org.nightlabs.jfire.trade.ui.articlecontainer.detail.action.openrelated.OpenRelatedInvoiceAction.action.text.disabled")); //$NON-NLS-1$
		InvoiceID invoiceID = getCommonInvoiceID(articles);
		setText(getText(invoiceID));
//		if (invoiceID != null) {
//			setText(
//					String.format(Messages.getString("org.nightlabs.jfire.trade.ui.articlecontainer.detail.action.openrelated.OpenRelatedInvoiceAction.action.text.enabled"), //$NON-NLS-1$
//					invoiceID.invoiceIDPrefix, ObjectIDUtil.longObjectIDFieldToString(invoiceID.invoiceID)
//				)
//			);
//		}
		return invoiceID != null && !(edit.getArticleContainerID() instanceof InvoiceID);
	}

	/**
	 * Extracts the InvoiceID common to all given articles or <code>null</code>.
	 * @param articles The articles to check.
	 */
	protected InvoiceID getCommonInvoiceID(Set<Article> articles) {
		InvoiceID invoiceID = null;
		boolean first = true;
		for (Article article : articles) {
			if (first) {
				invoiceID = article.getInvoiceID();
				first = false;
				continue;
			}
			if (!Util.equals(invoiceID, article.getInvoiceID()))
				return null;
		}
		return invoiceID;
	}

	@Override
	public void run() {
		InvoiceID invoiceID = getCommonInvoiceID(getArticles());
		if (invoiceID == null)
			return;
		try {
			RCPUtil.openEditor(new ArticleContainerEditorInput(invoiceID), ArticleContainerEditor.ID_EDITOR);
		} catch (PartInitException e) {
			throw new RuntimeException(e);
		}
	}

}
