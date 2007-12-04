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
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.reporting.oda.client.jdoql;

import java.util.Map;

import org.nightlabs.jfire.reporting.oda.Connection;
import org.nightlabs.jfire.reporting.oda.jdoql.IJDOQueryProxy;
import org.nightlabs.jfire.reporting.oda.jdoql.IJDOQueryProxyFactory;
import org.nightlabs.jfire.reporting.oda.jdoql.JDOQLConnection;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ClientJDOQLProxyFactory implements IJDOQueryProxyFactory {

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.reporting.admin.ui.oda.jdo.IJDOQueryProxyFactory#createJDOQueryProxy(java.util.Map)
	 */
	public IJDOQueryProxy createJDOQueryProxy(Map proxyProperties) {
		Connection.checkConnectonProperties(proxyProperties);
		return new ClientJDOQLProxy();
	}

}
