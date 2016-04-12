set -e

VERSION=$(cat pom.xml | grep "^    <version>.*</version>$" | awk -F'[><]' '{print $3}')
PROJECT_NAME=$(basename $(pwd))
PACKAGE_CATALOG=$PROJECT_NAME-$VERSION
JAR_NAME="$PACKAGE_CATALOG.jar"

# GEARPUMP BROKER
# download gearpump binaries
GEARPUMP_PACK_FULL_VER=$(cat src/cloudfoundry/manifest.yml | grep GEARPUMP_PACK_VERSION | cut -d ' ' -f 6- | sed 's/["]//g')
GEARPUMP_PACK_SHORT_VER=$(echo $GEARPUMP_PACK_FULL_VER | cut -d '-' -f 2-)
GEARPUMP_RESOURCES_PATH=src/main/resources/gearpump
wget "https://github.com/gearpump/gearpump/releases/download/$GEARPUMP_PACK_SHORT_VER/gearpump-$GEARPUMP_PACK_FULL_VER.zip" -P $GEARPUMP_RESOURCES_PATH

# build project
mvn clean install -Dmaven.test.skip=true

# create tmp catalog
mkdir $PACKAGE_CATALOG

# files to package
cp src/cloudfoundry/manifest.yml $PACKAGE_CATALOG
cp --parents target/$JAR_NAME $PACKAGE_CATALOG

# prepare build manifest
echo "commit_sha=$(git rev-parse HEAD)" > $PACKAGE_CATALOG/build_info.ini

# create zip package
cd $PACKAGE_CATALOG
zip -r ../$PROJECT_NAME-$VERSION.zip *
cd ..

# remove tmp catalog
rm -r $PACKAGE_CATALOG

echo "Zip package for $PROJECT_NAME project in version $VERSION has been prepared."

############################ GEARPUMP DASHBOARD #############################
TMP_CATALOG=/tmp/gearpump-binaries
TMP_ARCHIVE_CATALOG=tmp
mkdir -p $TMP_CATALOG
unzip $GEARPUMP_RESOURCES_PATH/gearpump-$GEARPUMP_PACK_FULL_VER.zip -d $TMP_CATALOG
cd scripts
./prepare.sh $TMP_CATALOG/gearpump-$GEARPUMP_PACK_FULL_VER $TMP_CATALOG
cd ..

# prepare files to archive
mkdir -p tmp/target
cp $TMP_CATALOG/target/gearpump-dashboard.zip $TMP_ARCHIVE_CATALOG/target/
cp $TMP_CATALOG/manifest.yml $TMP_ARCHIVE_CATALOG/
echo "commit_sha=$(git rev-parse HEAD)" > build_info.ini

# create artifact
cd $TMP_ARCHIVE_CATALOG
zip -r ../gearpump-dashboard-${VERSION}.zip *
cd ..

# clean temporary data
rm -r $TMP_CATALOG
rm -r $TMP_ARCHIVE_CATALOG

echo "Zip package for gearpump-dashboard project in version $VERSION has been prepared."

