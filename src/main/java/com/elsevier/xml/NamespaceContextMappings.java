/*
 * Copyright (c)2015 Elsevier, Inc.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.elsevier.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Class which provides the ability to 'register' namespace prefix to namespace
 * uri mappings. This is necessary for the XPath/XQuery expression evaluation
 * when the xpath/xquery expression contains namespace prefixes. If the
 * xpath/xquery expression does not contain namespace prefixes, then there is no
 * need to register any mappings. Default mappings are provided for the
 * well-known namespace prefixes 'xml', 'fn', and 'xs'.
 * 
 * @author Darin McBeath
 * 
 */
public class NamespaceContextMappings implements NamespaceContext {

	// Namespace Prefix to Namespace URI mapping cache
	private volatile static HashMap<String, String> prefixNamespaceMap = null;

	// Logger
	private static Log log = LogFactory.getLog(NamespaceContextMappings.class);
	
	
	/**
	 * Init the cache with the passed HashMap of prefixes and namespaces. 
	 * 
	 * @param initPrefixNamespaceMap HashMap of prefixes and namespaces
	 */
	public static void init(HashMap<String,String> initPrefixNamespaceMap)  {

		if (prefixNamespaceMap == null) {
			synchronized (NamespaceContextMappings.class) {
				if (prefixNamespaceMap == null) {

					// Add the defaults
					prefixNamespaceMap = new HashMap<String, String>();
					prefixNamespaceMap.put("xml", XMLConstants.XML_NS_URI);
					prefixNamespaceMap.put("fn",
							"http://www.w3.org/2005/xpath-functions");
					prefixNamespaceMap.put("xs",
							"http://www.w3.org/2001/XMLSchema");
					
					// Add those passed to init
					prefixNamespaceMap.putAll(initPrefixNamespaceMap);

					// Output the list of namespace prefix to uri mappings
					Set<String> keys = prefixNamespaceMap.keySet();
					Iterator<String> it = keys.iterator();
					log.info("** Namespace Mappings **");
					while (it.hasNext()) {
						String key = it.next();
						String val = prefixNamespaceMap.get(key);
						log.info(key + "=" + val);
					}

				}
			}
		}

	}

	
	/**
	 * Clear the cache.
	 */
	public static void emptyCache() {

		if (prefixNamespaceMap != null) {
			synchronized (NamespaceContextMappings.class) {
				if (prefixNamespaceMap != null) {
					prefixNamespaceMap = null;
				}
			}
		}

	}

	
	/**
	 * Get the Namespace Prefix to Uri Mappings. This will be used by the
	 * XPathProcessor and XQueryProcessor when setting namespaces.
	 * 
	 * @return Mappings
	 */
	public static Set<Entry<String, String>> getMappings() {

		if (prefixNamespaceMap != null) {
			return prefixNamespaceMap.entrySet();
		} else {
			return null;
		}

	}

	
	/**
	 * Provide namespace prefix to namespace uri resolution capability. All of
	 * the namespace prefixed declared in XPath Expressions should be mapped to
	 * the correct URI (in the XML) otherwise the evaluation will not be
	 * correct.
	 * 
	 * @param prefix Namespace prefix
	 *            
	 * @return Namespace uri
	 */
	public String getNamespaceURI(String prefix) {
		
		if (prefix == null)
			throw new NullPointerException("Null prefix");
		String uri = prefixNamespaceMap.get(prefix);
		if (uri == null) {
			return XMLConstants.NULL_NS_URI;
		} else {
			return uri;
		}
		
	}

	
	public String getPrefix(String uri) {
		
		throw new UnsupportedOperationException();
		
	}

	
	public Iterator<String> getPrefixes(String uri) {
		
		throw new UnsupportedOperationException();
		
	}

}
