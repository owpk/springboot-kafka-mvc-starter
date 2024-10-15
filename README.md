# Spring-boot kafka mvc starter

Spring Boot Starter Apache Kafka Wrapper для обработки сообщений в архитектуре MVC.

![alt main-flow](./doc/flow.png)

## Base concepts

- Автоматически настроенные компоненты для удобной отправки и приема сообщений через Kafka.
- Абстракция `kafka endpoints` поверх kafka топиков для взаимодействия как в стандартном rest http подходе.
- Автоматически настроенные `request` и `reply` топики для каждого клиента и поддержка семантики синхронных request-reply
- TraceId логи сообщений из коробки

## Main components

1. **Сериализация и десериализация**:

   - Интерфейсы и реализации для сериализации и десериализации сообщений Kafka, такие как `KafkaRequestDeserializer` и `KafkaResponseDeserializer` (например, `KafkaRequestDeserializerImpl` и `KafkaResponseDeserializerImpl`).
   - Эти классы обрабатывают преобразование сообщений в объекты Java и обратно.

2. **Аннотации**:

   - Аннотации для маркировки методов и классов, например, `@KafkaMvcController`, `@KafkaMvcMapping`, `@ExceptionHandler`.

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

1. **Добавление зависимости**:

   - Установка локально

   ```sh
   git clone https://github.com/owpk/springboot-kafka-mvc-starter
   cd springboot-kafka-mvc-starter
   ./mvnw clean install
   ```

   - maven

   ```xml
   <dependency>
       <groupId>ru.owpk.kafkamvc</groupId>
       <artifactId>springboot-kafka-mvc-starter</artifactId>
       <version>1.8.0-17</version>
   </dependency>
   ```

   - gradle

   ```groovy
   repositories {
       mavenLocal()
   }

   dependencies {
        implementation 'ru.owpk.kafkamvc:springboot-kafka-mvc-starter:1.8.0-17'
   }
   ```

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
   - По умолчанию bean `KafkaMvcRequestCreator` не создается автоконфигурацией, для этого вы должны создать его вручную:

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

   - application.yml

     ```yml
     kafka-mvc:
       bootstrap-servers: localhost:9092
       consumer:
         name: "service-a-consumer"
         threads:
           max: 50
           start: 10
       producer:
         replyTopic: "service.a.response"
         timeout: 10000
     ```

   - Пример конфигурации:

     ```java
     import ru.owpk.kafkamvc.annotation.EnableKafkaMvcConsumer;
     import ru.owpk.kafkamvc.annotation.EnableKafkaMvcProducer;
     import ru.owpk.kafkamvc.producer.KafkaMvcProducer;
     import ru.owpk.kafkamvc.utils.KafkaMvcRequestCreator;

     @Configuration
     @EnableKafkaMvcProducer
     @EnableKafkaMvcConsumer
     public class BeanConfig {

         @Bean
         public KafkaMvcRequestCreator requestCreator(KafkaMvcProducer kafkaSparuralProducer) {
             return new KafkaMvcRequestCreator(kafkaSparuralProducer);
         }
     }
     ```

   - Пример продюссера:

     ```java
     @Service
     public class ExampleService {

         @Autowired
         private KafkaMvcRequestCreator requestCreator;

         // Async example
         public String fooAsync(MyRequestDtoObj req) {
            var asyncResponse = requestCreator.createRequestBuilder()
                .withAction("/exampleEndpoint")
                .withTopicName("service-b")
                .withRequestBody(req)
                .sendAsync();
            System.out.println(asyncResponse);

         }

         // Sync example
         public void fooSync(MyRequestDtoObj req) {
            OtherResponseDtoObj syncResponse = requestCreator.createRequestBuilder()
                .withAction("/exampleEndpoint")
                .withTopicName("service-b")
                .withRequestBody(req)
                .sendForEntity(OtherResponseDtoObj.class);
            System.out.println(syncResponse);
         }
     }
     ```

   - Пример контроллера:

     ```java
     import ru.owpk.kafkamvc.annotation.Payload;
     import ru.owpk.kafkamvc.annotation.RequestParam;

     @KafkaMvcController(topic = "example-topic")
     public class ExampleController {

         @KafkaMvcMapping("/exampleEndpoint")
         public OtherResponseDtoObj bar(@Payload MyRequestDtoObj request) {
             // Логика обработки
         }

         // @RequestParam - "?key=value" analog
         @KafkaMvcMapping("/exampleEndpointWithRequestVariables")
         public OtherResponseDtoObj bar(@Payload MyRequestDtoObj request, @RequestParam Integer count) {
             // Логика обработки
         }
     }
     ```

Полный пример можно посмотреть тут: https://github.com/owpk/ocrv-kafka-demo
