/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.agapsys.web.toolkit.modules;

import com.agapsys.web.toolkit.AbstractWebApplication;
import com.agapsys.web.toolkit.Module;
import com.agapsys.web.toolkit.modules.LdapModule.LdapException.LdapExceptionType;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class LdapModule extends Module {
	// CLASS SCOPE =============================================================
	public static class LdapException extends Exception {
		// CLASS SCOPE =========================================================
		public static enum LdapExceptionType {
			INVALID_CREDENTIALS,
			AUTHENTICATION_NOT_SUPPORTED,
			COMMUNICATION_FAILURE,
			NAMING_ERROR
		}
		// =====================================================================

		// INSTANCE SCOPE ======================================================
		private final LdapExceptionType exceptionType;

		private LdapException(LdapExceptionType exceptionType, String message, Throwable cause) {
			super(message, cause);
			this.exceptionType = exceptionType;
		}

		private LdapException(LdapExceptionType exceptionType, Throwable cause) {
			super(cause);
			this.exceptionType = exceptionType;
		}

		public LdapExceptionType getExceptionType() {
			return exceptionType;
		}
		// =====================================================================
	}

	public static class LdapAttribute {
		private final String name;
		private final List<String> values = new LinkedList<>();
		private final List<String> unmodifiableValues = Collections.unmodifiableList(values);

		private LdapAttribute(Attribute attribute) throws NamingException {
			this.name = attribute.getID();

			NamingEnumeration nem = attribute.getAll();

			while(nem.hasMoreElements()) {
				Object valueObj = nem.next();

				if (valueObj instanceof String)
					this.values.add(valueObj.toString());
			}
		}

		public String getName() {
			return name;
		}

		public List<String> getValues() {
			return unmodifiableValues;
		}

		@Override
		public String toString() {
			return String.format("%s: %s", getName(), getValues().toString());
		}
	}

	public static class LdapUser {
		private final String dn;
		private final List<LdapAttribute> attributes = new LinkedList<>();
		private final List<LdapAttribute> unmodifiableAttributes = Collections.unmodifiableList(attributes);

		private LdapUser(String dn, Attributes coreAttributes) throws NamingException {
			this.dn = dn;

			NamingEnumeration<? extends Attribute> attrs = coreAttributes.getAll();

			while(attrs.hasMoreElements()) {
				Attribute attr = attrs.next();
				this.attributes.add(new LdapAttribute(attr));
			}
		}

		public String getDn() {
			return dn;
		}

		public List<LdapAttribute> getAttributes() {
			return unmodifiableAttributes;
		}
	}

	public static final String SETTINGS_GROUP_NAME = LdapModule.class.getName();

	public static final String KEY_LDAP_URL             = SETTINGS_GROUP_NAME + ".url";
	public static final String KEY_SEARCH_BASE_DN       = SETTINGS_GROUP_NAME + ".baseDn";
	public static final String KEY_SEARCH_PATTERN       = SETTINGS_GROUP_NAME + ".searchPattern";
	public static final String KEY_SEARCH_USER_DN       = SETTINGS_GROUP_NAME + ".searchUserDn";
	public static final String KEY_SEARCH_USER_PASSWORD = SETTINGS_GROUP_NAME + ".searchUserPassword";

	private static final String DEFAULT_LDAP_URL             = "ldaps://ldap.server:9876";
	private static final String DEFAULT_SEARCH_BASE_DN       = "ou=users,dc=ldap,dc=server";
	private static final String DEFAULT_SEARCH_PATTERN       = "(&(objectClass=uidObject)(uid=%s))";
	private static final String DEFAULT_SEARCH_USER_DN       = "cn=admin,dc=ldap,dc=sever";
	private static final String DEFAULT_SEARCH_USER_PASSWORD = "password";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private String ldapUrl;
	private String searchBaseDn;
	private String searchPattern;
	private String searchUserDn;
	private char[] searchUserPassword;

	public LdapModule() {
		reset();
	}

	private void reset() {
		ldapUrl            = null;
		searchBaseDn       = null;
		searchPattern      = null;
		searchUserDn       = null;
		searchUserPassword = null;
	}

	@Override
	protected final String getSettingsGroupName() {
		return SETTINGS_GROUP_NAME;
	}

	@Override
	public Properties getDefaultProperties() {
		Properties defaultProperties = super.getDefaultProperties();

		defaultProperties.setProperty(KEY_LDAP_URL,             DEFAULT_LDAP_URL);
		defaultProperties.setProperty(KEY_SEARCH_BASE_DN,       DEFAULT_SEARCH_BASE_DN);
		defaultProperties.setProperty(KEY_SEARCH_PATTERN,       DEFAULT_SEARCH_PATTERN);
		defaultProperties.setProperty(KEY_SEARCH_USER_DN,       DEFAULT_SEARCH_USER_DN);
		defaultProperties.setProperty(KEY_SEARCH_USER_PASSWORD, DEFAULT_SEARCH_USER_PASSWORD);

		return defaultProperties;
	}

	@Override
	protected void onInit(AbstractWebApplication webApp) {
		super.onInit(webApp);

		reset();

		Properties props = getProperties();

		ldapUrl            = getProperty(props, KEY_LDAP_URL);
		searchBaseDn       = getProperty(props, KEY_SEARCH_BASE_DN);
		searchPattern      = getProperty(props, KEY_SEARCH_PATTERN);
		searchUserDn       = getProperty(props, KEY_SEARCH_USER_DN);
		searchUserPassword = getProperty(props, KEY_SEARCH_USER_PASSWORD).toCharArray();
	}


	protected String getLdapUrl() {
		return ldapUrl;
	}

	protected String getSearchBaseDn() {
		return searchBaseDn;
	}

	protected String getSearchPattern() {
		return searchPattern;
	}

	protected String getSearchUserDn() {
		return searchUserDn;
	}

	protected char[] getSearchUserPassword() {
		return searchUserPassword;
	}

	private DirContext getContext(String url, String userDn, char[] password) throws LdapException {
		Hashtable<String, Object> properties = new Hashtable<>();

		properties.put(Context.PROVIDER_URL,            url);
		properties.put(Context.SECURITY_PRINCIPAL,      userDn);
		properties.put(Context.SECURITY_CREDENTIALS,    password);
		properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		properties.put(Context.URL_PKG_PREFIXES,        "com.sun.jndi.url");
		properties.put(Context.REFERRAL,                "ignore");
		properties.put(Context.SECURITY_AUTHENTICATION, "simple");

		try {
			return new InitialDirContext(properties);
		} catch (AuthenticationException ex) {
			throw new LdapException(LdapExceptionType.INVALID_CREDENTIALS, String.format("Invalid credentials for %s", userDn), ex);
		} catch (AuthenticationNotSupportedException ex) {
			throw new LdapException(LdapExceptionType.AUTHENTICATION_NOT_SUPPORTED, "Authentication not supported", ex);
		} catch (CommunicationException ex) {
			throw new LdapException(LdapExceptionType.COMMUNICATION_FAILURE, "Communication failure", ex);
		} catch (NamingException ex) {
			throw new LdapException(LdapExceptionType.NAMING_ERROR, ex);
		}
	}

	private SearchResult searchUser(DirContext ctx, String searchBase, String searchPattern, String userId) throws LdapException {
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

			NamingEnumeration<SearchResult> results = ctx.search(
				searchBase,
				String.format(searchPattern, userId),
				constraints
			);

			if (results.hasMoreElements()) {
				SearchResult sr = (SearchResult) results.next();
				return sr;
			} else {
				return null;
			}
		} catch (NamingException ex) {
			throw new LdapException(LdapExceptionType.NAMING_ERROR, ex);
		}
	}

	private LdapUser _getUser(String userId, char[] password) throws LdapException, NamingException {
		DirContext ctx;
		SearchResult searchResult;
		String userDn = null;

		ctx = getContext(getLdapUrl(), getSearchUserDn(), getSearchUserPassword());
		searchResult = searchUser(ctx, getSearchBaseDn(), getSearchPattern(), userId);

		boolean found;
		if (searchResult != null) {
			userDn = searchResult.getNameInNamespace();
			found = true;
		} else {
			found = false;
		}

		ctx.close();
		ctx = null;

		if (found) {
			// Once a user is found, try to authenticate it
			try {
				ctx = getContext(getLdapUrl(), userDn, password);
				return new LdapUser(userDn, ctx.getAttributes(userDn));
			} catch (LdapException ex) {
				if (ex.getExceptionType() == LdapExceptionType.INVALID_CREDENTIALS) return null;
				throw ex;
			} finally {
				if (ctx != null) ctx.close();
			}
		} else {
			return null;
		}
	}

	public final LdapUser getUser(String userId, char[] password) throws LdapException {
		if (!isActive()) throw new RuntimeException("Module is not active");

		try {
			return _getUser(userId, password);
		} catch (NamingException ex) {
			throw new LdapException(LdapExceptionType.NAMING_ERROR, ex);
		}
	}
	// =========================================================================
}
