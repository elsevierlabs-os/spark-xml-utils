# spark-xml-utils

This site offers some background information on how to utilize the capabilities provided by the spark-xml-utils library within an [Apache Spark](http://spark.apache.org) application.  Some  scala examples (leveraging xpath, xslt, and xquery) within the Apache Spark framework are provided.  I have modified the spark-xml-utils APIs in this new version.   The previous  version was really a work in progress whereas the newer version incorporates some of the experience I have gained with both Spark and the spark-xml-utils package.  My hope is that the new version will be  simpler to use as well as more performant.   As time permits, I plan to optimize the implementation as well as add some additional features.

Spark-xml-utils is not meant for processing one large single GBs xml record. However, if you have many xml records (we have millions)in the MBs (or less) then this should be a handy tool.

The examples included only scratch the surface for what is possible with spark-xml-utils.  I have used it to transform millions of xml documents to json and html, performed a simple batch search against millions of xml documents, and more.  When I have time, I will include some of these examples.

The javadoc is available for spark-xml-utils and could be helpful with understanding the class interactions.

## Motivation

The spark-xml-utils library was developed because there is a large amount of xml in our big datasets and I felt this data could be better served by providing some helpful xml utilities.  This includes the ability to filter documents  based on an xpath expression, return specific nodes for an xpath/xquery expression, or transform documents using a xslt stylesheet.  By providing some basic wrappers to [Saxon](http://www.saxonica.com), the spark-xml-utils library exposes some basic XPath, XQuery, and XSLT functionality that can readily be leveraged by any Spark application.  

## Examples

The  sequence file used in all of the examples are publicly available in s3://spark-xml-utils/xml.  In the sequence file, the key is a unique identifier for the record and the value is the XML (as a string).  This should allow you to try out the examples as well as experiment with your own expressions.

Once you are comfortable with the provided basic [XPath](https://github.com/elsevierlabs/spark-xml-utils/wiki/xpath), [XQuery](https://github.com/elsevierlabs/spark-xml-utils/wiki/xquery), and [XSLT](https://github.com/elsevierlabs/spark-xml-utils/wiki/xslt) examples, there are also some more [complex examples](https://github.com/elsevierlabs/spark-xml-utils/wiki/complexexamples) available to further showcase the power of spark-xml-utils.