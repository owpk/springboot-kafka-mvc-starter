package ru.owpk.kafkamvc.consumer;

import static ru.owpk.kafkamvc.model.serialization.HeaderEnum.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.PropertyResolver;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.owpk.kafkamvc.KafkaMvcBaseConfig;
import ru.owpk.kafkamvc.annotation.KafkaMvcController;
import ru.owpk.kafkamvc.annotation.KafkaMvcMapping;
import ru.owpk.kafkamvc.consumer.processors.AnnotationProcessor;
import ru.owpk.kafkamvc.consumer.processors.ProcessorsHolder;
import ru.owpk.kafkamvc.exception.KafkaControllerException;
import ru.owpk.kafkamvc.exception.KafkaControllerNotFoundException;
import ru.owpk.kafkamvc.exception.KafkaCreateBeanException;
import ru.owpk.kafkamvc.exception.KafkaSerializationException;
import ru.owpk.kafkamvc.handler.KafkaMvcExceptionHandlerBean;
import ru.owpk.kafkamvc.invokation.KafkaMvcInvokeHandlers;
import ru.owpk.kafkamvc.model.DefaultExceptionMessageBody;
import ru.owpk.kafkamvc.model.KafkaRequestMessage;
import ru.owpk.kafkamvc.model.KafkaResponseMessage;

@Slf4j
@RequiredArgsConstructor
public class KafkaMvcConsumer {

    private final ApplicationContext ctx;
    private final ConsumerFactory<String, KafkaRequestMessage> kafkaConsumerFactory;
    private final KafkaTemplate replyTemplate;
    private final KafkaMvcExceptionHandlerBean exceptionHandler;
    private final PropertyResolver propertyResolver;
    private final RequestGateway requestGateway;
    private final Map<String, GenericMessageListenerContainer> containerListeners = new HashMap<>();
    private final Map<ControllerEntryKey, ControllerEntry> controllerMethods = new HashMap<>();
    private final KafkaMvcInvokeHandlers kafkaMvcInvokeHandlers;

    @PostConstruct
    public void init() {
        log.debug("Processing sparural kafka controllers");
        ctx.getBeansWithAnnotation(KafkaMvcController.class).forEach((beanName, bean) -> {
            log.debug("{} :: {}", beanName, bean);
            try {
                String topic = processListener(bean);
                processControllerMethods(bean, topic);
            } catch (KafkaControllerException ex) {
                log.error(String.format("Error on create kafka controller for bean: '%s'", beanName), ex);
            }
        });
    }

    @PreDestroy
    public void unload() {
        containerListeners.values().forEach(GenericMessageListenerContainer::stop);
    }

    private String processListener(Object bean) throws KafkaControllerException {
        KafkaMvcController annot = bean.getClass().getAnnotation(KafkaMvcController.class);
        if (annot == null) {
            throw new KafkaControllerException(
                    "Unexpected exception: missing @" + KafkaMvcController.class.getSimpleName() + " annotation");
        }

        String topic = propertyResolver.resolvePlaceholders(annot.topic());
        String idleInterval = propertyResolver.resolvePlaceholders(annot.idleInterval());

        long idle;
        try {
            idle = Long.parseLong(idleInterval);
        } catch (Exception e) {
            throw new KafkaControllerException("Idle interval should represent digit value");
        }

        if (!StringUtils.hasText(topic)) {
            throw new KafkaControllerException(
                    "@" + KafkaMvcController.class.getSimpleName() + " annotation has not topic name");
        }

        if (!containerListeners.containsKey(topic)) {
            ContainerProperties containerProperties = new ContainerProperties(topic);
            containerProperties.setIdleBetweenPolls(idle);
            containerProperties.setMessageListener((MessageListener<String, KafkaRequestMessage>) this::processMessage);
            ConcurrentMessageListenerContainer container = new ConcurrentMessageListenerContainer<>(
                    kafkaConsumerFactory, containerProperties);
            containerListeners.put(topic, container);
            log.debug("Current listeners: {}", containerListeners);
            container.start();
        }
        return topic;
    }

    private void processControllerMethods(Object bean, String topic) {
        for (Method method : bean.getClass().getMethods()) {
            KafkaMvcMapping mapping = method.getAnnotation(KafkaMvcMapping.class);
            if (mapping == null) {
                continue;
            }

            ControllerEntryKey key = new ControllerEntryKey(topic, mapping.value());
            if (controllerMethods.containsKey(key)) {
                throw new KafkaCreateBeanException(
                        String.format("Kafka controller for action '%s' and topic '%s' already exist", mapping.value(),
                                topic));
            }

            ControllerEntry processor = new ControllerEntry(bean, method);
            controllerMethods.put(key, processor);
        }
    }

    private void processMessage(ConsumerRecord<String, KafkaRequestMessage> message) {
        requestGateway.submitTask(() -> {
            KafkaResponseMessage response = createResponse(message.value());
            try {
                String correlationId = convertRequestHeaderToString(CORRELATION_ID.getHeader(), message)
                        .orElseThrow(() -> new KafkaControllerException("Cannot process correlation id header value",
                                message));

                String traceId = convertRequestHeaderToString(TRACE_ID.getHeader(), message)
                        .orElse(null);

                if (message.value().getException() != null) {
                    throw message.value().getException();
                }
                String topic = message.topic();
                String action = message.value().getAction();
                log.debug("Received message to action '{}' for topic '{}'", action, topic);
                ControllerEntryKey key = new ControllerEntryKey(topic, action);
                ControllerEntry processor = controllerMethods.get(key);
                if (processor == null) {
                    log.warn("Action '{}' for topic '{}' does not exist", action, topic);
                    throw new KafkaControllerNotFoundException(topic, action);
                }
                Method method = processor.getMethod();
                Object[] values = fillInvokeArgs(message, method);
                long tm = System.currentTimeMillis();
                kafkaMvcInvokeHandlers.setCurrentThreadRequest(message.value());

                MDC.put(KafkaMvcBaseConfig.MDC_TRACE_ID_KEY, traceId);
                MDC.put(KafkaMvcBaseConfig.MDC_CORRELATION_ID_KEY, correlationId);

                Object responsePayload = method.invoke(processor.getBean(), values);
                log.info(
                        "Consumer timelog '{}' for topic '{}' at method {}.{} with correlationId '{}' processed at {}ms",
                        action,
                        topic,
                        processor.getBean().getClass().getSimpleName(),
                        method.getName(),
                        correlationId,
                        System.currentTimeMillis() - tm);
                response.setPayloadType(method.getGenericReturnType());
                response.setPayload(responsePayload);
                response.setStatus(KafkaResponseStatus.SUCCESS);
                sendResponse(response);
                MDC.remove(KafkaMvcBaseConfig.MDC_TRACE_ID_KEY);
            } catch (IOException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | KafkaControllerException | KafkaControllerNotFoundException
                    | KafkaSerializationException ex) {
                ex.printStackTrace();
                log.error("consumer exception: ", ex);
                response = exceptionHandler.handleException(ex, message.value());
                log.error("exception message: {}", ex.getCause() != null
                        ? ex.getCause().getLocalizedMessage()
                        : "no message present");
                if (response == null) {
                    response = createResponse(message.value());
                    response.setStatus(KafkaResponseStatus.STATUS_CODE.status(500));
                    response.setPayload(createDefaultMessageBody(ex));
                    log.warn("Kafka process message unexpected exception", ex);
                }

                if (response.getPayloadType() == null) {
                    Type payloadType = response.getPayload() != null ? response.getPayload().getClass() : Void.TYPE;
                    response.setPayloadType(payloadType);
                }

                sendResponse(response);
            }
        });
    }

    private Optional<String> convertRequestHeaderToString(String requestHeader,
            ConsumerRecord<String, KafkaRequestMessage> message) throws KafkaControllerException {
        return Stream.of(message.headers().toArray())
                .filter(header -> requestHeader.equals(header.key()))
                .map(header -> new String(header.value() != null
                        ? header.value()
                        : new byte[0]))
                .findFirst();
    }

    private KafkaResponseMessage createResponse(KafkaRequestMessage request) {
        KafkaResponseMessage response = new KafkaResponseMessage();
        response.setCorrelationId(request.getCorrelationId());
        response.setReplyTopic(request.getReplyTopic());
        return response;
    }

    private DefaultExceptionMessageBody createDefaultMessageBody(Exception e) {
        return new DefaultExceptionMessageBody(false,
                e.getCause() != null ? e.getCause().getLocalizedMessage() : e.getLocalizedMessage());
    }

    Object[] fillInvokeArgs(ConsumerRecord<String, KafkaRequestMessage> message, Method method)
            throws KafkaControllerException, IOException, ClassNotFoundException {
        var params = method.getParameters();
        var values = new Object[params.length];

        for (int idx = 0; idx < params.length; idx++) {
            Parameter parameter = params[idx];
            var annotations = parameter.getDeclaredAnnotations();
            AnnotationProcessor annotationProcessor = null;

            for (Annotation annotation : annotations)
                annotationProcessor = ProcessorsHolder
                        .getProcessor(annotation.annotationType());

            if (Objects.nonNull(annotationProcessor))
                values[idx] = annotationProcessor.evaluate(parameter, message);

            if (parameter.getType().equals(Headers.class)) {
                values[idx] = message.headers();
            } else if (parameter.getType().equals(KafkaRequestMessage.class)) {
                values[idx] = message.value();
            }
        }
        return values;
    }

    @SuppressWarnings("unchecked")
    private void sendResponse(KafkaResponseMessage response) {
        try {
            if (response.getReplyTopic() != null) {
                ProducerRecord<String, KafkaResponseMessage> record = new ProducerRecord<>(response.getReplyTopic(),
                        UUID.randomUUID().toString(), response);
                replyTemplate.send(record);
            }
        } catch (RuntimeException ex) {
            log.error("Error on send kafka response", ex);
        }
    }
}
