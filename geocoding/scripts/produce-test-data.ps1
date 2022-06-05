docker compose exec kafka bash -c "
  kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic geolocation-events \
  --property 'parse.key=true' \
  --property 'key.separator=|' < geolocation-events.json"
