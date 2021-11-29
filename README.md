# Sensor-Data-Stats

This is an application reading csv file from the given directory as command line argument. It is written using akka stream. Following the command to build the jar : 

```
sbt clean
sbt compile
sbt test
sbt assembly
```

Once the application jar is build(location of the jar will be at ```target\scala-2.13\SensorDataAccumulator-assembly-0.1.0-SNAPSHOT.jar```). you can run the application by :

```java -jar SensorDataAccumulator-assembly-0.1.0-SNAPSHOT.jar %CSV_DIR%```

I have tested this application for almost 450 files with approx 32+ GB data size. It worked fine.

It has fault-tolerance for empty line. It will skip the line if it is empty or doesn't have sensor-id. Also, It NaN, Blank or any other humidity values are considered as NaN in the application. I have written some test cases written for all major components. I also tried to print the rough amount for time taken by the application. 

I also tried to parallelize the folding operation with Graph DSL balance and merge, but It didn't produce better results. I kept the code there for parallelization.