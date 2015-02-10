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
package com.elsevier.spark.examples.xpath;

import org.apache.spark.api.java.function.Function;
import com.elsevier.xml.XPathProcessor;

/**
 * This class will be executed by a 'worker' in the Spark cluster.  The focus of this class
 * is to evaluate a record against the provided xpath expression.
 * 
 * @author mcbeathd
 *
 */
public class XPathEvaluateWorker  implements Function<String,String> {

	private String xpathExpression;
	
	/**
	 * Save the xpathExpression to be used in the evaluation.
	 * 
	 * @param xpathExpression
	 */
	public XPathEvaluateWorker(String xpathExpression) {
		this.xpathExpression = xpathExpression;
	}


	@Override
	public String call(String value) {
		return XPathProcessor.evaluateString(value, xpathExpression);
	}

	
}
