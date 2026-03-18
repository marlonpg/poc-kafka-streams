# POC Kafka Streams

A simple Kafka Streams application that processes orders from two input topics, merges them, and writes to an output topic.

## Architecture

```
┌──────────────────────┐     ┌─────────────────────────────┐     ┌─────────────────────────┐
│ sales.order.file.v1  │────▶│                             │     │                         │
│  (file-based orders) │     │   Order Stream Processor    │────▶│ sales.order.merged.v1   │
│                      │     │   (Kafka Streams app)       │     │   (merged output)       │
│ sales.order.ws.v1    │────▶│                             │     │                         │
│  (web-service orders)│     └─────────────────────────────┘     └─────────────────────────┘
└──────────────────────┘
```

### Processing Logic

1. Reads orders from `sales.order.file.v1` — tags them with `source=FILE`
2. Reads orders from `sales.order.ws.v1` — tags them with `source=WS`
3. Merges both streams
4. Filters out invalid orders (quantity <= 0)
5. Writes valid merged orders to `sales.order.merged.v1`

## Running

### Start everything with Docker Compose

```bash
docker compose up --build -d
```

This starts:
- **Zookeeper** — Kafka coordination
- **Kafka broker** — single-node cluster
- **init-kafka** — creates the 3 topics automatically
- **order-stream-processor** — the Java Kafka Streams app

### Produce test messages

Open a shell into the Kafka container and produce some test orders:

```bash
# Produce to the FILE topic
docker exec -it kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic sales.order.file.v1 \
  --property "parse.key=true" \
  --property "key.separator=:"

# Then type messages like:
# order-001:{"orderId":"order-001","customerId":"cust-10","product":"Laptop","quantity":2,"price":1499.99}
# order-002:{"orderId":"order-002","customerId":"cust-11","product":"Mouse","quantity":5,"price":29.99}
```

```bash
# Produce to the WS topic
docker exec -it kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic sales.order.ws.v1 \
  --property "parse.key=true" \
  --property "key.separator=:"

# Then type messages like:
# order-101:{"orderId":"order-101","customerId":"cust-20","product":"Keyboard","quantity":1,"price":79.99}
```

### Consume merged output

```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic sales.order.merged.v1 \
  --from-beginning \
  --property print.key=true
```

### View application logs

```bash
docker logs -f order-stream-processor
```

### Stop everything

```bash
docker compose down -v
```

## Order JSON Schema

```json
{
  "orderId": "order-001",
  "customerId": "cust-10",
  "product": "Laptop",
  "quantity": 2,
  "price": 1499.99,
  "source": "FILE"
}
```

## Project Structure

```
├── docker-compose.yml          # Infra + service orchestration
├── Dockerfile                  # Multi-stage build for the Java app
├── pom.xml                     # Maven project with Kafka Streams deps
└── src/main/java/com/example/streams/
    ├── OrderStreamApp.java     # Main application — topology definition
    ├── model/
    │   └── Order.java          # Order POJO
    └── serde/
        └── JsonSerde.java      # JSON serializer/deserializer for Kafka
```
