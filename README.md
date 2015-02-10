# spark-xml-utils

This site offers some background information on how to utilize the capabilities provided by the spark-xml-utils library within an [Apache Spark](http://spark.apache.org) application.  Some  java [examples](https://github.com/elsevierlabs/spark-xml-utils/wiki/examples) with using Apache Spark are provided.  The focus at this point has not been on performance but just showing how things would work. As time permits, we plan to optimize the implementation.

The javadoc is also available for spark-xml-utils and could be helpful with understanding the class interactions.



## Motivation

The spark-xml-utils library was developed because there is a large amount of XML in our big datasets and I felt this data could be better served by providing some helpful xml utilities.  This includes the ability to filter documents  based on an xpath expression, return specific nodes for an xpath/xquery expression, or transform documents using a xslt stylesheet.  By providing some basic wrappers to [Saxon](http://http://www.saxonica.com), the spark-xml-utils library exposes some basic [XPath](https://github.com/elsevierlabs/spark-xml-utils/wiki/xpath), [XSLT](https://github.com/elsevierlabs/spark-xml-utils/wiki/xslt), and [XQuery](https://github.com/elsevierlabs/spark-xml-utils/wiki/xquery) functionality that can readily be leveraged by any Spark application.  