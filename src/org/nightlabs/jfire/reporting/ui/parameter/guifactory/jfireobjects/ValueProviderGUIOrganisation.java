/**
 * 
 */
package org.nightlabs.jfire.reporting.ui.parameter.guifactory.jfireobjects;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.nightlabs.jfire.base.ui.security.UserSearchComposite;
import org.nightlabs.jfire.reporting.ReportingConstants;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;
import org.nightlabs.jfire.reporting.ui.parameter.IValueProviderGUI;
import org.nightlabs.jfire.reporting.ui.parameter.IValueProviderGUIFactory;
import org.nightlabs.jfire.security.id.UserID;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ValueProviderGUIOrganisation extends AbstractValueProviderGUIUser {
	
	public static class Factory implements IValueProviderGUIFactory {

		public IValueProviderGUI<UserID> createValueProviderGUI(ValueProviderConfig valueProviderConfig) {
			return new ValueProviderGUIOrganisation(valueProviderConfig);
		}

		public ValueProviderID getValueProviderID() {
			return ReportingConstants.VALUE_PROVIDER_ID_ORGANISATION;
		}

		public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		}
		
	}
	/**
	 * 
	 */
	public ValueProviderGUIOrganisation(ValueProviderConfig valueProviderConfig) {
		super(valueProviderConfig);
	}
	
	@Override
	protected int getTypeFlags() {
		return UserSearchComposite.FLAG_TYPE_ORGANISATION;
	}
}
