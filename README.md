# Points Web Service
Web service for the Fetch Rewards "points" coding exercise.

## Running the App
### Option 1: Java 11 Command Line
1. Make sure Java 11 is installed and on the execution path. It can be downloaded
   [here](https://www.oracle.com/java/technologies/downloads/#java11). 
2. Open a terminal to the project root and run:
    ```
    ./gradlew bootRun
    ```
    
### Option 2: IntelliJ
Open IntelliJ, click "Open or Import", select the "points" project folder, and launch the "PointsApplication"
run configuration.

### Option 3: Docker
Open a terminal to the project root and run:
```
docker build -t points-cbiegay .
docker run -it -p 8080:8080 points-cbiegay
```

## Using the App
The HTTP API can be exercised with any suitable client.  The request payloads follow the examples in the
exercise instructions.

### Endpoints
* Add Transaction
    * `http://localhost:8080/points/transaction` (POST)
* Spend Points
    * `http://localhost:8080/points/spend` (POST)
* Fetch Balances
    * `http://localhost:8080/points/balances` (GET)

### Examples with Curl

#### Add Transaction
```
curl -i \
  -H "Content-Type:application/json" \
  -X POST --data '{ "payer": "DANNON", "points": 1000, "timestamp": "2020-11-02T14:00:00Z" }' \
  http://localhost:8080/points/transaction
```

#### Spend Points
```
curl -i \
  -H "Content-Type:application/json" \
  -X POST --data '{ "points": 100 }' \
  http://localhost:8080/points/spend
```

#### Fetch Balances
```
curl -i http://localhost:8080/points/balances
``` 

## Running the Tests
### Option 1: Java 11 Command Line
Open a terminal to the project root, make sure Java 11 is on the execution path and run:
```
./gradlew test
```
A full report can be found in `build/reports/tests/test/index.html`.

### Option 2: IntelliJ
With the project open in IntelliJ, right-click on the `src/test` folder and choose the option to run the tests.

### Option 3: Docker
Open a terminal to the project root and run:
```
docker build -t points-cbiegay .
docker run -it points-cbiegay test
```
