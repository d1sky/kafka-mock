package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import com.google.common.io.Resources;

public class Consumer {
    public static void main(final String[] args) throws IOException {
        KafkaConsumer<String, String> consumer;
        try (InputStream props = Resources.getResource("consumer.properties").openStream()) {
            final Properties properties = new Properties();
            properties.load(props);
            consumer = new KafkaConsumer<>(properties);
        }

        // слушаем технический топик
        consumer.subscribe(Arrays.asList("mock.input"));

        KafkaProducer<String, String> producer;
        try (InputStream props = Resources.getResource("producer.properties").openStream()) {
            final Properties properties = new Properties();
            properties.load(props);
            producer = new KafkaProducer<>(properties);
        }

        while (true) {
            final ConsumerRecords<String, String> records = consumer.poll(100);

            for (final ConsumerRecord<String, String> record : records) {
                switch (record.topic()) {
                    case "mock.input":
                        // SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

                        MyTimeTask task = new MyTimeTask(producer);
                        task.setMessage(record);

                        Date date = new Date(Long.parseLong(record.key()));
                        Calendar timeout = Calendar.getInstance();

                        // время через сколько отправить в коненый топик
                        timeout.setTimeInMillis(date.getTime() + 30000);
                        Timestamp timestamp = new Timestamp(timeout.getTimeInMillis());

                        Timer timer = new Timer();
                        timer.schedule(task, timestamp);

                        System.out.println(
                                "Received mock.input message - key: " + record.key() + " value: " + record.value());
                        break;
                    default:
                        throw new IllegalStateException(
                                "Shouldn't be possible to get message on topic " + record.topic());
                }
            }
        }
    }
}

class MyTimeTask extends TimerTask {
    KafkaProducer<String, String> timeProducer;
    ConsumerRecord<String, String> record;

    public MyTimeTask(KafkaProducer<String, String> producer) {
        // TODO Auto-generated constructor stub
        this.timeProducer = producer;
    }

    public void setMessage(ConsumerRecord<String, String> record) {
        this.record = record;
    }

    public void run() {
        java.util.Date date = new java.util.Date();
        System.out.println(new Timestamp(date.getTime()));

        // отправляем в коненый топик
        timeProducer.send(new ProducerRecord<String, String>("mock.output", // topic
                record.key(), // key
                System.currentTimeMillis() + "some_value_" + record.value())); // value
        timeProducer.flush();
    }
}
