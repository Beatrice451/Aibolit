# Реализация отправки писем

(ЭТО Я ПИШУ САМ, НЕ НЕЙРОСЕТИ, ЧЕССЛОВО)

> Задача: Отправлять письмо пользователю при смене статуса заказа на READY_FOR_PICKUP (иликакевотам).

## Архитектура
Использовать **Spring Application Events** + `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Async`.  

Почему:

- **Декаплинг**: Сервис заказов не знает про почту.
- **Транзакционная** целостность: Письмо отправится только если транзакция БД успешно закоммитилась.
- **Производительность**: Отправка почты вынесена в отдельный поток, не блокирует ответ API.
___

## Реализация

### Создание события

Легковесный record, содержащий только необходимые данные. Не передавать целые сущности!

```java
public record OrderReadyForPickupEvent(
    Integer orderId,
    String userEmail,
    String userName
) {}
```

### Публикация события
В методе изменения статуса публикуем событие (код примерный, копировать свой сервис в лом):

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void updateStatus(Integer orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        
        order.setStatus(newStatus);
        // Hibernate dirty checking сохранит изменения при коммите

        if (newStatus == OrderStatus.READY_FOR_PICKUP) {
            eventPublisher.publishEvent(new OrderReadyForPickupEvent(
                order.getId(),
                order.getUser().getEmail(),
                order.getUser().getName()
            ));
        }
    }
}
```

### Обработчик события (Listener)

Важно: `phase = TransactionPhase.AFTER_COMMIT`  
Так слушатель сработает ТОЛЬКО после успешного коммита транзакции. Чтобы не получилось так, что письмо ушло, а транзакция упала и откатилась
```java
@Component
@RequiredArgsConstructor
public class OrderEventListener {
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderReadyForPickup(OrderReadyForPickupEvent event) {
        emailService.sendReadyForPickupEmail(event);
    }
}
```

### Асинхронная отправка письма 
Выносим отправку в другой поток, чтоб не тормозить весь бэк.  
Код примерный!

```java
@Service
@Slf4j
public class EmailService {
    
    @Async("emailTaskExecutor")
    public void sendReadyForPickupEmail(OrderReadyForPickupEvent event) {
        try {
            // Логика формирования и отправки письма
            log.info("Email sent to {} for order {}", event.userEmail(), event.orderId());
        } catch (Exception e) {
            log.error("Failed to send email for order {}: {}", event.orderId(), e.getMessage());
            // сюда ретраи
        }
    }
}
```

### Конфигурация Async Executor

> \- "Зачем? Ведь есть SimpleAsyncTaskExecutor"  

Так надо (вроде `SimpleAsyncTaskExecutor` на каждую задачу новый поток создаёт).

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);      // Минимальное кол-во потоков
        executor.setMaxPoolSize(5);       // Максимальное кол-во потоков
        executor.setQueueCapacity(100);   // Очередь задач
        executor.setThreadNamePrefix("email-sender-");
        executor.initialize();
        return executor;
    }
}
```

## Нюансики
1. Обработка ошибок в `@Async`:
   - Исключения в асинхронных методах не пробрасываются вызывающему потоку.
   - Обязательно оборачивать логику в try-catch внутри асинхронного метода и логировать ошибки.
   - Для глобальной обработки реализовать `AsyncUncaughtExceptionHandler`.
2. Гарантии доставки:
   - Если приложение упадет сразу после коммита БД, но до отправки письма — письмо будет потеряно.
   - Решение: Либо использовать брокер сообщений (RabbitMQ/Kafka) с persistence, либо писать запись о необходимости отправки в отдельную таблицу БД (outbox pattern) и иметь джобу-отправщик.
3. Идемпотентность:  
   - При использовании механизмов retry одно письмо может уйти дважды.
   - Решение: Генерировать уникальный messageId или проверять флаг `is_email_sent` в БД перед отправкой.