#!/bin/sh

##############################################################################
#
# ACE Modules Release Build script
#
# This script releases the ACE Modules environment
#
# Usage:
#  ace-modules-release.sh buildreltag [-r|--RELEASEVER arg] [-s|--SNAPSHOTVER arg]
#                                 [-m|--mavenopts]
#                    
# Examples:
#  - Release ACE Modules and set versions automatically:
#           ace-modules-release.sh
#  - Release ACE Modules and set versions on the command line:
#           ace-modules-release.sh -r 1.3-BETA1 --SNAPSHOTVER 1.3-SNAPSHOT
#
##############################################################################

##################################
# No parameters passed fail build
if [ -z $1 ]; then
    echo "*** Error! No parameters passed"
    exit 1
fi

######################
# Set basic variables
#
####################################
# Set default release command
#
RELCMD="mvn release:prepare release:perform --batch-mode -e"

###################
# Parse parameters
#
args=`getopt -o r:s: --long RELEASEVER:,SNAPSHOTVER: -- "$@"` 
eval set -- "$args"
while true;
do
    case "$1" in
    -r|--RELEASEVER) 
        case $2 in
        "") echo "Did not set Release version Maven will set it!"; exit 1 ;;
        *) RELEASEVER=$2 ; shift 2 ;;
        esac ;;
    -s|--SNAPSHOTVER)
        case $2 in
        "") echo "Did not set Snapshot version Maven will set it!"; shift 2 ;;
        *) SNAPSHOTVER=$2 ; shift 2 ;;
        esac ;;
    -m|--mavenopts)
        MAVEN="1"
        export MAVEN_OPTS="-Xmx4g -XX:-UseGCOverheadLimit -XX:+UseConcMarkSweepGC -XX:PermSize=256m -XX:MaxPermSize=256m"
        shift ;;
    --) shift ;;
    "") break ;;
    *) 
    echo "Error! Can't parse argument: $1"
    echo "*** usage: ace-modules-release.sh [-r|--RELEASEVER arg] [-s|--SNAPSHOTVER arg]  [-m|--mavenopts]"
    exit 1 ;;
    esac
done

######################
# Set build variables
#
SNAPSHOTDIR="snapshot"
RELEASEDIR="release"
DATABASEDIR="sct-au-data"
DISTRIBUTIONDIR="sct-au-distribution"
PROMOTIONDIR="sct-au-promotion"
RELEASEFILESDIR="sct-au-release-files"
RELEASEDBDIR="sct-au-release-database"
VIEWERDIR="sct-au-viewer"
REPOURL="https://csfe.aceworkspace.net/svn/repos/sct-au-master"
SVNUSER="continuum"
SVNPASS="nehta17"
BUILDDIR=`pwd`
RELEASETAG="$REPOURL/tags/releases/sct-au-master/sct-au-master-$RELEASEVER"

###############################
# Print build and release info
#
echo "*************************************"
echo "*    Specified Build parameters     *"
echo "*************************************"
if [ -n $RELEASEVER ]; then
    echo "* Release Version : $RELEASEVER"
fi
if [ -n $SNAPSHOTVER ]; then
    echo "* Next Snapshot   : $SNAPSHOTVER"
fi
if [ "$MAVEN" = "1" ]; then
    echo "* MAVEN_OPTS      : $MAVEN_OPTS" 
fi
echo "*************************************"
echo ""



#################################################################
# Tag snapshot build environment and set new development version
#
if [ -z "$RELEASEVER" ]; then
    if [ -z "$SNAPSHOTVER" ]; then
        # New develpoment version and release version weren't passed on command line let Maven set it 
        # Do nothing
        echo "*** No Versions have been set! Maven will choose automatically!"
        else 
        # Set new develpoment version to $SNAPSHOTVER 
        RELCMD="$RELCMD -DdevelopmentVersion=$SNAPSHOTVER"
    fi
 else
    if [ -z "$SNAPSHOTVER" ]; then
        # New develpoment version wasn't passed on command line let Maven set it. Set release version to $RELEASEVER
        RELCMD="$RELCMD -DreleaseVersion=$RELEASEVER"
        else 
        # Set new develpoment version to $SNAPSHOTVER. Set release version to $RELEASEVER
        RELCMD="$RELCMD -DdevelopmentVersion=$SNAPSHOTVER -DreleaseVersion=$RELEASEVER"
    fi
fi
echo "*** Execute: $RELCMD"
$RELCMD
if [ "$?" -ne "0" ]; then
    echo "*** Maven Build Failed!"
    exit 1
fi

##########################################
# The release or the build was successful 
echo "****************"
echo "BUILD SUCCESSFUL"
echo "****************"

exit 0
