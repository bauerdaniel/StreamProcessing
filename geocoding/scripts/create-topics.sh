echo "Waiting for Kafka to come online..."
cub kafka-ready -b kafka:9092 1 20
# create the geolocation-events topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic geolocation-events \
  --replication-factor 1 \
  --partitions 1 \
  --create
# create the geocoding topic
kafka-topics \
  --bootstrap-server kafka:9092 \
  --topic geocoding \
  --replication-factor 1 \
  --partitions 1 \
  --create
sleep infinity
