# Springboot kafka mvc starter

![alt main-flow](./doc/flow.png)

## Base concepts

- Автоматически настроенные компоненты для удобной отправки и приема сообщений через Kafka.
- Стартер устанавливает верхнеуровневую абстракцию `kafka endpoints` внутри kafka топиков, как в стандартном rest http подходе.
- Поддержка и настройка "из коробки" `request` и `reply` топиков для каждого клиента позволяющие реализовать семантику синхронных request-reply

## Main components

1. **Сериализация и десериализация**:

   - Интерфейсы и реализации для сериализации и десериализации сообщений Kafka, такие как `KafkaRequestDeserializer` и `KafkaResponseDeserializer` (например, `KafkaRequestDeserializerImpl` и `KafkaResponseDeserializerImpl`).
   - Эти классы обрабатывают преобразование сообщений в объекты Java и обратно.

2. **Аннотации**:

   - Проект использует аннотации для маркировки методов и классов, например, `@KafkaMvcController`, `@KafkaMvcMapping`, `@ExceptionHandler`.
   - Аннотации помогают в автоматической конфигурации и обработке исключений.

3. **Конфигурация**:

   - Классы, такие как `KafkaMvcConsumerAutoconfiguration` и `KafkaMvcProducerAutoconfiguration`, отвечают за настройку потребителей и производителей Kafka.
   - Эти классы создают фабрики потребителей и производителей, а также шаблоны Kafka для отправки и получения сообщений.

4. **Обработка сообщений**:

   - `KafkaMvcConsumer` и `KafkaMvcProducer` обрабатывают входящие и исходящие сообщения.
   - `KafkaMvcConsumer` использует `RequestGateway` для управления потоками и обработки задач.

5. **Исключения и обработка ошибок**:

   - `KafkaMvcExceptionHandlerBean` и связанные с ним аннотации обрабатывают исключения, возникающие при обработке сообщений.
   - `KafkaSerializationException` используется для обработки ошибок сериализации.

6. **Утилиты и вспомогательные классы**:
   - `KafkaMvcRequestCreator` и `KafkaMvcRequestBuilder` помогают в создании и отправке запросов.
   - `KafkaAdminProvider` управляет настройками Kafka Admin.

## Usage example

Этот проект представляет собой Spring Boot Starter, который интегрируется с Apache Kafka для обработки сообщений в архитектуре MVC. Вот как вы можете использовать его в своем проекте:

1. **Добавление зависимости**:

   - Убедитесь, что ваш проект имеет зависимость от этого стартер-пакета в файле `pom.xml` или `build.gradle`.

2. **Конфигурация**:

   - В `application.properties` или `application.yml` укажите настройки для Kafka-mvc, такие как
     `kafka-mvc.bootstrap-servers`, `kafka-mvc.consumer.name`, `kafka-mvc.producer.replyTopic`

3. **Аннотации**:

   - Используйте аннотацию `@EnableKafkaMvcConsumer` для включения функциональности потребителя Kafka.
   - Используйте аннотацию `@EnableKafkaMvcProducer` для включения функциональности производителя Kafka.

4. **Создание контроллеров**:

   - Создайте классы, аннотированные `@KafkaMvcController`, чтобы определить обработчики сообщений Kafka. Укажите `topic` и `idleInterval` в аннотации.
   - Внутри контроллера используйте аннотацию `@KafkaMvcMapping` для методов, которые будут обрабатывать определенные действия.

5. **Обработка исключений**:

   - Создайте классы, аннотированные `@KafkaMvcExceptionHandler`, для обработки исключений, возникающих при обработке сообщений.

6. **Отправка сообщений**:

   - Используйте `KafkaMvcRequestCreator` для отправки сообщений. Вы можете отправлять сообщения синхронно или асинхронно, используя методы `send` и `sendAsync`.
   - По умолчанию bean `KafkaMvcRequestCreator` не создается автоконфигурацией, для этого вы должны создать его сами:

   ```java
   @Configuration
   public class BeanConfig {
        @Bean
        KafkaMvcRequestCreator requestCreator(KafkaMvcProducer producer) {
            return new KafkaMvcRequestCreator(producer);
        }
   }
   ```

7. **Пример использования**:

   - Пример контроллера:

     ```java
     @KafkaMvcController(topic = "example-topic")
     public class ExampleController {

         @KafkaMvcMapping("exampleAction")
         public String handleExampleAction(KafkaRequestMessage request) {
             // Логика обработки
             return "Response";
         }
     }
     ```

Полный пример можно посмотреть тут: https://github.com/owpk/ocrv-kafka-demo
