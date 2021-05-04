package com.elsevier.spark_xml_utils.common;

import java.io.ByteArrayInputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class IgnoreDoctype implements EntityResolver {

	 public InputSource resolveEntity(java.lang.String publicId, java.lang.String systemId)
             throws SAXException, java.io.IOException
      {
          // Ignore everything
          return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));

      }
	 
}
