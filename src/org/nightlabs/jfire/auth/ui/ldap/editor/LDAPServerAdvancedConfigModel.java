package org.nightlabs.jfire.auth.ui.ldap.editor;

import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;

/**
 * Simple model of {@link LDAPServer} to be used on UI in {@link LDAPServerAdvancedConfigSection}.
 * It just wraps around {@link LDAPServer} and performs some additional data transformation and 
 * verification.
 * TODO: hide sync password
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPServerAdvancedConfigModel{

	private LDAPServer ldapServer;
	
	/**
	 * Constructs a new {@link LDAPServerGeneralConfigModel}.
	 * Throws {@link IllegalArgumentException} if <code>null</code> is passed as argument.
	 * 
	 * @param ldapServer Instance being edited, can't be <code>null</code>
	 */
	public LDAPServerAdvancedConfigModel(LDAPServer ldapServer){
		if (ldapServer == null){
			throw new IllegalArgumentException("LDAPServer can't be null!");
		}
		this.ldapServer = ldapServer;
	}
	
	/**
	 * Get entry name which is used for synchronization binding
	 * 
	 * @return name of LDAP entry
	 */
	public String getSyncDN(){
		return ldapServer.getSyncDN()!=null?ldapServer.getSyncDN():"";
	}
	
	/**
	 * Set entry name which is used for synchronization binding
	 * 
	 * @param syncDN
	 */
	public void setSyncDN(String syncDN){
		if ("".equals(syncDN)){
			syncDN = null;
		}
		ldapServer.setSyncDN(syncDN);
	}
	
	/**
	 * Get password which is used for synchronization binding
	 * 
	 * @return password as simple {@link String}
	 */
	public String getSyncPassword(){
		return ldapServer.getSyncPassword()!=null?ldapServer.getSyncPassword():"";
	}
	
	/**
	 * Set password which is used for synchronization binding
	 * 
	 * @param syncPassword
	 */
	public void setSyncPassword(String syncPassword){
		if ("".equals(syncPassword)){
			syncPassword = null;
		}
		ldapServer.setSyncPassword(syncPassword);
	}
	
	/**
	 * Get {@link LDAPServer} leading state
	 * 
	 * @return <code>true</code> if {@link LDAPServer} is leading system
	 */
	public boolean isLeading(){
		return ldapServer.isLeading();
	}
	
	/**
	 * Set {@link LDAPServer} leading state
	 * 
	 * @param isLeading
	 */
	public void setLeading(boolean isLeading){
		ldapServer.setLeading(isLeading);
	}
	
}
