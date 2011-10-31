#! /bin/sh
#
# Copyright (c) 2011, University of Konstanz, Distributed Systems Group
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#     * Neither the name of the University of Konstanz nor the
#       names of its contributors may be used to endorse or promote products
#       derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#


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
