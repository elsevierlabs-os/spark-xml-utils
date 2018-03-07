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
package com.elsevier.spark_xml_utils.xslt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Class that provides xml transforming capabilities  (via xslt) for xml passed as a String.
 * 
 * @author Darin McBeath
 * 
 */
public class XSLTProcessor implements Serializable {

	private static final long serialVersionUID = -5320116690116312347L;

	// Logger
	private static Log log = LogFactory.getLog(XSLTProcessor.class);

	// Member variables
	private HashMap<String,Object> featureMappings = null;
	private String stylesheet = null;
	private transient Processor proc  = null;
	private transient Serializer serializer = null;
	private transient ByteArrayOutputStream baos = null;
	private transient XsltTransformer trans = null;
	private transient DocumentBuilder builder = null;
	
	
	/**
	 * Create an instance of XSLTProcessor.
	 *  
	 * @param stylesheet 
	 * @param featureMappings Processor feature mappings
	 * @throws XSLTException
	 */
	private XSLTProcessor(String stylesheet, HashMap<String,Object> featureMappings) throws XSLTException  {
		
		this.stylesheet = stylesheet;
		this.featureMappings = featureMappings;
		
	}
	

	/**
	 * Restore the serialized object and then do a one time initialization to improve
	 * performance for repetitive invocations of transformations.  We need to
	 * initialize the transient variables.
	 * 
	 * @param inputStream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws XSLTException 
	 */
	private void readObject(ObjectInputStream inputStream)
            throws IOException, ClassNotFoundException, XSLTException
    {
		
        inputStream.defaultReadObject();
        init();
        
    } 
	
	
	/**
	 * Get an instance of XSLTProcessor and then do a one time initialization to improve
	 * performance for repetitive invocations of transformations.
	 * 
	 * @param stylesheet
	 * @return XSLTProcessor
	 * @throws XSLTException 
	 */
	public static XSLTProcessor getInstance(String stylesheet) throws XSLTException {
			
		XSLTProcessor proc = new XSLTProcessor(stylesheet, null);	
		proc.init();
		return proc;
		
	}


	/**
	 * Get an instance of XSLTProcessor and then do a one time initialization to improve
	 * performance for repetitive invocations of transformations.
	 * 
	 * @param stylesheet
	 * @param featureMappings Processor feature mappings
	 * @return XSLTProcessor
	 * @throws XSLTException 
	 */
	public static XSLTProcessor getInstance(String stylesheet, HashMap<String,Object> featureMappings) throws XSLTException {
			
		XSLTProcessor proc = new XSLTProcessor(stylesheet, featureMappings);	
		proc.init();
		return proc;
		
	}
	
	
	/**
	 * Initialization to improve performance for repetitive invocations of transformations.
	 * 
	 * @throws XSLTException
	 */
	private void init() throws XSLTException {
		
		try {
			
			// Get the processor
			proc = new Processor(false);
		
			// Set any specified configuration properties for the processor
			if (featureMappings != null) {
				for (Entry<String, Object> entry : featureMappings.entrySet()) {
					proc.setConfigurationProperty(entry.getKey(), entry.getValue());
				}
			}
			
			// Get the xslt compiler
			XsltCompiler xsltCompiler = proc.newXsltCompiler();
			
			// Get the document builder (used for params)
			builder = proc.newDocumentBuilder();
		
			// Set the uri resolver (for imported/included stylesheets)
			xsltCompiler.setURIResolver(new S3URIResolver());
		
			// Compile the stylesheet
			XsltExecutable exp = xsltCompiler.compile(new StreamSource(IOUtils.toInputStream(stylesheet, CharEncoding.UTF_8)));
		
			// Set up the output for the transformation
			baos = new ByteArrayOutputStream();
			serializer = proc.newSerializer(baos);
			serializer.setOutputStream(baos);
			// Appears ok to always set output property to xml
			serializer.setOutputProperty(Serializer.Property.METHOD, "xml");
			serializer.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION,"yes");
			serializer.setProcessor(proc);
			trans = exp.load();
			trans.setDestination(serializer);
	
		} catch (SaxonApiException e) {
		
			log.error("Problems creating an XSLTProcessor.  " + e.getMessage(),e);
			throw new XSLTException(e.getMessage());

		} catch (IOException e) {
			
			log.error("Problems creating an XSLTProcessor.  " + e.getMessage(),e);
			throw new XSLTException(e.getMessage());
			
		} 
		
	}
	
	
	/**
	 * Set the output method (the default is xml).
	 * @param method
	 */
	public void setOutputMethod(String method)  {
		serializer.setOutputProperty(Serializer.Property.METHOD, method);
	}
	
	
	/**
	 * Transform the content.
	 * 
	 * @param content the xml to be transformed
	 * @return transformed content
	 * @throws XSLTException
	 */
	public String transform(String content) throws XSLTException {

		// Apply transformation
		return transform(content, new HashMap<String,String>());

	}

	
	/**
	 * Transform the content.
	 * 
	 * @param content the xml to be transformed
	 * @return transformed content
	 * @throws XSLTException
	 */
	public String transform(InputStream content) throws XSLTException {

		// Apply transformation
		return transform(new StreamSource(content), new HashMap<String,String>());

	}
	
	
	/**
	 * Transform the content.
	 * 
	 * @param content the xml to be transformed
	 * @param stylesheetParams HashMap of stylesheet params
	 * @return transformed content
	 * @throws XSLTException
	 */
	public String transform(String content, HashMap<String,String> stylesheetParams) throws XSLTException {

		try {

			// Create streamsource for the content
			StreamSource contentSource = new StreamSource(IOUtils.toInputStream(content, CharEncoding.UTF_8));

			// Apply transformation
			return transform(contentSource, stylesheetParams);

		} catch (IOException e) {
			
			log.error("Problems transforming the content. "  + e.getMessage(),e);
			throw new XSLTException(e.getMessage());
			
		} 

	}
	
	/**
	 * Transform the content.
	 * @param content the xml to be transformed
	 * @param stylesheetParams HashMap of stylesheet params
	 * @return transformed content
	 * @throw XSLTException
	 */
	private String transform(StreamSource content, HashMap<String,String> stylesheetParams) throws XSLTException {
		
		try {
			
			//Reset the serializer
			serializer.close();
			baos.reset();
			
			// Set stylesheet parameters (if any were specified)
			for (Entry<String, String> entry : stylesheetParams.entrySet()) {
				XdmValue xdmValue = builder.build(new StreamSource(IOUtils.toInputStream(entry.getValue(), CharEncoding.UTF_8)));
				trans.setParameter(new QName("",entry.getKey()), xdmValue);
			}
			
			
			// Set the content to use for the transformation
			trans.setSource(content);
			
			// Transform the content
			trans.transform();

			// Return the transformed content
			return new String(baos.toByteArray(), CharEncoding.UTF_8);

		} catch (IOException e) {
			
			log.error("Problems transforming the content.  " + e.getMessage(),e);
			throw new XSLTException(e.getMessage());
			
		} catch (SaxonApiException e) {
			
			log.error("Problems transforming the content.  " + e.getMessage(),e);
			throw new XSLTException(e.getMessage());
			
		} 

	}

}
