#!/usr/bin/env bash
START_DIR=$(cd `dirname $0` && pwd )
echo "$START_DIR"
#export BW_SESSION=$(bw unlock --raw)
function fail(){
  echo "could not build"
  exit
}

echo "this script will prompt you for the GPG passphrase"
export GPG_TTY=$(tty)

## RELEASE
echo "setting release version..."
mvn build-helper:parse-version versions:set \
  -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.incrementalVersion}
RELEASE_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

echo "the release version is $RELEASE_VERSION "
echo "deploying..."
mvn versions:commit                           # accept the release version

KEY_LOCAL_PATH=secret-backup.gpg
KEY_INSTALL_PATH=$HOME/.gnupg/private-keys-v1.d/${KEY_LOCAL_PATH}

echo $GPG_KEY | base64 -d  > $KEY_LOCAL_PATH
mv ${KEY_LOCAL_PATH} "${KEY_INSTALL_PATH}"


mvn -X -DskipTests=true -P publish clean deploy   || fail

git commit -am "releasing ${RELEASE_VERSION}" # release the main version
TAG_NAME=v${RELEASE_VERSION}
git tag -a $TAG_NAME -m "release tag ${TAG_NAME}"
git push origin "$TAG_NAME"

## BACK TO THE LAB AGAIN
mvn build-helper:parse-version versions:set \
  -DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion}-SNAPSHOT
echo "the next snapshot version is $(mvn help:evaluate -Dexpression=project.version -q -DforceStdout) "
mvn versions:commit
SNAPSHOT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
git commit -am "moving to $SNAPSHOT_VERSION"
git push

rm "${KEY_INSTALL_PATH}"