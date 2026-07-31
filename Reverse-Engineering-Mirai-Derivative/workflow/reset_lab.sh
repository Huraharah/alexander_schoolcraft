#!/bin/bash

NAME="$1"

if [[ -z "$NAME" ]]; then
	echo "Usage: $0 <4-char-process-name>"
	exit 1
fi

ps -eo pid=,args= | awk -v name="$NAME" '$2==name {print $1}' | xargs -r kill -9

cp ./gayfemboy.backup ./gayfemboy
