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

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;

/**
 * Sample 'driver' class that provides example usage of the XSLTProcessor methods.
 * This class is considered the 'mainline' and is executed as the 'driver' in a
 * Spark cluster.  In the code below, a sample hadoop sequence file is loaded from
 * S3.  It is then transformed by the XSLTProcessor (using a specified stylesheet).
 * The final RDD is then persisted back to S3.  The workers in the Spark
 * cluster will execute the code in XSLTTransformWorker.
 * 
 * @author mcbeathd
 *
 */
public class XSLTDriver {

	// Literals for the AWS keys
	public static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
	public static final String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";
	
	/**
	 * Mainline
	 * @param args command line args
	 */
	public static void main(String[] args) {
		
		// Create and Initialize a SparkConf
		SparkConf conf = new SparkConf().setAppName("Example XSLT Application");
		
		// Comment out below to use the stand-alone cluster
		//conf.setMaster("local[2]");
		
		// Set environment variables for the executors
		conf.setExecutorEnv(AWS_ACCESS_KEY_ID,System.getenv(AWS_ACCESS_KEY_ID));
		conf.setExecutorEnv(AWS_SECRET_ACCESS_KEY,System.getenv(AWS_SECRET_ACCESS_KEY));

		// Create and Initialize a SparkContext
		JavaSparkContext sc = new JavaSparkContext(conf);
			
		// Load up a hadoop sequence file (key will be the pii, value is the xml)
		JavaPairRDD<Text,Text> xmlRDDReadable = sc.hadoopFile("s3n://els-ats/darin/sd-test-xml", SequenceFileInputFormat.class, Text.class, Text.class);
		JavaPairRDD<String, String> xmlKeyPairRDD = xmlRDDReadable.mapToPair(new ConvertFromWritableTypes()).cache();	

		System.out.println("Number of initial records is " + xmlKeyPairRDD.count());
		
		// Init the partitions. 
		xmlKeyPairRDD.foreachPartition(new XSLTInitWorker()); 
						
		// Transform the content.  S3 bucket is 'spark-stylesheets' and key is 'xmlMeta2json.xsl'
		JavaPairRDD<String, String> transformXmlKeyPairRDD = xmlKeyPairRDD.mapValues(new XSLTTransformWorker("spark-stylesheets","xmlMeta2json.xsl"));
		
		// Save the results back to S3 as a hadoop sequence file
		JavaPairRDD<Text, Text> transformRDDWritable = transformXmlKeyPairRDD.mapToPair(new ConvertToWritableTypes());
		transformRDDWritable.saveAsHadoopFile("s3n://els-ats/darin/xslt-results", Text.class, Text.class, SequenceFileOutputFormat.class);

	}
}

/**
 * Convert from writable hadoop types (text) to Strings
 *
 */
class ConvertFromWritableTypes implements PairFunction<Tuple2<Text, Text>, String, String> {
	
	public Tuple2<String, String> call(Tuple2<Text, Text> record) {				
		return new Tuple2(record._1.toString(), record._2.toString()); 
	}
	
}

/**
 * Convert to writable hadoop types (text) from Strings
 *
 */
class ConvertToWritableTypes implements PairFunction<Tuple2<String, String>, Text, Text> {
	
	public Tuple2<Text, Text> call(Tuple2<String, String> record) {	
		return new Tuple2(new Text(record._1), new Text(record._2)); 
		 
	}
	
}
