# Mensageria Assíncrona com Apache ActiveMQ, Docker e Protocolo AMQP

Implementação prática de um sistema de mensageria assíncrona utilizando **Apache ActiveMQ Artemis** em container **Docker** e comunicação via protocolo **AMQP 1.0** em **Java 21** com **Maven**.

O projeto explora os modelos de entrega **Ponto a Ponto (`queue`)**, **Difusão (`topic` / *fan-out*)** e a durabilidade das mensagens nos modos **persistente** e **não persistente**.

---

## Tecnologias Utilizadas

* **Java 21**
* **Apache Maven** (`exec-maven-plugin`)
* **Apache Qpid JMS Client 2.6.0** (cliente AMQP 1.0 sobre Jakarta JMS)
* **Apache ActiveMQ Artemis** (`apache/activemq-artemis:latest-alpine`)
* **Docker & Docker Compose**

---

## Estrutura do Projeto

```text
activemq-amqp-messaging/
├── src/main/java/com/leonardo/amqp/
│   ├── config/
│   │   └── BrokerConfig.java       # Fábrica de conexão AMQP e resolução de destinos
│   ├── producer/
│   │   └── ProducerApp.java        # Publica 5 mensagens com intervalo de 1 minuto
│   └── consumer/
│       └── ConsumerApp.java        # Consome 1 mensagem por instância e encerra
├── docker-compose.yml              # Configuração do broker ActiveMQ Artemis
├── pom.xml                         # Dependências Maven e plugins
└── README.md                       # Documentação do projeto
```

---

## Como Executar

### 1. Iniciar o Broker ActiveMQ no Docker

Na raiz do projeto, suba o container do ActiveMQ Artemis:

```bash
docker compose up -d
```

* **Porta AMQP 1.0:** `amqp://localhost:5672`
* **Console Web Administrativo:** `http://localhost:8161`
* **Credenciais padrão:** usuário `artemis` / senha `artemis`

---

### 2. Cenário 1: Fila (`queue`) com 5 Instâncias Consumidoras

Abra **5 terminais** na raiz do projeto e inicie uma instância do consumidor em cada um (`instancia-1` a `instancia-5`):

```bash
mvn exec:java -Dexec.mainClass="com.leonardo.amqp.consumer.ConsumerApp" -Dexec.args="instancia-1 queue lab.amqp.mensagens"
```

Em seguida, inicie o produtor para publicar 5 mensagens em intervalos de 1 minuto:

```bash
mvn exec:java -Dexec.mainClass="com.leonardo.amqp.producer.ProducerApp" -Dexec.args="queue lab.amqp.mensagens true"
```

> **Resultado esperado:** Cada instância consome exatamente uma mensagem distinta e encerra a execução, demonstrando o balanceamento de carga ponto a ponto.

---

### 3. Cenário 2: Difusão / Fan-out (`topic`)

Inicie múltiplas instâncias consumidoras inscritas no mesmo tópico:

```bash
mvn exec:java -Dexec.mainClass="com.leonardo.amqp.consumer.ConsumerApp" -Dexec.args="instancia-1 topic lab.amqp.fanout"
mvn exec:java -Dexec.mainClass="com.leonardo.amqp.consumer.ConsumerApp" -Dexec.args="instancia-2 topic lab.amqp.fanout"
```

Publique no tópico através do produtor:

```bash
mvn exec:java -Dexec.mainClass="com.leonardo.amqp.producer.ProducerApp" -Dexec.args="topic lab.amqp.fanout true"
```

> **Resultado esperado:** Todas as instâncias ativas recebem simultaneamente uma cópia da mesma mensagem publicada.

---

### 4. Cenário 3: Persistência vs. Não Persistência

Com os consumidores encerrados, envie uma mensagem persistente (`true`) ou não persistente (`false`), reinicie o container e tente consumir a mensagem:

```bash
# 1. Enviar mensagem (interrompa com Ctrl+C após o envio da Mensagem #1)
mvn exec:java -Dexec.mainClass="com.leonardo.amqp.producer.ProducerApp" -Dexec.args="queue lab.amqp.persistente true"

# 2. Reiniciar o broker
docker compose restart

# 3. Consumir após o reinício (opcionalmente informando timeout em ms no 4º argumento)
mvn exec:java -Dexec.mainClass="com.leonardo.amqp.consumer.ConsumerApp" -Dexec.args="instancia-1 queue lab.amqp.persistente 10000"
```

> **Resultado esperado:** Mensagens enviadas com `true` são recuperadas do disco após o reinício; mensagens enviadas com `false` permanecem apenas em memória volátil e são descartadas.

---

## Contexto Acadêmico

* **Instituição:** Instituto Federal Catarinense (IFC) – Campus Rio do Sul
* **Curso:** Bacharelado em Ciência da Computação
* **Disciplina:** Desenvolvimento Web II
* **Professor:** Wesley dos Reis Bezerra
* **Autor:** Leonardo Dümes de Souza
```
