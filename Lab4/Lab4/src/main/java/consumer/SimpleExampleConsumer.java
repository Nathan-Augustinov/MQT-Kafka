package consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class SimpleExampleConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleExampleConsumer.class);

    private static final String OUR_BOOTSTRAP_SERVERS = ":9092";
    private static final String OFFSET_RESET = "earliest";
    private static final String OUR_CONSUMER_GROUP_ID = "group_1";
    private static final String topicName = "events2";
    private static final String topicName2 = "events1";
    private static final String OUR_CLIENT_ID = "firstProducer";

    private static Producer<String, String> kafkaProducer;

    KafkaConsumer<String, String> kafkaConsumer;

    public SimpleExampleConsumer(Properties consumerPropsMap, Properties prodcerPropsMap){
        kafkaConsumer = new KafkaConsumer<String, String>(consumerPropsMap);
        kafkaProducer = new KafkaProducer<String, String>(prodcerPropsMap);
    }

    public static Properties buildConsumerPropsMap(){
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, OUR_BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, OUR_CONSUMER_GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        return props;
    }

    public static Properties buildProducerPropsMap(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, OUR_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, OUR_CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        props.put(ProducerConfig.BATCH_SIZE_CONFIG,16384);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG,1048576);

        return props;
    }

    public void pollKafka(String kafkaTopicName){

        kafkaConsumer.subscribe(Collections.singleton(kafkaTopicName));
        //kafkaConsumer.subscribe(List.of(topicName, topicName2));

        Duration pollingTime = Duration.of(2, ChronoUnit.SECONDS);
        while (true){
            // get records from kafka
            //The poll method is a blocking method waiting for specified time in seconds.
            // If no records are available after the time period specified, the poll method returns an empty ConsumerRecords.
            ConsumerRecords<String, String> records = kafkaConsumer.poll(pollingTime);

            // consume the records
            records.forEach(crtRecord -> {
                LOG.info("------ Simple Example Consumer ------------- topic ={}  key = {}, value = {} => partition = {}, offset = {}",kafkaTopicName, crtRecord.key(), crtRecord.value(), crtRecord.partition(), crtRecord.offset());
                if(crtRecord.value().equals("v7")){
                    ProducerRecord<String, String> data = new ProducerRecord<>(topicName2, crtRecord.key(), crtRecord.value());
                    try{
                        RecordMetadata metadata = kafkaProducer.send(data).get();
                        LOG.info("------ Producer topic - events1 ------ key = {}, value = {} ==> partition = {}, offset = {}", data.key(), data.value(), metadata.partition(), metadata.offset());
                    }
                    catch(InterruptedException | ExecutionException e){
                        kafkaProducer.flush();
                    }

                }
            });
        }
    }

    public static void main(String[] args) {
        SimpleExampleConsumer consumer = new SimpleExampleConsumer(buildConsumerPropsMap(), buildProducerPropsMap());
        consumer.pollKafka("events2");
    }
}
