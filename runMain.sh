#/bin/sh

echo "Going to Home directory..."
pwd

java -Djava.util.logging.config.file=logging.properties -classpath "Data.Mining.jar:lib/weka.jar" mao.datamining.Main $@

