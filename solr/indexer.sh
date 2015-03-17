collection=pdfcollection

echo Cleaning up Solr collections ...
for c in `solrctl --zk localhost:2181/solr collection --list | awk {'print $1'}`; do echo $c && solrctl --zk localhost:2181/solr collection --delete $c; done
echo Cleaning up Solr instancedirs ...
for i in `solrctl --zk localhost:2181/solr instancedir --list`; do echo $i && solrctl --zk localhost:2181/solr instancedir --delete $i; done

echo Generating new config directory ...
echo "- delete old config dir ..."
rm -r -f ./pdfconfigdir
echo "- generate new config dir ..."
solrctl --zk localhost:2181/solr instancedir --generate ./pdfconfigdir
echo "- update schema.xml and solarconfig.xml and skills.txt in conf dir ..."
cp schema.xml ./pdfconfigdir/conf
cp solrconfig.xml ./pdfconfigdir/conf
cp skills.txt ./pdfconfigdir/conf
cp synonyms.txt ./pdfconfigdir/conf

echo Creating new instancedir "pdfconfig" ...
solrctl --zk localhost:2181/solr instancedir --create pdfconfig ./pdfconfigdir
echo Creating new collection "$collection" ...
solrctl --zk localhost:2181/solr collection --create $collection -s 1 -c pdfconfig

echo Cleaning up HDFS input folder ...
hdfs dfs -rm -r -f -skipTrash /user/cloudera/solr/data/input/* >tmp.txt
count=`wc -l tmp.txt | awk {'print $1'}`
if [ $count -le 10 ]
then
  while read line
  do
     echo $line
  done <tmp.txt
else
  echo $count files deleted from HDFS folder /user/cloudera/solr/data/input
fi

hdfs dfs -ls /user/cloudera/solr/data/input

echo Copying local files to HDFS input folder ...
hdfs dfs -copyFromLocal /home/cloudera/solr/data/input/* /user/cloudera/solr/data/input

hdfs dfs -ls /user/cloudera/solr/data/input >tmp.txt
count=`wc -l tmp.txt | awk {'print $1'}`
if [ $count -le 10 ]
then
  while read line
  do
     echo $line
  done <tmp.txt
else
  echo `head -1 tmp.txt` in HDFS folder /user/cloudera/solr/data/input
fi

echo Solr instancedirs:
solrctl --zk localhost:2181/solr instancedir --list
echo Solr collections:
solrctl --zk localhost:2181/solr collection --list

echo Updating collection $collection with new schema.xml and solrconfig.xml and skills.txt ...
cp schema.xml ./pdfconfigdir/conf
cp solrconfig.xml ./pdfconfigdir/conf
cp skills.txt ./pdfconfigdir/conf
cp synonyms.txt ./pdfconfigdir/conf
solrctl --zk localhost:2181/solr instancedir --update pdfconfig ./pdfconfigdir

solrctl --zk localhost:2181/solr collection --deletedocs $collection

hadoop jar /opt/cloudera/parcels/CDH-5.0.0-1.cdh5.0.0.p0.47/lib/solr/contrib/mr/search-mr-1.0.0-cdh5.0.0-job.jar \
org.apache.solr.hadoop.MapReduceIndexerTool \
-D'mapred.child.java.opts=-Xmx2G' \
--log4j `pwd`/log4j.properties \
--morphline-file `pwd`/loadPDFintoSolr.conf \
--output-dir hdfs://localhost:8020/user/cloudera/solr/data/output \
--verbose --go-live --zk-host localhost:2181/solr \
--collection $collection \
hdfs://localhost:8020/user/cloudera/solr/data/input

exit

