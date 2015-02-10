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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * URI Resolver that will be used by XLTProcessor when resolving xsl:import and
 * xsl:include statements. The purpose of this class is to create a local cache
 * of the stylesheets retrieved (presumably) from S3 so they don't need to be 
 * re-retrieved from S3 on subsequent requests.
 * 
 * @author Darin McBeath
 * 
 */
public class S3URIResolver implements URIResolver {

	// Stylesheet map cache
	private static volatile HashMap<String, String> stylesheetMap = new HashMap<String, String>();
	
	// Logger
	private static Log log = LogFactory.getLog(S3URIResolver.class);

	
	/**
	 * Return the requested stylesheet. If the stylesheet hasn't been cached,
	 * then save the stylesheet to the cache. The assumption (although not
	 * required) is that the imported/included stylesheets will be stored in an
	 * S3 bucket and accessible using an S3 url.
	 * 
	 * @param href url for the stylesheet
	 * @param base not used (assuming absolute urls)
	 */
	public Source resolve(String href, String base) throws TransformerException {

		try {
			// Check local cache
			if (stylesheetMap.containsKey(href)) {
				return new StreamSource(IOUtils.toInputStream(stylesheetMap.get(href)));						
			} else {

				// Read the data from the URL and populate the cache
				URL theUrl = new URL(href);
				synchronized (this) {
					stylesheetMap.put(href,IOUtils.toString(theUrl.openStream()));							
				}
				// Return a StreamSource
				return new StreamSource(IOUtils.toInputStream(stylesheetMap.get(href)));						
			}

		} catch (IOException e) {
			
			log.error("Problems resolving a stylesheet. URI:" + href + " " + e.getMessage(),e);
			throw new TransformerException(e.getMessage());
			
		}
	}

	
	/**
	 * Clear the cache for the imported/included stylesheets.
	 */
	public static void emptyCache() {
		if (stylesheetMap.size() > 0) {
			synchronized (S3URIResolver.class) {
				if (stylesheetMap.size() > 0) {
					stylesheetMap.clear();
				}
			}
		}
	}
	
}
