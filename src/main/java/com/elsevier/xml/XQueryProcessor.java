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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryEvaluator;
import net.sf.saxon.s9api.XQueryExecutable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Class with static methods to apply an XQuery expression against a string of
 * arbitrary xml content.
 * 
 * @author Darin McBeath
 * 
 */
public class XQueryProcessor {

	// Logger
	private static Log log = LogFactory.getLog(XQueryProcessor.class);

	
	/**
	 * Init the NamespaceContext. Set the prefix to namespace mappings specified 
	 * in the passed HashMap.  
	 * 
	 * @param prefixesNamespaces HashMap of namespace prefix / namespace uri mappings

	 */
	public static void init(HashMap<String,String> prefixesNamespaces) throws IOException {

		NamespaceContextMappings.init(prefixesNamespaces);

	}

	
	/**
	 * Clear namespace prefix / namespace uri mappings. Useful if you want to use a 
	 * different set of prefix/namespace mappings.
	 */
	public static void clear() {

		NamespaceContextMappings.emptyCache();

	}


	/**
	 * Apply the xqueryExpression to the specified string and return a
	 * serialized response.
	 * 
	 * @param content
	 *            String to which the xqueryExpression will be applied
	 * @param xqueryExpression
	 *            XQuery expression to apply to the content
	 * 
	 * @return Serialized response from the evaluation. If an error, the response will be "<error/>".
	 */
	public static String evaluateString(String content, String xqueryExpression) {

		try {

			return evaluate(new StreamSource(IOUtils.toInputStream(content,CharEncoding.UTF_8)), xqueryExpression);

		} catch (IOException e) {
			
			log.error("Problems processing the content.  " + e.getMessage(),e);
			return "<error/>";
			
		}

	}

	
	/**
	 * Apply xqueryExpression to the content and return a serialized response
	 * 
	 * @param content
	 *            StreamSource of content for which the xqueryExpression will be
	 *            applied
	 * @param xqueryExpression
	 *            XQuery expression to apply agains the content
	 * @return Serialized response from the evaluation
	 * @throws IOException 
	 */
	private static String evaluate(StreamSource content, String xqueryExpression) throws IOException {

		try {

			// Get the processor
			Processor proc = new Processor(false);

			// Compile the xquery
			XQueryCompiler xqueryCompiler = proc.newXQueryCompiler();
			xqueryCompiler.setEncoding(CharEncoding.UTF_8);

			// Set the namespace to prefix mappings
			setPrefixNamespaceMappings(xqueryCompiler);

			XQueryExecutable exp = xqueryCompiler.compile(xqueryExpression);

			// Get the evaluator, set the source (content)
			XQueryEvaluator eval = exp.load();
			eval.setSource(content);

			// Create an output serializer
			Serializer out = new Serializer();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			out.setOutputStream(baos);
			// Appears ok to always set output property to xml (even if we are just returning a text string)
			out.setOutputProperty(Serializer.Property.METHOD, "xml");
			out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION,"yes");
			out.setProcessor(proc);

			// Run the query
			eval.run(out);

			// Return the results
			return new String(baos.toByteArray(), CharEncoding.UTF_8);

		} catch (IOException e) {
			
			log.error("Problems processing the content.  EXPRESSION:" + xqueryExpression + " "  + e.getMessage(),e);
			return "<error/>";
			
		} catch (SaxonApiException e) {
			
			log.error("Problems processing the content.  EXPRESSION:" + xqueryExpression + " "  + e.getMessage(),e);
			return "<error/>";
			
		}

	}

	
	/**
	 * Set the namespaces in the XQueryCompiler.
	 * 
	 * @param xqueryCompiler
	 */
	private static void setPrefixNamespaceMappings(XQueryCompiler xqueryCompiler) {

		// Get the mappings
		Set<Entry<String, String>> mappings = NamespaceContextMappings.getMappings();

		// If mappings exist, set the namespaces
		if (mappings != null) {
			synchronized (XQueryProcessor.class) {
				if (mappings != null) {
					Iterator<Entry<String, String>> it = mappings.iterator();
					while (it.hasNext()) {
						Entry<String, String> entry = it.next();
						xqueryCompiler.declareNamespace(entry.getKey(),entry.getValue());
					}
				}
			}
		}

	}

}
