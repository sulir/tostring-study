#!/bin/bash

: ${1?"Usage: $0 output_directory"}

corpus="$1"
shopt -s globstar
export JAVA_HOME=`dirname $(dirname $(readlink -f $(which javac)))` || exit

download() {
  url="$1"
  dir="$2"

  mkdir -p "$dir"
  curl -Ls "$url" | tar xz -C "$dir" --strip-components=1 || exit
}

ant_proj() {
  name=$1
  url="$2"
  
  src="$corpus/$name/source"
  download "$url" "$src"
  ant -f "$src/build.xml" || exit
  
  deps="$corpus/$name/dependencies"
  mkdir -p "$deps"
  mv "$src"/**/*.jar "$deps"/
  
  ant -f "$src/build.xml" clean
}

gradle_proj() {
  name=$1
  url="$2"
  
  gradle_dir="$corpus/$name/gradle"
  src="$corpus/$name/source"
  download "$url" "$src"
  GRADLE_USER_HOME="$gradle_dir" "$src/gradlew" --no-daemon -p "$src" assemble -x api -x asciidoctor -x dokka -x javadoc -x distZip \
    || GRADLE_USER_HOME="$gradle_dir" "$src/gradlew" --no-daemon -p "$src" assemble -x javadoc \
    || exit
  
  deps="$corpus/$name/dependencies"
  mkdir -p "$deps"
  mv "$gradle_dir/caches/modules-2/files-2.1"/**/*.jar "$deps"/ || exit
  mv "$src"/**/libs/*.jar "$deps"/ || exit
  
  GRADLE_USER_HOME="$gradle_dir" "$src/gradlew" --no-daemon -p "$src" clean
  rm -rf "$gradle_dir"
}

mvn_proj() {
  name=$1
  url="$2"
  
  src="$corpus/$name/source"
  download "$url" "$src"
  mvn -f "$src/pom.xml" package -DskipTests -Dmaven.javadoc.skip || exit
  for dir in "$src"/{,**/}target/; do
    mkdir -p "$dir"/classes/META-INF
    touch "$dir"/classes/META-INF/MANIFEST.MF
  done
  mvn -f "$src/pom.xml" jar:test-jar
  mvn -f "$src/pom.xml" dependency:unpack-dependencies -Dclassifier=sources -DoutputDirectory="$src/deps" --fail-at-end
  
  deps="$corpus/$name/dependencies"
  mvn -f "$src/pom.xml" dependency:copy-dependencies -DoutputDirectory="$deps" || exit
  mv "$src"/{,**/}target/*.jar "$deps/"
  
  mvn -f "$src/pom.xml" dependency:copy-dependencies -Dclassifier=sources -DoutputDirectory="$corpus/dep-stats" --fail-at-end
  mvn -f "$src/pom.xml" clean
}

mvn_stats() {
  deps="$corpus/dep-stats"
  count=`ls "$deps" | perl -pe 's/(.*?)-\d+.*/$1/' | uniq -u | wc -l`
  echo "Number of Maven artifacts: $count."
  rm -rf "$deps"
}

jdk_src() {
  url="https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.3%2B7/OpenJDK11U-jdk_x64_linux_hotspot_11.0.3_7.tar.gz"
  dir="$corpus/jdk/download"
  download "$url" "$dir"
  
  src="$corpus/jdk/source"
  unzip -oq "$dir/lib/src.zip" -d "$src"
 
  deps="$corpus/jdk/dependencies"
  for file in "$dir"/jmods/*.jmod; do
    jmod extract "$file" --dir "$dir/jars"
    jar -c -C "$dir/jars/classes" . > "$dir/jars"/`basename "$file" .jmod`.jar
    rm -rf "$dir/jars/classes"
  done
  mkdir -p "$deps"
  mv "$dir/jars"/*.jar "$deps"/
  
  rm -rf "$dir"
}

ant_proj ant "https://github.com/apache/ant/archive/rel/1.10.6.tar.gz"
mvn_proj antlr "https://github.com/antlr/antlr4/archive/4.7.2.tar.gz"
mvn_proj checkstyle "https://github.com/checkstyle/checkstyle/archive/checkstyle-8.20.tar.gz"
ant_proj freecol "https://github.com/FreeCol/freecol/archive/nightly-2019-01-02.tar.gz"
mvn_proj galleon "https://github.com/wildfly/galleon/archive/3.0.2.Final.tar.gz"
jdk_src
mvn_proj jgroups "https://github.com/belaban/JGroups/archive/JGroups-4.0.19.Final.tar.gz"
mvn_proj jruby "https://github.com/jruby/jruby/archive/9.2.7.0.tar.gz"
mvn_proj jspwiki "https://github.com/apache/jspwiki/archive/2.11.0.M3.tar.gz"
mvn_proj maven "https://github.com/apache/maven/archive/maven-3.6.1.tar.gz"
mvn_proj pmd "https://github.com/pmd/pmd/archive/pmd_releases/6.14.0.tar.gz"
gradle_proj spring "https://github.com/spring-projects/spring-framework/archive/v5.1.7.RELEASE.tar.gz"
mvn_proj struts "https://github.com/apache/struts/archive/STRUTS_2_5_20.tar.gz"
mvn_proj velocity "https://github.com/apache/velocity-engine/archive/2.1.tar.gz"
mvn_proj wildfly "https://github.com/wildfly/wildfly/archive/16.0.0.Final.tar.gz"
mvn_stats
