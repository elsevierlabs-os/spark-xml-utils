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
package com.elsevier.spark_xml_utils.xquery;

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
 * Class with methods to apply an XQuery expression against a string of
 * arbitrary xml content.
 * 
 * @author Darin McBeath
 * 
 */
public class XQueryProcessor implements Serializable {

	private static final long serialVersionUID = 4059250550297763372L;

	// Logger
	private static Log log = LogFactory.getLog(XQueryProcessor.class);

	// Member variables
	private String xQueryExpression = null;
	private HashMap<String,String> namespaceMappings = null;
	private transient Processor proc  = null;
	private transient XQueryExecutable exp = null;
	private transient XQueryEvaluator eval = null;
	private transient Serializer serializer = null;
	private transient ByteArrayOutputStream baos = null;

	
	/** 
	 * Private constructor.  Create an instance of XQueryProcessor and do a one time initialization to improve
	 * performance for repetitive invocations of evaluate expressions. 
	 * 
	 * @param xQueryExpression XQuery expression to apply to the content
	 * @param namespaceMappings Namespace prefix to Namespace uri mappings
	 * @throws XQueryException
	 */
	private XQueryProcessor(String xQueryExpression, HashMap<String,String> namespaceMappings) throws XQueryException  {
			
		this.xQueryExpression = xQueryExpression;
		this.namespaceMappings = namespaceMappings;
		
	}

	
	/**
	 * Restore the serialized object and then do a one time initialization to improve
	 * performance for repetitive invocations of evaluate expressions.  We need to
	 * initialize the transient variables.
	 * 
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws XQueryException 
	 */
	private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException, XQueryException
    {
		
        inputStream.defaultReadObject();
        init();
        
    } 
	
	
	/**
	 * Get an instance of XQueryProcessor.
	 * 
	 * @param xQueryExpression XQuery expression to apply to the content
	 * @return XQueryProcessor
	 * @throws XQueryException 
	 */
	public static XQueryProcessor getInstance(String xQueryExpression) throws XQueryException {
			
		XQueryProcessor proc = new XQueryProcessor(xQueryExpression, null);	
		proc.init();
		return proc;
		
	}
	
	
	/**
	 * Get an instance of XQueryProcessor.
	 * 
	 * @param xQueryExpression XQuery expression to apply to the content
	 * @param namespaceMappings Namespace prefix to Namespace uri mappings
	 * @return XQueryProcessor
	 * @throws XQueryException 
	 */
	public static XQueryProcessor getInstance(String xQueryExpression, HashMap<String,String> namespaceMappings) throws XQueryException {
			
		XQueryProcessor proc = new XQueryProcessor(xQueryExpression, namespaceMappings);	
		proc.init();
		return proc;
		
	}
	

	/**
	 * Initialization to improve performance for repetitive invocations of evaluate expressions
	 * 
	 * @throws XQueryException
	 */
	private void init() throws XQueryException {
		
		try {
			
			// Get the processor
			proc = new Processor(false);

			// Get the XQuery compiler
			XQueryCompiler xqueryCompiler = proc.newXQueryCompiler();
			xqueryCompiler.setEncoding(CharEncoding.UTF_8);

			// Set the namespace to prefix mappings
			this.setPrefixNamespaceMappings(xqueryCompiler, namespaceMappings);

			// Compile the XQuery expression and get an XQuery evaluator
			exp = xqueryCompiler.compile(xQueryExpression);
			eval = exp.load();
			
			// Create and initialize the serializer 
			baos = new ByteArrayOutputStream();
			serializer = proc.newSerializer(baos);
			// Appears ok to always set output property to xml (even if we are just returning a text string)
			serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
			serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION,"yes");
			serializer.setProcessor(proc);
			
		} catch (SaxonApiException e) {
			
			log.error("Problems creating an XQueryProcessor.  " + e.getMessage(),e);
			throw new XQueryException(e.getMessage());

		}
		
	}
	
	
	/**
	 * Evaluate the content with the XQuery expression specified when creating the XQueryProcessor
	 * and return a serialized response.
	 * 
	 * @param content String to which the XQuery Expression will be evaluated
	 * @return Serialized response from the evaluation. 
	 * @throws XQueryException
	 */
	public String evaluateString(String content) throws XQueryException {

		try {

			return evaluate(new StreamSource(IOUtils.toInputStream(content,CharEncoding.UTF_8)));

		} catch (IOException e) {
			
			log.error("Problems processing the content.  " + e.getMessage(),e);
			throw new XQueryException(e.getMessage());
			
		}

	}

	
	/**
	 * Evaluate the content with the XQuery expression specified when creating the XQueryProcessor
	 * and return a serialized response.
	 * 
	 * @param content StreamSource to which the XQuery expression will be evaluated
	 * @return Serialized response from the evaluation
	 * @throws XQueryException 
	 */
	private String evaluate(StreamSource content) throws XQueryException {

		try {

			//Reset the serializer
			serializer.close();
			baos.reset();
			
			// Set the source (content)
			eval.setSource(content);

			// Run the query
			eval.run(serializer);

			// Return the results
			return new String(baos.toByteArray(), CharEncoding.UTF_8);

		} catch (IOException e) {
			
			log.error("Problems processing the content.  EXPRESSION:" + xQueryExpression + " "  + e.getMessage(),e);
			throw new XQueryException(e.getMessage());
			
		} catch (SaxonApiException e) {
			
			log.error("Problems processing the content.  EXPRESSION:" + xQueryExpression + " "  + e.getMessage(),e);
			throw new XQueryException(e.getMessage());
			
		}

	}

	
	/**
	 * Set the namespaces in the XQueryCompiler.
	 * 
	 * @param xqueryCompiler
	 * @param namespaceMappings Namespace prefix to Namespace uri mappings
	 */
	private  void setPrefixNamespaceMappings(XQueryCompiler xqueryCompiler, HashMap<String,String> namespaceMappings) {

		if (namespaceMappings != null) {
			
			// Get the mappings
			Set<Entry<String, String>> mappings = namespaceMappings.entrySet();

			// If mappings exist, set the namespaces
			if (mappings != null) {
			
				Iterator<Entry<String, String>> it = mappings.iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					xqueryCompiler.declareNamespace(entry.getKey(),entry.getValue());
				}
			
			}	
			
		}
		
		// Add in the defaults
		xqueryCompiler.declareNamespace("xml",NamespaceConstant.XML);
		xqueryCompiler.declareNamespace("xs",NamespaceConstant.SCHEMA);
		xqueryCompiler.declareNamespace("fn",NamespaceConstant.FN);
		
	}

}
