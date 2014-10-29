/*
 * Copyright (c)2014 Elsevier, Inc.

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.elsevier.s3.SimpleStorageService;

/**
 * Class that provides XML transforming capabilities for an object in an S3
 * bucket or passed as a String.
 * 
 * @author Darin McBeath
 * 
 */
public class XSLTProcessor {

	// Stylesheet cache
	private static volatile HashMap<String, String> stylesheetMap = new HashMap<String, String>();

	// Logger
	private static Log log = LogFactory.getLog(XSLTProcessor.class);
	
	/**
	 * Transform the content in the S3 bucket using the stylesheet in the S3
	 * bucket.
	 * 
	 * @param stylesheetBucket
	 *            S3 bucket containing the stylesheet
	 * @param stylesheetKey
	 *            key identifying the stylesheet object
	 * @param contentBucket
	 *            S3 bucket containing the content
	 * @param contentKey
	 *            key identifying the content object
	 * @return transformed content
	 */
	public static String transform(String stylesheetBucket,
			String stylesheetKey, String contentBucket, String contentKey) {

		String content = null;
		String stylesheet = null;

		try {

			// Get the object from S3 and create streamsource for the content
			content = SimpleStorageService.getObject(contentBucket, contentKey);
			StreamSource contentSource = new StreamSource(
					IOUtils.toInputStream(content, CharEncoding.UTF_8));

			// Get the stylesheet and create streamsource for the stylesheet
			stylesheet = XSLTProcessor.getStylesheet(stylesheetBucket,
					stylesheetKey);
			StreamSource stylesheetSource = new StreamSource(
					IOUtils.toInputStream(stylesheet, CharEncoding.UTF_8));

			// Apply transformation
			return transform(contentSource, stylesheetSource);
			

		} catch (IOException e) {
			
			log.error("Problems transforming the content. STYLESHEET_BUCKET:" + stylesheetBucket + " STYLESHEET_KEY: " + stylesheetKey + " CONTENT_BUCKET:" + contentBucket + " CONTENT_KEY:" + contentKey + " " + e.getMessage(),e);
			return "</error>";
			
		} 

	}

	/**
	 * Transform the content using the stylesheet in the S3 bucket.
	 * 
	 * @param stylesheetBucket
	 *            S3 bucket containing the stylesheet
	 * @param stylesheetKey
	 *            key identifying the stylesheet object
	 * @param content
	 *            the xml to be transformed
	 * @return transformed content
	 */
	public static String transform(String stylesheetBucket,
			String stylesheetKey, String content)  {

		String stylesheet = null;

		try {

			// Get the stylesheet and create streamsource for the stylesheet
			stylesheet = XSLTProcessor.getStylesheet(stylesheetBucket,
					stylesheetKey);
			StreamSource stylesheetSource = new StreamSource(
					IOUtils.toInputStream(stylesheet, CharEncoding.UTF_8));

			// Create streamsource for the content
			StreamSource contentSource = new StreamSource(
					IOUtils.toInputStream(content, CharEncoding.UTF_8));

			// Apply transformation
			return transform(contentSource, stylesheetSource);

		} catch (IOException e) {
			
			log.error("Problems transforming the content. STYLESHEET_BUCKET:" + stylesheetBucket + " STYLESHEET_KEY: " + stylesheetKey + "  " + e.getMessage(),e);
			return "</error>";
			
		}

	}

	/**
	 * Transform the content with the specified stylesheet
	 * @param content the xml to be transformed
	 * @param stylesheet the stylesheet to apply to the xml content
	 * @return transformed content
	 */
	private static String transform(StreamSource content,
			StreamSource stylesheet) {
		
		try {
			
			// Get the processor
			Processor proc = new Processor(false);
			
			// Get the xslt compiler
			XsltCompiler xsltCompiler = proc.newXsltCompiler();
			
			// Set the uri resolver (for imported/included stylesheets)
			xsltCompiler.setURIResolver(new S3URIResolver());

			// Compile the stylesheet
			XsltExecutable exp = xsltCompiler.compile(stylesheet);
			
			// Set up the output for the transformation
			Serializer out = new Serializer();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			out.setOutputStream(baos);
			// Appears ok to always set output property to xml
			out.setOutputProperty(Serializer.Property.METHOD, "xml");
			out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION,"yes");
			out.setProcessor(proc);
			XsltTransformer trans = exp.load();
			trans.setDestination(out);
			
			// Set the content to use for the transformation
			trans.setSource(content);
			
			// Transform the content
			trans.transform();

			// Return the transformed content
			return new String(baos.toByteArray(), CharEncoding.UTF_8);

		} catch (IOException e) {
			
			log.error("Problems transforming the content.  " + e.getMessage(),e);
			return "<error/>";
			
		} catch (SaxonApiException e) {
			
			log.error("Problems transforming the content.  " + e.getMessage(),e);
			return "<error/>";
			
		}

	}

	/**
	 * Get the requested stylesheet.
	 * 
	 * @param stylesheetBucket
	 *            S3 bucket containing the stylesheet
	 * @param stylesheetKey
	 *            key identifying the stylesheet object
	 * @return stylesheet
	 * @throws IOException
	 */
	private static String getStylesheet(String stylesheetBucket,
			String stylesheetKey) throws IOException {

		String cacheKey = stylesheetBucket + stylesheetKey;

		if (stylesheetMap.containsKey(cacheKey)) {
			return stylesheetMap.get(cacheKey);
		} else {
			synchronized (XSLTProcessor.class) {
				if (!stylesheetMap.containsKey(cacheKey)) {
					stylesheetMap.put(cacheKey, SimpleStorageService.getObject(
							stylesheetBucket, stylesheetKey));
				}
			}
			return stylesheetMap.get(cacheKey);
		}

	}

	/**
	 * Init the XMLTransform. Set the S3 client.
	 */
	public static void init() {

		SimpleStorageService.init();

	}

	/**
	 * Clear the S3 client and empty the caches for stylesheets.
	 * and S3URIResolver
	 */
	public static void clear() {
		
		SimpleStorageService.clear();
		XSLTProcessor.emptyCache();
		S3URIResolver.emptyCache();
		SimpleStorageService.init();
		
	}

	/**
	 * Empty the stylesheet cache.
	 */
	private static void emptyCache() {

		if (stylesheetMap.size() > 0) {
			synchronized (XSLTProcessor.class) {
				if (stylesheetMap.size() > 0) {
					stylesheetMap.clear();
				}
			}
		}

	}

}
