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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.trade.TradePlugin;
import org.nightlabs.jfire.trade.articlecontainer.detail.GeneralEditorInput;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.resource.Messages;
import org.nightlabs.util.Util;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class GeneralEditorInputOffer
extends GeneralEditorInput
implements IEditorInput
{
	private OfferID offerID;

	public GeneralEditorInputOffer()
	{
	}

	public GeneralEditorInputOffer(OfferID offerID)
	{
		this.offerID = offerID;
	}

	/**
	 * @return Returns the offerID.
	 */
	public OfferID getOfferID()
	{
		return offerID;
	}

	public int hashCode()
	{
		return offerID == null ? 0 : offerID.hashCode();
	}

	public boolean equals(Object obj)
	{
		if (obj == this) return true;

		if (!(obj instanceof GeneralEditorInputOffer))
			return false;

		GeneralEditorInputOffer other = (GeneralEditorInputOffer)obj;

		return Util.equals(this.offerID, other.offerID);
	}

	public String getName()
	{
		return String.format(
				Messages.getString("org.nightlabs.jfire.trade.articlecontainer.detail.offer.GeneralEditorInputOffer.name"), //$NON-NLS-1$
				(offerID == null ? "" : offerID.offerIDPrefix + '/' + ObjectIDUtil.longObjectIDFieldToString(offerID.offerID))); //$NON-NLS-1$
	}

	private static final String IMAGE = "icons/articlecontainer/detail/offer/GeneralEditorInputOffer.16x16.png"; //$NON-NLS-1$

	public ImageDescriptor getImageDescriptor()
	{
		return TradePlugin.imageDescriptorFromPlugin(TradePlugin.ID_PLUGIN, IMAGE);
	}

}
