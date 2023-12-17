#! /bin/bash

LANGUAGE=()

if [[ -f Makefile ]]; then
	LANGUAGE+=("c")
fi
if [[ -f app/pom.xml ]]; then
	LANGUAGE+=("java")
fi
if [[ -f package.json ]]; then
	LANGUAGE+=("javascript")
fi
if [[ -f requirements.txt ]]; then
	LANGUAGE+=("python")
fi
if find app -type f | grep -q 'app/main.bf'; then
	LANGUAGE+=("befunge")
fi

if [[ ${#LANGUAGE[@]} == 0 ]]; then
	echo "Invalid project: no language matched."
	exit 1
fi
if [[ ${#LANGUAGE[@]} != 1 ]]; then
	echo "Invalid project: multiple languages matched (${LANGUAGE[@]})."
	exit 1
fi
echo "${LANGUAGE[@]} matched"

image_name="$2/whanos-$1-${LANGUAGE[0]}"

if [[ -f Dockerfile ]]; then
	docker build . -t "$image_name"
else
	image_name="$image_name-standalone"
	docker build . -f "/var/lib/jenkins/images/${LANGUAGE[0]}/Dockerfile.standalone" -t "$image_name"
fi

if ! docker push "$image_name"; then
	exit 1
fi
