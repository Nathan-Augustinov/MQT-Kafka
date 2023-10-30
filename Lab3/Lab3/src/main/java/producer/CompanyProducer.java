package producer;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class CompanyProducer {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleExampleProducer.class);

    private static final String OUR_BOOTSTRAP_SERVERS = ":9092";
    // private static final String OUR_BOOTSTRAP_SERVERS = "localhost:9092, localhost:9093, localhost:9094";
    private static final String OUR_CLIENT_ID = "firstProducer";
    private static final String OUR_SCHEMA_REGISTRY_URL = "http://localhost:8081";

//    private static Producer<String, Company> producer;
    private static Producer<String, AvroCompany> producer;

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, OUR_BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, OUR_CLIENT_ID);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", OUR_SCHEMA_REGISTRY_URL);



        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 1048576);

        producer = new KafkaProducer<>(props);

//        Company company = new Company("company", 1, "CompanyName");
        AvroCompany avroCompany = new AvroCompany(2, "AvroCompanyName");

        // create message for topic events2 with key
//        ProducerRecord<String, Company> companyData = new ProducerRecord<>("events2", "key", company);
        ProducerRecord<String, AvroCompany> avroCompanyData = new ProducerRecord<>("events2", "key", avroCompany);

        try {
//            RecordMetadata meta = producer.send(companyData).get();
//            LOG.info("aaaaaaaaaaaaaaaaaaaaaaaa key = {}, value = {} ==> partition = {}, offset = {}", companyData.key(), companyData.value(), meta.partition(), meta.offset());

            RecordMetadata meta = producer.send(avroCompanyData).get();
            LOG.info("aaaaaaaaaaaaaaaaaaaaaaaa key = {}, value = {} ==> partition = {}, offset = {}", avroCompanyData.key(), avroCompanyData.value(), meta.partition(), meta.offset());

        } catch (InterruptedException | ExecutionException e) {
            producer.flush();
        }
        producer.close();
    }
}
