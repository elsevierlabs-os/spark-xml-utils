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
package com.elsevier.spark_xml_utils.xpath;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Class with  methods to filter an XPath expression (return a TRUE/FALSE)
 * against a string of arbitrary xml content and evaluate an XPath expression (return 
 * a serialized response) against a string of arbitrary xml content.
 * 
 * @author Darin McBeath
 * 
 */
public class XPathProcessor implements Serializable {

	private static final long serialVersionUID = 7211034934310706719L;

	// Logger
	private static Log log = LogFactory.getLog(XPathProcessor.class);

	// Member variables
	private String xPathExpression = null;
	private HashMap<String,String> namespaceMappings = null;
	private HashMap<String,Object> featureMappings = null;
	private transient XPathSelector xsel = null;
	private transient DocumentBuilder builder = null;
	private transient Processor proc  = null;
	private transient Serializer serializer = null;
	private transient ByteArrayOutputStream baos = null;
	
	/** 
	 * Create an instance of XPathProcessor. 
	 * 
	 * @param xPathExpression XPath expression to apply to the content
	 * @param namespaceMappings Namespace prefix to Namespace uri mappings
	 * @param featureMappings Processor feature mappings
	 * @throws XPathException
	 */
	private XPathProcessor(String xPathExpression, HashMap<String,String> namespaceMappings, HashMap<String,Object> featureMappings) throws XPathException  {
	
		this.xPathExpression = xPathExpression;
		this.namespaceMappings = namespaceMappings;
		this.featureMappings = featureMappings;
		
	}

	
	/**
	 * Restore the serialized object and then do a one time initialization to improve
	 * performance for repetitive invocations of filter and evaluate expressions.  We need to
	 * initialize the transient variables.
	 * 
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws XPathException 
	 */
	private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException, XPathException
    {
		
        inputStream.defaultReadObject();
        init();
        
    } 

	
	/**
	 * Get an instance of XPathProcessor.
	 * 
	 * @param xPathExpression XPath expression to apply to the content
	 * @return XPathProcessor
	 * @throws XPathException 
	 */
	public static XPathProcessor getInstance(String xPathExpression) throws XPathException {
		
		XPathProcessor proc = new XPathProcessor(xPathExpression, null, null);	
		proc.init();
		return proc;
		
	}
	
	
	/**
	 * Get an instance of XPathProcessor.
	 * 
	 * @param xPathExpression XPath expression to apply to the content
	 * @param namespaceMappings Namespace prefix to Namespace uri mappings
	 * @return XPathProcessor
	 * @throws XPathException 
	 */
	public static XPathProcessor getInstance(String xPathExpression, HashMap<String,String> namespaceMappings) throws XPathException {
		
		XPathProcessor proc = new XPathProcessor(xPathExpression, namespaceMappings, null);	
		proc.init();
		return proc;
		
	}

	
	/**
	 * Get an instance of XPathProcessor.
	 * 
	 * @param xPathExpression XPath expression to apply to the content
	 * @param namespaceMappings Namespace prefix to Namespace uri mappings
	 * @param featureMappings Processor feature mappings
	 * @return XPathProcessor
	 * @throws XPathException 
	 */
	public static XPathProcessor getInstance(String xPathExpression, HashMap<String,String> namespaceMappings, HashMap<String,Object> featureMappings) throws XPathException {
		
		XPathProcessor proc = new XPathProcessor(xPathExpression, namespaceMappings, featureMappings);	
		proc.init();
		return proc;
		
	}
	
	/**
	 * Initialization to improve performance for repetitive invocations of filter and evaluate expressions
	 * 
	 * @throws XPathException
	 */
	private void init() throws XPathException {
		
		try {
		
			log.info("***** XPathProcessor init called.");
			
			// Get the processor
			proc = new Processor(false);

			// Set any specified configuration properties for the processor
			if (featureMappings != null) {
				for (Entry<String, Object> entry : featureMappings.entrySet()) {
					proc.setConfigurationProperty(entry.getKey(), entry.getValue());
				}
			}
			
			//proc.setConfigurationProperty(FeatureKeys.ENTITY_RESOLVER_CLASS, "com.elsevier.spark_xml_utils.common.IgnoreDoctype");
			
			// Get the XPath compiler
			XPathCompiler xpathCompiler = proc.newXPathCompiler();

			// Set the namespace to prefix mappings
			this.setPrefixNamespaceMappings(xpathCompiler, namespaceMappings);

			// Compile the XPath expression  and get a document builder
			xsel = xpathCompiler.compile(xPathExpression).load();
			builder = proc.newDocumentBuilder();
		
			// Create and initialize the serializer  
			baos = new ByteArrayOutputStream();
			serializer = proc.newSerializer(baos);
			serializer.setOutputStream(baos);
			serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
			serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION,"yes");			
			serializer.setProcessor(proc);
		
		} catch (SaxonApiException e) {
			
			log.error("Problems creating an XPathProcessor.  " + e.getMessage(),e);
			throw new XPathException(e.getMessage());

		}
		
	}
	
	
	/**
	 * Filter the content with the XPath expression specified when creating the XPathProcessor.
	 * 
	 * @param content String to which the XPath expression will be applied
	 * @return TRUE if the XPath expression evaluates to true, FALSE otherwise
	 * @throws XPathException
	 */
	public boolean filterString(String content) throws XPathException {

		try {

			return filter(
					new StreamSource(IOUtils.toInputStream(content,
							CharEncoding.UTF_8)));

		} catch (IOException e) {
			
			log.error("Problems processing the content.  " + e.getMessage(),e);
			throw new XPathException(e.getMessage());
			
		}

	}
	
	
	/**
	 * Evaluate the content with the XPath expression specified when creating the XPathProcessor
	 * and return a serialized response.
	 * 
	 * @param content String to which the XPath Expression will be evaluated
	 * @return Serialized response from the evaluation.  
	 * @throws XPathException
	 */
	public String evaluateString(String content) throws XPathException{

		try {
			
			return evaluate(
					new StreamSource(IOUtils.toInputStream(content,
							CharEncoding.UTF_8)));

		} catch (IOException e) {
			
			log.error("Problems processing the content.  " + e.getMessage(),e);
			throw new XPathException(e.getMessage());
			
		}

	}

	
	/**
	 * Filter the content with the XPath expression specified when creating the XPathProcessor.
	 * 
	 * @param content String to which the XPath expression will be applied
	 * @return TRUE if the XPath expression evaluates to true, FALSE otherwise
	 * @throws XPathException
	 */
	private  boolean filter(StreamSource content) throws XPathException {

		try {

			// Prepare to evaluate the XPath expression against the content
			XdmNode xmlDoc = builder.build(content);
			xsel.setContextItem(xmlDoc);

			// Evaluate and return the boolean value for the XPath expression
			return xsel.effectiveBooleanValue();

		} catch (SaxonApiException e) {
			
			log.error("Problems processing the content.  EXPRESSION:" + xPathExpression + " "  + e.getMessage(),e);
			throw new XPathException(e.getMessage());
			
		}

	}

	
	/**
	 * Evaluate the content with the XPath expression specified when creating the XPathProcessor
	 * and return a serialized response.
	 * 
	 * @param content StreamSource to which the XPath expression will be evaluated
	 * @return Serialized response from the evaluation.  
	 * @throws XPathException
	 */
	private  String evaluate(StreamSource content) throws XPathException {

		try {

			//Reset the serializer
			serializer.close();
			baos.reset();
			
			// Prepare to evaluate the XPath expression against the content
			XdmNode xmlDoc = builder.build(content);
			xsel.setContextItem(xmlDoc);

			
			// Evaluate the XPath expression
			XdmValue results = xsel.evaluate();
			Iterator<XdmItem> it = results.iterator();
			while (it.hasNext()) {
				XdmItem item = it.next();
				serializer.serializeXdmValue(item);
			}

			// Return the results
			return new String(baos.toByteArray(), CharEncoding.UTF_8);

		} catch (IOException e) {
			
			log.error("Problems processing the content.  EXPRESSION:" + xPathExpression + " "  + e.getMessage(),e);
			throw new XPathException(e.getMessage());
			
		} catch (SaxonApiException e) {
			
			log.error("Problems processing the content.  EXPRESSION:" + xPathExpression + " "  + e.getMessage(),e);
			throw new XPathException(e.getMessage());
			
		}

	}

	
	/**
	 * Set the namespaces in the XPathCompiler.
	 * 
	 * @param xpathCompiler
	 * @param namespaceMappings Namespace prefix to Namespace uri mappings
	 */
	private  void setPrefixNamespaceMappings(XPathCompiler xpathCompiler, HashMap<String,String> namespaceMappings) {

		if (namespaceMappings != null) {
			
			// Get the mappings
			Set<Entry<String, String>> mappings = namespaceMappings.entrySet();		
		
			// If mappings exist, set the namespaces
			if (mappings != null) {
			
				Iterator<Entry<String, String>> it = mappings.iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					xpathCompiler.declareNamespace(entry.getKey(), entry.getValue());					
				}
				
			}
			
		}
		
		// Add in the defaults	
		xpathCompiler.declareNamespace("xml",NamespaceConstant.XML);
		xpathCompiler.declareNamespace("xs",NamespaceConstant.SCHEMA);
		xpathCompiler.declareNamespace("fn",NamespaceConstant.FN);

	}

}
