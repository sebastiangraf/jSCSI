#! /bin/sh

BLOCKSIZE=512
SAMPLE=sample.img
READBACK=readback.img

DEVICE=${1}
COUNT=${2}
IO_SIZE=${3}

function usage() {
   echo "Usage: sg_udc DEVICE COUNT [IO_SIZE}"
   exit 1
}


if [ -f ${SAMPLE} ]; then
   rm ${SAMPLE}
fi

if [ -f ${READBACK} ]; then
   rm ${READBACK}
fi

if [ "${DEVICE}" == "" ]; then
   usage
fi

if [ "${COUNT}" == "" ]; then
   usage
fi

if [ "${IO_SIZE}" == "" ]; then
   BPT=""
else
   BPT="bpt=${IO_SIZE}"
fi

echo -n "Generating random sample data of ${COUNT} blocks..."
dd if=/dev/urandom of=${SAMPLE} bs=${BLOCKSIZE} count=${COUNT} 2>/dev/null
echo " done"

echo "Copying ${COUNT} blocks to ${DEVICE} position 0"
sg_dd if=${SAMPLE} of=${DEVICE} bs=512 count=${COUNT} ${BPT}


echo "Copying ${COUNT} blocks back from ${DEVICE} position 0"
sg_dd if=${DEVICE} of=${READBACK} bs=512 count=${COUNT} ${BPT}



echo
echo "Original sample data"
hexdump -n 128 ${SAMPLE}
echo
echo "Data read back from ${DEVICE}"
hexdump -n 128 ${READBACK}

diff ${SAMPLE} ${READBACK}
STATUS=$?

if [ ${STATUS} -ne 0 ]; then
   exit ${STATUS}
fi

echo "Copying ${COUNT} blocks to ${DEVICE} position ${COUNT}"
sg_dd if=${SAMPLE} of=${DEVICE} bs=512 count=${COUNT} seek=${COUNT} ${BPT}

echo "Copying ${COUNT} blcoks from ${DEVICE} position ${COUNT}"
sg_dd if=${DEVICE} of=${READBACK} bs=512 count=${COUNT} skip=${COUNT} ${BPT}


echo
echo "Original sample data"
hexdump -n 128 ${SAMPLE}
echo
echo "Data read back from ${DEVICE}"
hexdump -n 128 ${READBACK}

diff ${SAMPLE} ${READBACK}
STATUS=$?

if [ ${STATUS} -ne 0 ]; then
   exit ${STATUS}
fi

# EOF
