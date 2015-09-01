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
import java.io.ObjectInputStream;
import java.io.Serializable;

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
	private String stylesheet = null;
	private transient Processor proc  = null;
	private transient Serializer serializer = null;
	private transient ByteArrayOutputStream baos = null;
	private transient XsltTransformer trans = null;
	
	
	private XSLTProcessor(String stylesheet) throws XSLTException  {
		
		this.stylesheet = stylesheet;
		
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
	 * Get an instance of XSLTProcessor.
	 * 
	 * @param stylesheet
	 * @return XSLTProcessor
	 * @throws XSLTException 
	 */
	public static XSLTProcessor getInstance(String stylesheet) throws XSLTException {
			
		XSLTProcessor proc = new XSLTProcessor(stylesheet);	
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
		
			// Get the xslt compiler
			XsltCompiler xsltCompiler = proc.newXsltCompiler();
		
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
	 * Transform the content.
	 * 
	 * @param content the xml to be transformed
	 * @return transformed content
	 * @throws XSLTException
	 */
	public String transform(String content) throws XSLTException {

		try {

			// Create streamsource for the content
			StreamSource contentSource = new StreamSource(IOUtils.toInputStream(content, CharEncoding.UTF_8));

			// Apply transformation
			return transform(contentSource);

		} catch (IOException e) {
			
			log.error("Problems transforming the content. "  + e.getMessage(),e);
			throw new XSLTException(e.getMessage());
			
		} 

	}

	
	/**
	 * Transform the content.
	 * @param content the xml to be transformed
	 * @return transformed content
	 * @throw XSLTException
	 */
	private String transform(StreamSource content) throws XSLTException {
		
		try {
			
			//Reset the serializer
			serializer.close();
			baos.reset();
			
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