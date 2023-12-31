package consumer;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import producer.AvroCompany;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

public class AvroConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleConsumer.class);

    private static final String BOOTSTRAP_SERVERS = ":9092";
    private static final String GROUP_ID = "ex3";
    private static final String OFFSET_RESET = "earliest";
    private static final String OUR_SCHEMA_REGISTRY_URL = "http://localhost:8081";



    @SuppressWarnings("boxing")
    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", OUR_SCHEMA_REGISTRY_URL);


        try (KafkaConsumer<String, AvroCompany> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singleton(args[0]), new ConsumerRebalanceListener() {

                @Override
                public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                    LOG.info("onPartitionsRevoked - partitions:{}", formatPartitions(partitions));
                }

                @Override
                public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                    LOG.info("onPartitionsAssigned - partitions: {}", formatPartitions(partitions));

                }
            });
            while (true) {
                ConsumerRecords<String, AvroCompany> records = consumer.poll(Duration.ofSeconds(2));
                for (ConsumerRecord<String, AvroCompany> data : records) {
                    LOG.info("--------------  Example Consumer ----------- topic = {}, key = {}, value = {} => partition = {}, offset= {}", args[0], data.key(), data.value(), data.partition(), data.offset());

                }
            }
        } catch (Exception e) {
            LOG.error("Something goes wrong: {}", e.getMessage(), e);
        }
    }
    @SuppressWarnings("boxing")
    public static String formatPartitions(Collection<TopicPartition> partitions) {
        return partitions.stream()
                .map(topicPartition -> String.format("topic: %s, partition: %s", topicPartition.topic(), topicPartition.partition()))
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
