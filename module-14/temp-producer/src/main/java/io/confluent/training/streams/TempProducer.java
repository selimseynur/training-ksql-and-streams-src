package io.confluent.training.streams;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class TempProducer {
    public static void main(String[] args) throws InterruptedException{
        System.out.println("*** Starting Temp Producer");
        Properties props = getConfig();
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        setupShutdownHook(producer);

        produceTemperatures(producer);
    }

    private static void produceTemperatures(KafkaProducer<String, String> producer) throws InterruptedException
    {
        final Random random = new Random();
        final String topic = "temperature-readings";
        final String[] stations = {"S-01", "S-02", "S-03", "S-04", "S-05", "S-06", "S-07", "S-08", "S-09", "S-10"};
        final Integer[] tempAverage = {10, 15, 8, 23, 7, 2, 22, 30, -3, 13};
        Integer[] lastTemperature = {10, 15, 8, 23, 7, 2, 22, 30, -3, 13};
        Long epoch_time = System.currentTimeMillis();
        while(true){
            Integer stationIndex = random.nextInt(9);
            String station = stations[stationIndex];
            Integer diff = tempAverage[stationIndex] - lastTemperature[stationIndex];
            Integer rand = random.nextInt(9);
            Integer delta = 0;
            if (rand < 4){              // 40% chance that temperature stays the same
                delta = 0;
            } else if (rand < 8){       // 40% chance that temperature comes closer to the average
                delta = 1;
            } else {
                delta = -1;
            }
            if (diff < 0){
                delta = -delta;
            }
            Integer temperature = lastTemperature[stationIndex] + delta;
            lastTemperature[stationIndex] = temperature;
            // create a JSON string
            String value = "{ 'station': '" + station + "', 'temperature': " + temperature.toString() + "}";
            ProducerRecord<String, String> rec = new ProducerRecord<>(topic, station, value);
            epoch_time += 1000;         // add one second to simulate one reading per second...

            System.out.println("The record is: " + rec.key() + ", " + rec.value());
            producer.send(rec);

            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    private static Properties getConfig(){
        Properties props = new Properties();
        props.put("bootstrap.servers", "kafka:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        return props;
    }

    private static void setupShutdownHook(KafkaProducer<String, String> producer){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("### Stopping Temp Producer");
            producer.close();
        }));
    }
}