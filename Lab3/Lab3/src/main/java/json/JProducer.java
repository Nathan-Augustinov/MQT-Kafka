package json;

import io.confluent.kafka.serializers.KafkaJsonSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import producer.ExampleProducer;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JProducer {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleProducer.class);

    private static final String BOOTSTRAP_SERVERS = ":9092";
    private static final String CLIENT_ID = "ex";

    private static Producer<String, JCompany> producer;

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSerializer.class.getName());

        producer = new KafkaProducer<>(props);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        //newSingleThreadScheduledExecutor --> Creates an Executor that uses a single worker thread operating off an unbounded queue.
        executor.scheduleAtFixedRate(() -> send(args[0]), 0, 3, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
    }



    public static void send(String topic) {
        final int number = new Random().nextInt(10);
        JCompany jCompany = new JCompany(number, "JCompanyName no. " + number);
        ProducerRecord<String, JCompany> data = new ProducerRecord<>(topic, "key" + number, jCompany);
        try {
            RecordMetadata meta = producer.send(data).get();
            System.out.println(String.format("----------------- Example Producer -------------- key = %s, value = %s => partition = %d, offset= %d", data.key(), data.value(), meta.partition(), meta.offset()));
            //LOG.info("----------------- Example Producer -------------- key = {}, value = {} => partition = {}, offset= {}", data.key(), data.value(), meta.partition(), meta.offset());
        } catch (InterruptedException | ExecutionException e) {
            producer.flush();
        }
    }
}