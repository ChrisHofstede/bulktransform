# bulktransform
Transforms XML files in bulk

usage: Command line syntax:
 -in,--input <arg>     input file name (wildcards allowed): -in in\*.xml
 -out,--output <arg>   output folder: -out out
 -xsl,--xslt <arg>     XSLT stylesheet input file name: -xsl html.xsl

 java -jar bulktransform-1.0.jar -in in\*.xml -xsl html.xsl -out out