# spark-xml-utils

This site offers some background information on how to utilize the capabilities provided by the spark-xml-utils library within an [Apache Spark](http://spark.apache.org) application.  In addition, some [helpful tips](https://github.com/elsevierlabs/spark-xml-utils/wiki/helpful_tips), [lessons learned](https://github.com/elsevierlabs/spark-xml-utils/wiki/lessons_learned), and java [examples](https://github.com/elsevierlabs/spark-xml-utils/wiki/examples) with using Apache Spark are provided.

The javadoc is also available for spark-xml-utils and could be helpful with understanding the class interactions.



## Motivation

The spark-xml-utils library was developed because there is a large amount of XML in our big datasets and I felt this data could be better served by providing some helpful xml utilities.  This includes the ability to filter documents  based on an xpath/xquery expression, return specific nodes for an xpath/xquery expression, or transform documents using a xslt stylesheet.  By providing some basic wrappers to [Saxon](http://http://www.saxonica.com), the spark-xml-utils library exposes some basic [XPath](https://github.com/elsevierlabs/spark-xml-utils/wiki/xpath), [XSLT](https://github.com/elsevierlabs/spark-xml-utils/wiki/xslt), and [XQuery](https://github.com/elsevierlabs/spark-xml-utils/wiki/xquery) functionality that can readily be leveraged by any Spark application.  