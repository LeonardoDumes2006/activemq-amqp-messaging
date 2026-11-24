package com.leonardo.amqp.config;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import org.apache.qpid.jms.JmsConnectionFactory;

public final class BrokerConfig {

    public static final String DEFAULT_BROKER_URL = "amqp://localhost:5672";
    public static final String DEFAULT_USER = "artemis";
    public static final String DEFAULT_PASSWORD = "artemis";

    private BrokerConfig() {
    }

    public static ConnectionFactory createConnectionFactory() {
        return createConnectionFactory(DEFAULT_BROKER_URL, DEFAULT_USER, DEFAULT_PASSWORD);
    }

    public static ConnectionFactory createConnectionFactory(String brokerUrl, String user, String password) {
        return new JmsConnectionFactory(user, password, brokerUrl);
    }

    public static Destination resolveDestination(Session session, String destinationType, String destinationName)
            throws JMSException {
        if ("topic".equalsIgnoreCase(destinationType)) {
            return session.createTopic(destinationName);
        }
        return session.createQueue(destinationName);
    }
}