docker compose exec kafka bash -c "
  kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic users \
  --property 'parse.key=true' \
  --property 'key.separator=|' < users.json"
docker compose exec kafka bash -c "
  kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic activities \
  --property 'parse.key=true' \
  --property 'key.separator=|' < activities.json"
docker compose exec kafka bash -c "
  kafka-console-producer \
  --bootstrap-server kafka:9092 \
  --topic statistic-events < statistic-events.json"