docker compose exec kafka bash -c "
  kafka-console-consumer \
  --bootstrap-server kafka:9092 \
  --topic geocoding \
  --from-beginning"
