# Copyright (C) 2001 - 2005 ScalAgent Distributed Technologies
# 
if test $# -ne 1
then
    echo usage $0 "<name>" >&2
    exit 1
fi
echo "hello $1 !"
exit 0
