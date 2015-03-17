#!/bin/bash
function check() {
if [ "$debug" == "off" ]
then
  return
fi
echo "***" $1:
echo "--- instancedirs:"
solrctl $zk instancedir --list
echo "--- collections:"
solrctl $zk collection --list
echo "-- directories:"
ls -l | grep "dir$" 
}

function copyfile {
  if [ -f $1.$collection.$2 ]
  then
    echo Copying $1.$2 into conf directory ...
    cp $1.$collection.$2 $dir/conf/$1.$2
  fi
}

function copyfiles {
  copyfile solrconfig xml
  copyfile schema xml
  copyfile synonyms txt
  copyfile skills txt
}

echo "*** BEGIN $0 ***"

function help {
  echo "Usage: $0 collection [--debug] [--rebuild|--update|--deletedocs]"
  exit
}

if [ $# -lt 1 ]
then
  help
fi

collection=$1
echo collection: $collection
dir=$1"dir"
echo dir: $dir
cfg=$1"cfg"
echo cfg: $cfg

if [[ ${collection:0:1} == "-" ]]
then
  help
fi

zookeeper=mfhadoopt03:2181/solr
echo zookeeper: $zookeeper
zk=`echo "--zk" $zookeeper`
echo zk: $zk

debug=off
deletedocs=off
rebuild=off
update=off
while [ $# -gt 1 ]
do
  case "$2" in
    --debug) debug=on;;
    --deletedocs) deletedocs=on;;
    --rebuild) rebuild=on;;
    --update) update=on;;
  esac
  shift
done
echo debug=$debug
echo rebuild=$rebuild
echo update=$update
echo deletedocs=$deletedocs

check "Starting with"

if [ "$rebuild" == "on" ]
then
  echo Rebuilding the configuration and collection from scratch ...
  for c in `solrctl $zk collection --list | grep $collection | awk '{print $1}'` ; do solrctl $zk collection --delete $c; done;

  for i in `solrctl $zk instancedir --list | grep $cfg` ; do solrctl $zk instancedir --delete $i; done;

  rm -r -f ./$dir

  check "After deletion"

  solrctl $zk instancedir --generate ./$dir

  copyfiles

  solrctl $zk instancedir --create $cfg ./$dir

  #solrctl $zk instancedir --update $cfg ./$dir

  #solrctl $zk collection --delete $collection

  solrctl $zk collection --create $collection -s 1 -c $cfg

fi

function error {
  echo $*
  exit
}

function checkexists {
  count=`solrctl $zk instancedir --list | grep $cfg | wc -l`
  if [ $count -eq 0 ]
  then
    error Configuration $cfg does not exist! 
  fi
  count=`solrctl $zk collection --list | grep $collection | wc -l`
  if [ $count -eq 0 ]
  then
    error Collection $collection does not exist
  fi
}

if [ "$update" == "on" ]
then
  echo Updating configuration ...
  checkexists

  copyfiles
  
  echo "solrctl $zk instancedir --update $cfg ./$dir" 
  solrctl $zk instancedir --update $cfg ./$dir

  echo "solrctl $zk collection --reload $collection"
  solrctl $zk collection --reload $collection
fi

if [ "$deletedocs" == "on" ]
then
  echo Deleting all documents from collection ...
  checkexists
  
  solrctl $zk collection --deletedocs $collection
fi

check "Completed"

echo "*** END $0 ***"
