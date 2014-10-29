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
package com.elsevier.spark.examples.xslt;

import org.apache.spark.api.java.function.Function;
import com.elsevier.xml.XSLTProcessor;

/**
 * This class will be executed by a 'worker' in the Spark cluster.  The focus of this class
 * is to transform a record using the provided bucket/stylesheet.
 * 
 * @author mcbeathd
 *
 */
public class XSLTTransformWorker  implements Function<String,String> {

	private String stylesheetBucket;
	private String stylesheetKey;

	
	/**
	 * Save the stylesheet bucket/key to be used in the transformation.
	 * 
	 * @param stylesheetBucket
	 * @param stylesheetKey
	 */
	public XSLTTransformWorker(String stylesheetBucket, String stylesheetKey) {
		this.stylesheetBucket = stylesheetBucket;
		this.stylesheetKey = stylesheetKey;
	}

	@Override
	public String call(String value) throws Exception {
		return XSLTProcessor.transform(stylesheetBucket, stylesheetKey, value);
	}

	
}
