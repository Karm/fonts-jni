# Test app, JNI, Fonts

```
export JAVA_HOME=/home/karm/tmp/mandrel-java11-21.1-SNAPSHOT/;export PATH=${JAVA_HOME}/bin:${PATH}
mvn clean package
java -jar combined/target/fonts-1.0.jar -agentlib:native-image-agent=config-output-dir=java/src/main/resources/META-INF/native-image
jar uf combined/target/fonts-1.0.jar -C ./java/src/main/resources/ META-INF
native-image --no-fallback --initialize-at-build-time= -jar combined/target/fonts-1.0.jar
./fonts-1.0 -l X -s 3739 -f ./myletter.bmp
```
