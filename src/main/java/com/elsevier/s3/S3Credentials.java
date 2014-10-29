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
package com.elsevier.s3;

/**
 * Class with static methods to get the AWS keys. A system environment variable
 * will have precedence over a system property.
 * 
 * @author Darin McBeath
 * 
 */
public class S3Credentials {

	// Literals for the AWS keys
	private static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
	private static final String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";

	/**
	 * Get the AWS access key id
	 * 
	 * @return AWS access key id
	 */
	public static String getAwsAccessKeyId() {

		String awsAccessKeyId = null;
		awsAccessKeyId = System.getenv(AWS_ACCESS_KEY_ID);
		if (awsAccessKeyId == null) {
			awsAccessKeyId = System.getProperty(AWS_ACCESS_KEY_ID);
		}
		return awsAccessKeyId;

	}

	/**
	 * Get the AWS secret access key
	 * 
	 * @return AWS secret access key
	 */
	public static String getAwsSecretAccessKey() {

		String awsAccessSecretKey = null;
		awsAccessSecretKey = System.getenv(AWS_SECRET_ACCESS_KEY);
		if (awsAccessSecretKey == null) {
			awsAccessSecretKey = System.getProperty(AWS_SECRET_ACCESS_KEY);
		}
		return awsAccessSecretKey;

	}

}
