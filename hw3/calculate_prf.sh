DATA_DIR="data"
QUERIES_FILE="queries.tsv"
RESULTS_DIR=${DATA_DIR}"/results"
mkdir -p $RESULTS_DIR
rm -f ${RESULTS_DIR}/prf*.tsv
i=0
while read q ; do
i=$((i + 1));
prfout=prf-"$i".tsv;
Q=`echo $q| sed "s/ /%20/g"`;
curl 'http://localhost:25816/prf?query='${Q}'&ranker=comprehensive&numdocs=10&numterms=5'  >> ${RESULTS_DIR}"/"$prfout;
echo $q:$prfout >> ${RESULTS_DIR}"/prf.tsv"
done < ${DATA_DIR}"/"${QUERIES_FILE}
