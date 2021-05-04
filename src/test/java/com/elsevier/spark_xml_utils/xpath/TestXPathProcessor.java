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

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import net.sf.saxon.lib.FeatureKeys;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class TestXPathProcessor {
	
	@Test
	public void testFilterString1() {
		try {
			XPathProcessor proc = XPathProcessor.getInstance("/name[.='john']");
			assertEquals(true, proc.filterString("<name>john</name>"),"Should return true.");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFilterString2() {
		try {
			XPathProcessor proc = XPathProcessor.getInstance( "/name[.='joe']");
			assertEquals(false, proc.filterString("<name>john</name>"),"Should return false.");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testFilterString3() {
		try {
			HashMap<String,String> pfxUriMap = new HashMap<String,String>();
			pfxUriMap.put("a", "namespace1");
			pfxUriMap.put("b", "namespace2");
			pfxUriMap.put("c", "namespace3");
			XPathProcessor proc = XPathProcessor.getInstance("/a:name[.='john']", pfxUriMap);
			assertEquals(true, proc.filterString("<name xmlns='namespace1'>john</name>"),"Should return true.");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testFilterString4() {
		try {
			HashMap<String,String> pfxUriMap = new HashMap<String,String>();
			pfxUriMap.put("a", "namespace1");
			pfxUriMap.put("b", "namespace2");
			pfxUriMap.put("c", "namespace3");
			XPathProcessor proc = XPathProcessor.getInstance("/b:name[.='john']", pfxUriMap);
			assertEquals(false, proc.filterString("<name xmlns='namespace1'>john</name>"),"Should return false.");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFilterString5() {
		try {
			XPathProcessor proc = XPathProcessor.getInstance("/*:name[.='john']");
			assertEquals(true, proc.filterString("<name xmlns='namespace1'>john</name>"),"Should return true.");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testFilterString6() {
		try {
			XPathProcessor proc = XPathProcessor.getInstance( "/name[.='joe']");
			assertEquals(false, proc.filterString("<name>john</name>"),"Should return false.");
			assertEquals(true, proc.filterString("<name>joe</name>"),"Should return true.");
			assertEquals(false, proc.filterString("<name>jim</name>"),"Should return false.");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testFilter1() {
		try {
			XPathProcessor proc = XPathProcessor.getInstance( "/name[.='joe']");
			assertEquals(false, proc.filter("<name>john</name>"),"Should return false.");
			assertEquals(true, proc.filter("<name>joe</name>"),"Should return true.");
			assertEquals(false, proc.filter("<name>jim</name>"),"Should return false.");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 
	

	@Test
	public void testFilterStream1() {
		try {
			XPathProcessor proc = XPathProcessor.getInstance( "/name[.='joe']");
			String str = "<name>joe</name>";
			InputStream is = null;
			try {
				 is = IOUtils.toInputStream(str, StandardCharsets.UTF_8.name());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			assertEquals(true, proc.filter(is),"Should return true.");

		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} 

	@Test
	public void testFilterStream2() {
		try {
			XPathProcessor proc = XPathProcessor.getInstance( "/name[.='joe']");
			String str = "<name>jim</name>";
			InputStream is = null;
			try {
				is = IOUtils.toInputStream(str, StandardCharsets.UTF_8.name());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			assertEquals(false, proc.filter(is),"Should return false.");

		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEvaluateString1() {	
		try {
			XPathProcessor proc = XPathProcessor.getInstance("/name[.='john']");
			assertEquals("<name>john</name>", proc.evaluateString("<name>john</name>"),"Should match <name>john</name>.");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testEvaluateString2() {	
		try {
			XPathProcessor proc = XPathProcessor.getInstance("/name[.='joe']");
			assertEquals("", proc.evaluateString("<name>john</name>"),"Should match nothing.");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testEvaluateString3() {
		try {
			HashMap<String,String> pfxUriMap = new HashMap<String,String>();
			pfxUriMap.put("a", "namespace1");
			pfxUriMap.put("b", "namespace2");
			pfxUriMap.put("c", "namespace3");
			XPathProcessor proc = XPathProcessor.getInstance("/name[.='john']",pfxUriMap);
			assertEquals("", proc.evaluateString("<name xmlns='namespace1'>john</name>"),"Should match nothing.");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testEvaluateString4() {
		try {
			HashMap<String,String> pfxUriMap = new HashMap<String,String>();
			pfxUriMap.put("a", "namespace1");
			pfxUriMap.put("b", "namespace2");
			pfxUriMap.put("c", "namespace3");
			XPathProcessor proc = XPathProcessor.getInstance("/a:name[.='john']",pfxUriMap);
			assertEquals("<name xmlns=\"namespace1\">john</name>", proc.evaluateString("<name xmlns='namespace1'>john</name>"),"Should match <name xmlns='namespace1'>john</name>.");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testEvaluateString5() {
		try {
			HashMap<String,String> pfxUriMap = new HashMap<String,String>();
			pfxUriMap.put("a", "namespace1");
			pfxUriMap.put("b", "namespace2");
			pfxUriMap.put("c", "namespace3");
			XPathProcessor proc = XPathProcessor.getInstance("/*:name[.='john']",pfxUriMap);
			assertEquals("<name xmlns=\"namespace1\">john</name>", proc.evaluateString("<name xmlns='namespace1'>john</name>"),"Should match <name xmlns='namespace1'>john</name>.");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testEvaluateString6() {	
		try {
			XPathProcessor proc = XPathProcessor.getInstance("/name[.='joe']",null);
			assertEquals("", proc.evaluateString("<name>john</name>"),"Should match nothing.");
			assertEquals("<name>joe</name>", proc.evaluateString("<name>joe</name>"),"Should match <name>joe</name>.");

		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEvaluateString7() {	
		try {
			HashMap<String,Object> featureMap = new HashMap<String,Object>();
			featureMap.put(FeatureKeys.ENTITY_RESOLVER_CLASS, "com.elsevier.spark_xml_utils.common.IgnoreDoctype");
			XPathProcessor proc = XPathProcessor.getInstance("/books/book",null,featureMap);
			assertEquals("<book><title lang=\"en\">Harry Potter</title><author>J K. Rowling</author><year>2005</year><price>29.99</price></book>", proc.evaluateString("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE books SYSTEM \"sample.dtd\"><books><book><title lang=\"en\">Harry Potter</title><author>J K. Rowling</author><year>2005</year><price>29.99</price></book></books>"),"Should match <book><title lang=\"en\">Harry Potter</title><author>J K. Rowling</author><year>2005</year><price>29.99</price></book>");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEvaluateString8() {	
		try {
			HashMap<String,Object> featureMap = new HashMap<String,Object>();
			featureMap.put(FeatureKeys.ENTITY_RESOLVER_CLASS, "com.elsevier.spark_xml_utils.common.IgnoreDoctype");
			XPathProcessor proc = XPathProcessor.getInstance("/books/book",null,featureMap);
			assertEquals("<book><title lang=\"en\">Harry Potter</title><author>J K. Rowling</author><year>2005</year><price>29.99</price></book>", proc.evaluate("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE books SYSTEM \"sample.dtd\"><books><book><title lang=\"en\">Harry Potter</title><author>J K. Rowling</author><year>2005</year><price>29.99</price></book></books>"),"Should match <book><title lang=\"en\">Harry Potter</title><author>J K. Rowling</author><year>2005</year><price>29.99</price></book>");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testEvaluateStream1() {	
		try {
			HashMap<String,Object> featureMap = new HashMap<String,Object>();
			featureMap.put(FeatureKeys.ENTITY_RESOLVER_CLASS, "com.elsevier.spark_xml_utils.common.IgnoreDoctype");
			XPathProcessor proc = XPathProcessor.getInstance("/books/book",null,featureMap);
			String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE books SYSTEM \"sample.dtd\"><books><book><title lang=\"en\">Harry Potter</title><author>J K. Rowling</author><year>2005</year><price>29.99</price></book></books>";
			InputStream is = null;
			try {
				is = IOUtils.toInputStream(str, StandardCharsets.UTF_8.name());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			assertEquals("<book><title lang=\"en\">Harry Potter</title><author>J K. Rowling</author><year>2005</year><price>29.99</price></book>", proc.evaluate(is),"Should match <book><title lang=\"en\">Harry Potter</title><author>J K. Rowling</author><year>2005</year><price>29.99</price></book>");
		} catch (XPathException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
