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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Class with static methods to put/get/delete object in an S3 bucket as well as
 * static methods for getting the length of the object and last modified date.
 * 
 * @author Darin McBeath
 * 
 */
public class SimpleStorageService {

	// S3 client
	private static volatile AmazonS3 s3Client = null;
	
	// Logger
	private static Log log = LogFactory.getLog(SimpleStorageService.class);

	/**
	 * Initialize the S3 client
	 */
	public static void init() {

		if (s3Client == null && S3Credentials.getAwsAccessKeyId() != null
				&& S3Credentials.getAwsSecretAccessKey() != null) {
			synchronized (SimpleStorageService.class) {
				if (s3Client == null) {
					AWSCredentials awsCredentials = new BasicAWSCredentials(
							S3Credentials.getAwsAccessKeyId(),
							S3Credentials.getAwsSecretAccessKey());
					s3Client = new AmazonS3Client(awsCredentials);
				}
			}
		}
		if (s3Client == null) {
			log.fatal("Unable to init the S3 client");
		}

	}

	/**
	 * Clear the S3 client.
	 */
	public static void clear() {

		if (s3Client != null) {
			synchronized (SimpleStorageService.class) {
				if (s3Client != null) {
					s3Client = null;
				}
			}
		}

	}

	/**
	 * Put the object (with the key value) into the specified S3 bucket. Will
	 * also set the content type, content length, encoding, and encryption.
	 * 
	 * @param bucketName
	 *            S3 bucket name
	 * @param key
	 *            S3 key value for the content
	 * @param content
	 *            content to put into S3
	 * @param contentType
	 *            Content type (such as text/xml)
	 * @throws IOException
	 */
	public static void putObject(String bucketName, String key, String content,
			String contentType) throws IOException {

		try {

			if (s3Client == null) {
				throw new AmazonClientException("S3Client not initialized.");
			}
			
			// Request server-side encryption.
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata
					.setServerSideEncryption(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
			// Method not available in the 1.4.* AWS SDK. Currently can't use
			// recent versions of AWS SDK due to conflicts with the HttpClient
			// library used by Spark.
			// objectMetadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION);
			objectMetadata.setContentType(contentType);
			objectMetadata.setContentLength(content.getBytes(CharEncoding.UTF_8).length);
			objectMetadata.setContentEncoding(CharEncoding.UTF_8);

			PutObjectRequest putRequest = new PutObjectRequest(bucketName, key,
					new ByteArrayInputStream(content
							.getBytes(CharEncoding.UTF_8)),
					objectMetadata);
			
			s3Client.putObject(putRequest);

		} catch (AmazonClientException e) {
			
			log.error("Problems writing to S3. BUCKET:" + bucketName + " KEY:" + key + " " + e.getMessage(),e);
			throw new IOException("*** Problems writing " + key + " to S3.");

		} catch (UnsupportedEncodingException e) {
			
			log.error("Problems writing to S3. BUCKET:" + bucketName + " KEY:" + key + " " + e.getMessage(),e);
			throw new IOException("*** Problems writing " + key + " to S3." + e.getMessage());

		}

	}

	/**
	 * Get the object (with key value) from the specified S3 bucket.
	 * 
	 * @param bucketName
	 *            S3 bucket name
	 * @param key
	 *            S3 key value
	 * @return S3 object contents as String
	 * @throws IOException
	 */
	public static String getObject(String bucketName, String key)
			throws IOException {

		try {

			if (s3Client == null) {
				throw new AmazonClientException("S3Client not initialized.");
			}
			
			S3Object object = s3Client.getObject(new GetObjectRequest(
					bucketName, key));
			return IOUtils.toString(object.getObjectContent());

		} catch (AmazonClientException e) {
			
			log.error("Problems retrieving from S3. BUCKET:" + bucketName + " KEY:" + key + " " + e.getMessage(),e);
			throw new IOException("*** Problems getting " + key + " from S3." + e.getMessage());

		}

	}

	/**
	 * Delete the object (with key value) from the specified S3 bucket.
	 * 
	 * @param bucketName
	 *            S3 bucket name
	 * @param key
	 *            S3 key value
	 * 
	 */
	public static void deleteObject(String bucketName, String key)
			throws IOException {

		try {

			if (s3Client == null) {
				throw new AmazonClientException("S3Client not initialized.");
			}
			
			s3Client.deleteObject(bucketName, key);

		} catch (AmazonClientException e) {
			
			log.error("Problems deleting from S3. BUCKET:" + bucketName + " KEY:" + key + " " + e.getMessage(),e);
			throw new IOException("*** Problems deleting " + key + " from S3."+ e.getMessage());

		}

	}

	/**
	 * Get the object length (with key value) from the specified S3 bucket.
	 * 
	 * @param bucketName
	 *            S3 bucket name
	 * @param key
	 *            S3 key value
	 * @return S3 object length
	 * @throws IOException
	 */
	public static long getObjectLength(String bucketName, String key)
			throws IOException {

		try {

			if (s3Client == null) {
				throw new AmazonClientException("S3Client not initialized.");
			}
			
			ObjectMetadata metadata = s3Client.getObject(
					new GetObjectRequest(bucketName, key)).getObjectMetadata();
			return metadata.getContentLength();

		} catch (AmazonClientException e) {
			
			log.error("Problems getting object length from S3. BUCKET:" + bucketName + " KEY:" + key + " " + e.getMessage(),e);
			throw new IOException("*** Problems getting content length for "
					+ key + " from S3." + e.getMessage());

		}

	}

	/**
	 * Get the object last modified (with key value) from the specified S3
	 * bucket.
	 * 
	 * @param bucketName
	 *            S3 bucket name
	 * @param key
	 *            S3 key value
	 * @return S3 last modified
	 * @throws IOException
	 */
	public static Date getObjectLastModifed(String bucketName, String key)
			throws IOException {

		try {

			if (s3Client == null) {
				throw new AmazonClientException("S3Client not initialized.");
			}
			
			ObjectMetadata metadata = s3Client.getObject(
					new GetObjectRequest(bucketName, key)).getObjectMetadata();
			return metadata.getLastModified();

		} catch (AmazonClientException e) {

			log.error("Problems getting last modified date from S3. BUCKET:" + bucketName + " KEY:" + key + " " + e.getMessage(),e);
			throw new IOException(
					"*** Problems getting last modified date for " + key
							+ " from S3." + e.getMessage());

		}

	}

}
