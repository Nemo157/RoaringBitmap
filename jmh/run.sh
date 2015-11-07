BASEDIR=$(dirname $0)



echo "Building RoaringBitmap jar"
rm -f $BASEDIR/../target/RoaringBitmap*.jar
mvn -f $BASEDIR/../pom.xml clean install -DskipTests -Dgpg.skip=true

echo "Building Real Roaring Dataset jar"
rm -f $BASEDIR/../real-roaring-dataset/target/real-roaring-dataset*.jar
mvn -f $BASEDIR/../real-roaring-dataset/pom.xml clean install

echo "Building benchmarks jar"
rm -f  $BASEDIR/target/benchmarks.jar
mvn -f $BASEDIR/pom.xml clean install -Dtest=*$1* -DfailIfNoTests=false -Djna.library.path="$BASEDIR/../../bindings/target/release"

echo "Running benchmarks"
java -Djna.library.path="$BASEDIR/../../bindings/target/release" -jar $BASEDIR/target/benchmarks.jar -foe true -wi 5 -i 5 -f 1 $1
