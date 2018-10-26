## Prepare

```
$ docker run --rm --name adder-db -e MYSQL_ALLOW_EMPTY_PASSWORD=true -p 3306:3306 mysql:8.0.13
```

```
$ echo "
    echo '
      create database adder;
      create user adder;
      grant all on adder.* to adder;
    ' | mysql
  " | docker exec -i adder-db bash
```

## Run

```
$ sbt jetty:start jetty:join
```

## Test

Five replicas are running on ports 8080-8084.  Try different
combinations of these:

```
$ curl http://localhost:8080
0
$ curl http://localhost:8081 -X POST -d 1
$ curl http://localhost:8082
1
$ curl http://localhost:8083 -X POST -d 11
$ curl http://localhost:8084
12
$ curl http://localhost:8080 -X POST -d 13
$ curl http://localhost:8081
25
$ curl http://localhost:8082 -X POST -d 17
$ curl http://localhost:8083
42
```

## Clean up

```
$ docker kill adder-db
```
