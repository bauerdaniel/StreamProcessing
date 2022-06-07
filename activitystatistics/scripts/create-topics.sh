echo "Waiting for Kafka to come online..."
cub kafka-ready -b kafka:9092 1 20
# create the statistic-events topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic statistic-events \
  --replication-factor 1 \
  --partitions 1 \
  --create
# create the activities topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic activities \
  --replication-factor 1 \
  --partitions 1 \
  --create
# create the users topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic users \
  --replication-factor 1 \
  --partitions 1 \
  --create
# create the activity-statistics topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic activity-statistics \
  --replication-factor 1 \
  --partitions 1 \
  --create
sleep infinity
