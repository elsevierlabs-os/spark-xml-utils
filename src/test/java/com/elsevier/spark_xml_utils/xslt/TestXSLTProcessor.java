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

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.sf.saxon.lib.FeatureKeys;

public class TestXSLTProcessor {

	private static String stylesheet1 = "<?xml version='1.0' encoding='UTF-8'?>"
			+ "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0' xmlns:xocs='http://www.elsevier.com/xml/xocs/dtd'>"
			+ "<xsl:output method='text' encoding='utf-8' indent='yes'/>"
			+ "<xsl:template match='/xocs:doc/xocs:meta'>"
			+ "<xsl:text>{ </xsl:text>"
			+ "<xsl:text>'srctitle':'</xsl:text><xsl:value-of select='./xocs:srctitle/text()'/><xsl:text>'</xsl:text>"
			+ "<xsl:text> }</xsl:text>"
			+ "</xsl:template>"
			+ "<xsl:template match='text()'/>"
			+ "</xsl:stylesheet>";

	private static String stylesheet2 = "<?xml version='1.0' encoding='UTF-8'?>"
			+ "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>"
			+ "<xsl:output method='text' encoding='utf-8' indent='yes'/>"
			+ "<xsl:template match='/books/book'>"
			+ "<xsl:text>{ </xsl:text>"
			+ "<xsl:text>'title':'</xsl:text><xsl:value-of select='./title/text()'/><xsl:text>'</xsl:text>"
			+ "<xsl:text> }</xsl:text>"
			+ "</xsl:template>"
			+ "<xsl:template match='text()'/>"
			+ "</xsl:stylesheet>";
	
	private static String stylesheet3 = "<?xml version='1.0' encoding='UTF-8'?>"
			+ "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>"
			+ "<xsl:output method='xml' encoding='utf-8' indent='no'/>"
			+ "<xsl:param name='givenName' required='yes'/>" 
			+ "<xsl:param name='surName' required='yes'/>" 
			+ "<xsl:param name='age' required='yes'/>" 
			+ "<xsl:template match='/'>"
            + "<name>"
            + "<given-name><xsl:value-of select='$givenName//text()'/></given-name>"
            + "<surname><xsl:value-of select='$surName//text()'/></surname>"
            + "<xsl:copy-of select='$age'/>"
            + "</name>"
            + "</xsl:template>"
			+ "</xsl:stylesheet>";
	
	
	@Test
	public void testTransform1() {
		
		try {
			XSLTProcessor proc = XSLTProcessor.getInstance(stylesheet1);
			assertEquals("{ 'srctitle':'Biochemical and Biophysical Research Communications' }",proc.transform("<xocs:doc xsi:schemaLocation=\"http://www.elsevier.com/xml/xocs/dtd http://schema.elsevier.com/dtds/document/fulltext/xcr/xocs-article.xsd\" xmlns:xocs=\"http://www.elsevier.com/xml/xocs/dtd\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.elsevier.com/xml/ja/dtd\" xmlns:ja=\"http://www.elsevier.com/xml/ja/dtd\" xmlns:mml=\"http://www.w3.org/1998/Math/MathML\" xmlns:tb=\"http://www.elsevier.com/xml/common/table/dtd\" xmlns:sb=\"http://www.elsevier.com/xml/common/struct-bib/dtd\" xmlns:ce=\"http://www.elsevier.com/xml/common/dtd\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:cals=\"http://www.elsevier.com/xml/common/cals/dtd\"><xocs:meta><xocs:content-family>serial</xocs:content-family><xocs:content-type>JL</xocs:content-type><xocs:cid>272308</xocs:cid><xocs:srctitle>Biochemical and Biophysical Research Communications</xocs:srctitle><xocs:normalized-srctitle>BIOCHEMICALBIOPHYSICALRESEARCHCOMMUNICATIONS</xocs:normalized-srctitle><xocs:orig-load-date yyyymmdd=\"20020419\">2002-04-19</xocs:orig-load-date><xocs:ew-transaction-id>2010-11-13T15:26:06</xocs:ew-transaction-id><xocs:eid>1-s2.0-S0006291X96917131</xocs:eid><xocs:pii-formatted>S0006-291X(96)91713-1</xocs:pii-formatted><xocs:pii-unformatted>S0006291X96917131</xocs:pii-unformatted><xocs:doi>10.1006/bbrc.1996.1713</xocs:doi><xocs:item-stage>S300</xocs:item-stage><xocs:item-version-number>S300.1</xocs:item-version-number><xocs:item-weight>HEAD-ONLY</xocs:item-weight><xocs:hub-eid>1-s2.0-S0006291X00X0160X</xocs:hub-eid><xocs:timestamp yyyymmdd=\"20101204\">2010-12-04T00:24:31.901978-05:00</xocs:timestamp><xocs:dco>0</xocs:dco><xocs:tomb>0</xocs:tomb><xocs:date-search-begin>19961121</xocs:date-search-begin><xocs:year-nav>1996</xocs:year-nav><xocs:indexeddate epoch=\"1019174400\">2002-04-19T00:00:00Z</xocs:indexeddate><xocs:articleinfo>articleinfo crossmark dco dateupdated tomb dateloaded datesearch indexeddate issuelist volumelist yearnav articletitlenorm authfirstinitialnorm authfirstsurnamenorm cid cids contenttype copyright dateloadedtxt docsubtype doctype doi eid ewtransactionid hubeid issfirst issn issnnorm itemstage itemtransactionid itemweight openaccess openarchive pg pgfirst pglast pii piinorm pubdatestart pubdatetxt pubyr sortorder srctitle srctitlenorm srctype volfirst volissue webpdf webpdfpagecount affil articletitle auth authfirstini authfull authlast footnotes primabst pubtype</xocs:articleinfo><xocs:issns><xocs:issn-primary-formatted>0006-291X</xocs:issn-primary-formatted><xocs:issn-primary-unformatted>0006291X</xocs:issn-primary-unformatted></xocs:issns><xocs:crossmark is-crossmark=\"0\"/><xocs:vol-first>228</xocs:vol-first><xocs:volume-list><xocs:volume>228</xocs:volume></xocs:volume-list><xocs:iss-first>3</xocs:iss-first><xocs:issue-list><xocs:issue>3</xocs:issue></xocs:issue-list><xocs:vol-iss-suppl-text>Volume 228, Issue 3</xocs:vol-iss-suppl-text><xocs:sort-order>3</xocs:sort-order><xocs:first-fp>655</xocs:first-fp><xocs:last-lp>661</xocs:last-lp><xocs:pages><xocs:first-page>655</xocs:first-page><xocs:last-page>661</xocs:last-page></xocs:pages><xocs:cover-date-orig><xocs:start-date>19961121</xocs:start-date></xocs:cover-date-orig><xocs:cover-date-text>21 November 1996</xocs:cover-date-text><xocs:cover-date-start>1996-11-21</xocs:cover-date-start><xocs:cover-date-year>1996</xocs:cover-date-year><xocs:document-type>converted-article</xocs:document-type><xocs:document-subtype>fla</xocs:document-subtype><xocs:copyright-line>Copyright © 1996 Academic Press. All rights reserved.</xocs:copyright-line><xocs:normalized-article-title>INHIBITORSPREADIPOCYTEDIFFERENTIATIONINDUCECOUPTFBINDINGAPPARRXRBINDINGSEQUENCE</xocs:normalized-article-title><xocs:normalized-first-auth-surname>BRODIE</xocs:normalized-first-auth-surname><xocs:normalized-first-auth-initial>A</xocs:normalized-first-auth-initial><xocs:attachment-metadata-doc><xocs:attachment-set-type>item</xocs:attachment-set-type><xocs:pii-formatted>S0006-291X(96)91713-1</xocs:pii-formatted><xocs:pii-unformatted>S0006291X96917131</xocs:pii-unformatted><xocs:eid>1-s2.0-S0006291X96917131</xocs:eid><xocs:doi>10.1006/bbrc.1996.1713</xocs:doi><xocs:cid>272308</xocs:cid><xocs:timestamp>2010-12-04T00:24:31.901978-05:00</xocs:timestamp><xocs:path>/272308/1-s2.0-S0006291X00X0160X/1-s2.0-S0006291X96917131/</xocs:path><xocs:cover-date-start>1996-11-21</xocs:cover-date-start><xocs:attachments><xocs:web-pdf><xocs:attachment-eid>1-s2.0-S0006291X96917131-main.pdf</xocs:attachment-eid><xocs:filename>main.pdf</xocs:filename><xocs:extension>pdf</xocs:extension><xocs:pdf-optimized>false</xocs:pdf-optimized><xocs:filesize>794890</xocs:filesize><xocs:web-pdf-purpose>MAIN</xocs:web-pdf-purpose><xocs:web-pdf-page-count>7</xocs:web-pdf-page-count><xocs:web-pdf-images><xocs:web-pdf-image><xocs:attachment-eid>1-s2.0-S0006291X96917131-main_1.png</xocs:attachment-eid><xocs:filename>main_1.png</xocs:filename><xocs:extension>png</xocs:extension><xocs:filesize>87900</xocs:filesize><xocs:pixel-height>849</xocs:pixel-height><xocs:pixel-width>656</xocs:pixel-width><xocs:attachment-type>IMAGE-WEB-PDF</xocs:attachment-type><xocs:pdf-page-num>1</xocs:pdf-page-num></xocs:web-pdf-image></xocs:web-pdf-images></xocs:web-pdf></xocs:attachments></xocs:attachment-metadata-doc><xocs:refkeys><xocs:refkey3>BRODIEX1996X655</xocs:refkey3><xocs:refkey4lp>BRODIEX1996X655X661</xocs:refkey4lp><xocs:refkey4ai>BRODIEX1996X655XA</xocs:refkey4ai><xocs:refkey5>BRODIEX1996X655X661XA</xocs:refkey5></xocs:refkeys><xocs:open-access><xocs:oa-article-status is-open-access=\"0\" is-open-archive=\"0\"/></xocs:open-access></xocs:meta><xocs:serial-item><converted-article version=\"4.5.2\" docsubtype=\"fla\" xml:lang=\"en\"><item-info><jid>YBBRC</jid><aid>91713</aid><ce:pii>S0006-291X(96)91713-1</ce:pii><ce:doi>10.1006/bbrc.1996.1713</ce:doi><ce:copyright type=\"full-transfer\" year=\"1996\">Academic Press</ce:copyright></item-info><head><ce:dochead><ce:textfn>Regular Article</ce:textfn></ce:dochead><ce:title>Inhibitors of Preadipocyte Differentiation Induce COUP-TF Binding to a PPAR/RXR Binding Sequence</ce:title><ce:author-group><ce:author><ce:given-name>A.E.</ce:given-name><ce:surname>Brodie</ce:surname><ce:cross-ref refid=\"FN1\">1</ce:cross-ref></ce:author><ce:author><ce:given-name>V.A.</ce:given-name><ce:surname>Manning</ce:surname></ce:author><ce:author><ce:given-name>C.Y.</ce:given-name><ce:surname>Hu</ce:surname></ce:author><ce:affiliation><ce:textfn>Department of Animal Sciences, Oregon State University, Corvallis, Oregon, 97331-6702</ce:textfn></ce:affiliation><ce:footnote id=\"FN1\"><ce:label>1</ce:label><ce:note-para>To whom correspondence should be addressed. Fax: (541) 737-4174.</ce:note-para></ce:footnote></ce:author-group><ce:date-received day=\"3\" month=\"10\" year=\"1996\"/><ce:abstract class=\"author\"><ce:section-title>Abstract</ce:section-title><ce:abstract-sec><ce:simple-para view=\"all\">Inhibition of preadipocyte differentiation by 2,3,7,8 tetrachlorodibenzo-p-dioxin (TCDD) or retinoic acid (RA) identified another transcription factor which appears to be important for preadipocyte differentiation. Within 15 min of treating 3T3-L1 cells with TCDD, the aryl hydrocarbon receptor (AhR) is present within the cell nucleus, and increased binding of COUP-TF to an oligomer of the PPARγ2/RXR binding sequence (ARE7) occurs. Following 2 days of RA treatment, increased binding of COUP-TF to the ARE7 oligomer also occurs. In untreated preadipocytes, COUP-TF mRNA increased at confluence and then decreased after induction. TCDD treatment did not alter COUP-TF mRNA changes. Dephosphorylating the nuclear extracts from TCDD and RA-treated cells eliminated binding of COUP-TF to ARE7. This is the first indication that COUP-TF may play a role in preadipocyte differentiation and that COUP-TF binding to DNA is correlated with TCDD and RA-induced phosphorylation.</ce:simple-para></ce:abstract-sec></ce:abstract></head></converted-article></xocs:serial-item></xocs:doc>"),"Extract the srctitle");
		} catch (XSLTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	@Test
	public void testTransform2() {
		
		try {
			HashMap<String,Object> featureMap = new HashMap<String,Object>();
			featureMap.put(FeatureKeys.ENTITY_RESOLVER_CLASS, "com.elsevier.spark_xml_utils.common.IgnoreDoctype");
			XSLTProcessor proc = XSLTProcessor.getInstance(stylesheet2,featureMap);
			assertEquals("{ 'title':'Harry Potter' }",proc.transform("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE books SYSTEM \"sample.dtd\"><books><book><title lang=\"en\">Harry Potter</title><author>J K. Rowling</author><year>2005</year><price>29.99</price></book></books>"),"Extract the title");
		} catch (XSLTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void testTransform3() {
		
		try {

			XSLTProcessor proc = XSLTProcessor.getInstance(stylesheet3);
			HashMap<String,String> stylesheetParams = new HashMap<String,String>();
			stylesheetParams.put("givenName", "<a>Darin</a>");
			stylesheetParams.put("surName", "<b>McBeath</b>");
			stylesheetParams.put("age", "<age>30</age>");
			assertEquals("<name><given-name>Darin</given-name><surname>McBeath</surname><age>30</age></name>",proc.transform("<stuff/>",stylesheetParams),"Use stylesheet params");
		} catch (XSLTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testTransformStream1() {
		
		try {
			HashMap<String,Object> featureMap = new HashMap<String,Object>();
			featureMap.put(FeatureKeys.ENTITY_RESOLVER_CLASS, "com.elsevier.spark_xml_utils.common.IgnoreDoctype");
			XSLTProcessor proc = XSLTProcessor.getInstance(stylesheet2,featureMap);
			String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE books SYSTEM \"sample.dtd\"><books><book><title lang=\"en\">Harry Potter</title><author>J K. Rowling</author><year>2005</year><price>29.99</price></book></books>";
			InputStream is = null;
			try {
				is = IOUtils.toInputStream(str, StandardCharsets.UTF_8.name());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			assertEquals("{ 'title':'Harry Potter' }",proc.transform(is),"Extract the title");
		} catch (XSLTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
