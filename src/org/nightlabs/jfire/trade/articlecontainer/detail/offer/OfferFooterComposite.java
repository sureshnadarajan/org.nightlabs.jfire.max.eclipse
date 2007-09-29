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

package org.nightlabs.jfire.trade.articlecontainer.detail.offer;

import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.TradePlugin;
import org.nightlabs.jfire.trade.articlecontainer.detail.FooterComposite;
import org.nightlabs.jfire.trade.articlecontainer.detail.GeneralEditorComposite;
import org.nightlabs.l10n.NumberFormatter;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class OfferFooterComposite
extends FooterComposite
{
	private Offer offer;

	public OfferFooterComposite(GeneralEditorComposite generalEditorComposite, Offer offer)
	{
		super(generalEditorComposite, generalEditorComposite, offer);
		this.offer = offer;
//		refresh(offer.getArticles());
	}

//	public void refresh() 
//	{
//		long priceAmount = 0;
//		for (Article article : offer.getArticles()) {
//			ArticlePrice articlePrice = article.getPrice();
//			priceAmount =+ articlePrice.getAmount(); 
//		}		
//		String price = NumberFormatter.formatCurrency(priceAmount , offer.getCurrency());		
//		setFooterText(TradePlugin.getResourceString("FooterComposite.totalPrice")+ " " + price);					
//	}
}
