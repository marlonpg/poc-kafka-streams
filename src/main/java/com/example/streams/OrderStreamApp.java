package com.example.streams;

import com.example.streams.model.Order;
import com.example.streams.serde.JsonSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class OrderStreamApp {

    private static final Logger log = LoggerFactory.getLogger(OrderStreamApp.class);

    private static final String TOPIC_FILE = "sales.order.file.v1";
    private static final String TOPIC_WS = "sales.order.ws.v1";
    private static final String TOPIC_OUTPUT = "sales.order.merged.v1";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "order-stream-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
                System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"));
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);

        StreamsBuilder builder = new StreamsBuilder();
        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class);

        // Read from 'file' topic and tag with source
        KStream<String, Order> fileOrders = builder
                .stream(TOPIC_FILE, Consumed.with(Serdes.String(), orderSerde))
                .peek((key, order) -> {
                    order.setSource("FILE");
                    log.info("[FILE]  key={} order={}", key, order);
                });

        // Read from 'ws' (web-service) topic and tag with source
        KStream<String, Order> wsOrders = builder
                .stream(TOPIC_WS, Consumed.with(Serdes.String(), orderSerde))
                .peek((key, order) -> {
                    order.setSource("WS");
                    log.info("[WS]    key={} order={}", key, order);
                });

        // Merge both streams into one
        KStream<String, Order> mergedOrders = fileOrders.merge(wsOrders);

        // Process: filter out orders with quantity <= 0, then write to output topic
        mergedOrders
                .filter((key, order) -> order.getQuantity() > 0)
                .peek((key, order) -> log.info("[MERGED] key={} order={}", key, order))
                .to(TOPIC_OUTPUT, Produced.with(Serdes.String(), orderSerde));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        // Graceful shutdown
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down streams...");
            streams.close();
            latch.countDown();
        }));

        try {
            log.info("Starting Order Stream Processor...");
            log.info("Consuming from: {} and {}", TOPIC_FILE, TOPIC_WS);
            log.info("Producing to:   {}", TOPIC_OUTPUT);
            streams.start();
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
