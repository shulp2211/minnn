#!/bin/bash
set -e
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
cd "$SCRIPT_DIR/../milib"
mvn clean install -DskipTests=true
cd ..
mvn clean package shade:shade -Dmaven.test.skip=true

# readlink -f not on mac
function readlinkUniversal() {
    targetFile=$1

    cd `dirname $targetFile`
    targetFile=`basename $targetFile`

    # iterate down a (possible) chain of symlinks
    while [ -L "$targetFile" ]
    do
        targetFile=`readlink $targetFile`
        cd `dirname $targetFile`
        targetFile=`basename $targetFile`
    done

    # compute the canonicalized name by finding the physical path 
    # for the directory we're in and appending the target file.
    phys_dir=`pwd -P`
    result=$phys_dir/$targetFile
    echo $result
}

MINNN="$( readlinkUniversal "$( find "$SCRIPT_DIR/../target" -name '*distribution.jar' | head -1 )" )"
java -jar "$MINNN" docs --output "$SCRIPT_DIR/source/reference.rst"
# requires soffice, not on mac by default:
#soffice --headless --convert-to svg "$SCRIPT_DIR/source/usage-chart.odg" --outdir "$SCRIPT_DIR/source/_static"
