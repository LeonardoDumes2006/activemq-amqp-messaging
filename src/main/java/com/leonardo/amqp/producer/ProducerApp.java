package com.leonardo.amqp.producer;

import com.leonardo.amqp.config.BrokerConfig;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.DeliveryMode;
import jakarta.jms.Destination;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.time.Instant;

public class ProducerApp {

    private static final long INTERVALO_MS = 60_000L; // 1 minuto entre mensagens
    private static final int TOTAL_MENSAGENS = 5;

    public static void main(String[] args) {
        String tipoDestino = args.length > 0 ? args[0].toLowerCase() : "queue";
        String nomeDestino = args.length > 1 ? args[1] : "lab.amqp.mensagens";
        boolean persistente = args.length > 2 ? Boolean.parseBoolean(args[2]) : true;

        ConnectionFactory factory = BrokerConfig.createConnectionFactory();

        try (Connection connection = factory.createConnection()) {
            connection.start();

            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                Destination destination = BrokerConfig.resolveDestination(session, tipoDestino, nomeDestino);

                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(
                        persistente ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT
                );

                System.out.printf("Conectado a %s | Destino: %s (%s) | Persistente: %s%n",
                        BrokerConfig.DEFAULT_BROKER_URL, nomeDestino, tipoDestino, persistente);

                for (int i = 1; i <= TOTAL_MENSAGENS; i++) {
                    String corpo = String.format("Mensagem #%d | %s", i, Instant.now());
                    TextMessage message = session.createTextMessage(corpo);
                    message.setIntProperty("numeroMensagem", i);
                    message.setBooleanProperty("persistente", persistente);

                    producer.send(message);
                    System.out.printf("[%s] Enviada %d/%d -> %s%n",
                            Instant.now(), i, TOTAL_MENSAGENS, corpo);

                    if (i < TOTAL_MENSAGENS) {
                        Thread.sleep(INTERVALO_MS);
                    }
                }

                System.out.println("Publicação das 5 mensagens concluída.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Execução interrompida: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro no produtor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}