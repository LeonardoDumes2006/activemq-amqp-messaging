package com.leonardo.amqp.consumer;

import com.leonardo.amqp.config.BrokerConfig;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

public class ConsumerApp {

    public static void main(String[] args) {
        String instanciaId = args.length > 0 ? args[0] : "instancia-1";
        String tipoDestino = args.length > 1 ? args[1].toLowerCase() : "queue";
        String nomeDestino = args.length > 2 ? args[2] : "lab.amqp.mensagens";
        long timeoutMs = args.length > 3 ? Long.parseLong(args[3]) : 360_000L;

        ConnectionFactory factory = BrokerConfig.createConnectionFactory();

        try (Connection connection = factory.createConnection()) {
            connection.setClientID("consumer-" + instanciaId + "-" + System.nanoTime());
            connection.start();

            try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                Destination destination = BrokerConfig.resolveDestination(session, tipoDestino, nomeDestino);

                MessageConsumer consumer = session.createConsumer(destination);
                System.out.printf("[%s] Aguardando 1 mensagem em %s (%s)...%n",
                        instanciaId, nomeDestino, tipoDestino);

                Message msg = consumer.receive(timeoutMs);
                if (msg instanceof TextMessage textMessage) {
                    System.out.printf("[%s] Recebeu: %s%n", instanciaId, textMessage.getText());
                } else if (msg != null) {
                    System.out.printf("[%s] Recebeu mensagem não textual: %s%n", instanciaId, msg);
                } else {
                    System.out.printf("[%s] Nenhuma mensagem recebida em %d ms.%n",
                            instanciaId, timeoutMs);
                }
            }
        } catch (Exception e) {
            System.err.printf("[%s] Erro no consumidor: %s%n", instanciaId, e.getMessage());
            e.printStackTrace();
        }
    }
}