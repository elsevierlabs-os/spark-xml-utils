# spark-xml-utils

This site offers some background information on how to utilize the capabilities provided by the spark-xml-utils library within an [Apache Spark](http://spark.apache.org) application.  Some  scala examples (leveraging XPath, XSLT, and XQuery) within the Apache Spark framework are provided.  I have modified the spark-xml-utils APIs in this new version.   The previous  version was really a work in progress whereas the newer version incorporates some of the experience I have gained with both Spark and the spark-xml-utils package.  My hope is that the new version will be  simpler to use as well as more performant.   As time permits, I plan to optimize the implementation as well as add some additional features.

Spark-xml-utils is not meant for processing one large single GBs XML record. However, if you have many XML records (we have millions) in the MBs (or less) then this should be a handy tool.

The javadoc is available for spark-xml-utils and could be helpful with understanding the class interactions.
## Motivation

The spark-xml-utils library was developed because there is a large amount of XML in our big datasets and I felt this data could be better served by providing some helpful XML utilities.  This includes the ability to filter documents  based on an XPath expression, return specific nodes for an XPath/XQuery expression, or transform documents using a XSLT stylesheet.  By providing some basic wrappers to [Saxon](http://www.saxonica.com), the spark-xml-utils library exposes some basic XPath, XQuery, and XSLT functionality that can readily be leveraged by any Spark application.  

## Examples

The basic examples included only scratch the surface for what is possible with spark-xml-utils and [XPath](https://github.com/elsevierlabs/spark-xml-utils/wiki/xpath), [XQuery](https://github.com/elsevierlabs/spark-xml-utils/wiki/XQuery), and [XSLT](https://github.com/elsevierlabs/spark-xml-utils/wiki/xslt).  I have used spark-xml-utils to transform millions of XML documents to json and html, performed a simple batch search against millions of XML documents, and more. Some more [complex examples](https://github.com/elsevierlabs/spark-xml-utils/wiki/complexexamples) are available to further showcase the power of spark-xml-utils.

The  sequence file used in all of the examples is publicly available in s3://spark-xml-utils/XML.  In the sequence file, the key is a unique identifier for the record and the value is the XML (as a string).  This should allow you to try out the examples as well as experiment with your own expressions.