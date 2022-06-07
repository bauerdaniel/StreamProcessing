# Activity Statistics

Daniel Bauer  
Jenestin Anthonipillai

## Running Locally

1. Clone Git repository  
   `git clone https://github.com/bauerdaniel/StreamProcessing.git`  
   `cd StreamProcessing/activitystatistics`
2. Run Kafka Cluster with Docker  
   `docker compose up`
3. Start App via IDE
4. Produce test data  
   `cd scripts`  
   `./produce-test-data`

## Query the API
This application exposes the activity statistics using Kafka Streams interactive queries feature. The API is listening on port `7000`.
You can call the API via the Terminal or, e.g., via PostMan.

### Get all activity statistic entries, grouped by activity (i.e. _activityId_)

```sh
$ curl -s localhost:7000/activity-statistics
```

### Get the statistics for a specific activity (i.e. _activityId_)
```sh
$ curl -s localhost:7000/activity-statistics/6

[
  {
    "activityId": 6,
    "userId": 3,
    "activityName": "Lunch at SQUARE",
    "userName": "Amber",
    "viewDuration": 90.0
  },
  {
    "activityId": 6,
    "userId": 2,
    "activityName": "Lunch at SQUARE",
    "userName": "Obi-Wan",
    "viewDuration": 25.0
  },
  {
    "activityId": 6,
    "userId": 4,
    "activityName": "Lunch at SQUARE",
    "userName": "Johnny",
    "viewDuration": 120.0
  },
  ...
]
```
