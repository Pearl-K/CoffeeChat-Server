# RabbitMQ Scalability Guide

## 1. 고정 파티션의 문제점과 확장성 확보의 필요성

현재 시스템은 vCPU 개수를 고려하여 3개의 파티션으로 고정되어 있다.
이는 초기 설계 시 합리적인 결정이었으나, 향후 서버 스펙 변경이나 트래픽 증가 시 유연하게 대처하기 어렵다는 단점이 있다.


특히 Consistent Hash Exchange를 사용하는 상황에서 파티션 수를 변경하면 해시 결과가 완전히 바뀌기 때문에, 
기존 채팅방의 메시지 순서 보장이 깨지는 심각한 문제가 발생한다.

따라서 코드 변경 없이 설정을 통해 파티션과 컨슈머 수를 동적으로 조절하고, 파티션 수 변경 없이 확장이 가능한 아키텍처로 개선할 수 있다.

---
## 2. 확장성 개선 방안
### 2.1. 설정 분리를 통한 유연성 확보

가장 먼저, 현재 하드 코딩되어 있는 파티션 개수를 `application.yml` 설정 파일로 분리해야 한다.

#### `application.yml` 설정

파티션 개수를 명시적으로 설정하고, 컨슈머 수를 이 값에 연동시킨다.

```yaml
spring:
  rabbitmq:
    # ... other settings
    partition-count: 3 # 파티션 개수 설정
    listener:
      simple:
        # 파티션 수와 컨슈머(워커) 수를 동적으로 동기화
        concurrency: ${spring.rabbitmq.partition-count}
        # ...
```

#### `RabbitConfig.java` 수정

`@Value` 어노테이션을 통해 설정 값을 읽어와 동적으로 큐를 생성한다.

```java
@Configuration
public class RabbitConfig {

    @Value("${spring.rabbitmq.partition-count}")
    private int partitionCount; // 설정 파일에서 파티션 수 주입

    public static final String CHAT_QUEUE_NAME_PREFIX = "chat.queue.";

    // ...

    @Bean
    public Declarables partitionedQueuesAndBindings() {
        List<Declarable> declarables = new ArrayList<>();
        for (int i = 0; i < partitionCount; i++) { // 설정값 기반으로 큐 동적 생성
            Queue queue = QueueBuilder.durable(CHAT_QUEUE_NAME_PREFIX + i)
                    // ... DLQ 설정
                    .build();
            declarables.add(queue);

            Binding binding = BindingBuilder.bind(queue)
                    .to(chatExchange())
                    .with(String.valueOf(i))
                    .noargs();
            declarables.add(binding);
        }
        return new Declarables(declarables);
    }
}
```

#### 프로그래밍 방식의 리스너 등록

`@RabbitListener` 어노테이션은 동적으로 큐를 할당하는 데 한계가 있다. 따라서 `SimpleMessageListenerContainer`를 직접 `Bean` 으로 등록하여 런타임에 큐를 할당하는 방식으로 변경해야 한다.

1.  **`ChatMessageConsumer.java`에서 `@RabbitListener` 제거**
    ```java
    @Component
    @RequiredArgsConstructor
    public class ChatMessageConsumer {
        // ...
        // @RabbitListener(...) // 어노테이션 제거
        public void receiveMessage(ChatMessage message) { // public 메소드로 유지
            // ...
        }
    }
    ```

2.  **`RabbitConfig.java`에 리스너 컨테이너 `Bean` 추가**
    ```java
    @Configuration
    public class RabbitConfig {
        // ...

        @Bean
        public SimpleMessageListenerContainer chatMessageListenerContainer(
                ConnectionFactory connectionFactory,
                ChatMessageConsumer chatMessageConsumer,
                MessageConverter messageConverter,
                @Value("${spring.rabbitmq.partition-count}") int partitionCount) {

            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);

            // 설정값에 따라 동적으로 큐 이름 배열 생성
            String[] queueNames = new String[partitionCount];
            for (int i = 0; i < partitionCount; i++) {
                queueNames[i] = CHAT_QUEUE_NAME_PREFIX + i;
            }
            container.setQueueNames(queueNames);

            // 컨슈머 스레드 수 설정
            container.setConcurrentConsumers(partitionCount);

            // 메시지 리스너 어댑터 설정
            MessageListenerAdapter adapter = new MessageListenerAdapter(chatMessageConsumer, "receiveMessage");
            adapter.setMessageConverter(messageConverter);
            container.setMessageListener(adapter);

            return container;
        }
    }
    ```

### 2.2. 사전 파티셔닝 (Pre-partitioning) 전략

해시 일관성 문제를 근본적으로 해결하기 위한 가장 효과적인 전략이다.

-   **개념**: 현재 필요한 것보다 훨씬 많은 수의 파티션(예: 12개, 24개)을 미리 생성한다.
-   **실행**:
    1.  `application.yml`의 `partition-count`를 12와 같이 넉넉한 값으로 설정한다.
    2.  `concurrency`는 현재 서버 스펙에 맞게 3으로 유지한다.
-   **동작**: 3개의 컨슈머 스레드가 12개의 큐를 나누어 처리한다. (각 스레드가 4개의 큐 담당)
-   **장점**:
    -   **무중단 확장**: 향후 서버 증설 시, 파티션 자체를 변경할 필요 없이 `concurrency` 값만 늘리거나 애플리케이션 인스턴스를 추가하는 것만으로 병렬성 정도를 수정하면서도 수평 확장이 가능하다.
    -   **해시 일관성 보장**: 파티션 수가 고정되므로 해시 충돌이나 메시지 순서 변경 문제가 발생하지 않는다.

---
## 3. 결론 (개선 방향 정리)

**설정 분리**와 **사전 파티셔닝** 전략을 함께 사용한다면 가장 이상적인 방법으로, 순서가 깨지지 않으면서도 동적 큐 관리가 가능할 것이다.
아래와 같이 깔끔하게 정리할 수 있다.

1.  코드를 수정하여 `application.yml`을 통해 파티션과 컨슈머 관련 설정을 관리하도록 변경한다.
2.  `partition-count`를 정할 때, 서비스에 예상되는 최대 트래픽을 고려하여 넉넉하게(ex. 12) 설정한다.
3.  `concurrency`는 현재 서버 스펙과 부하에 맞게 (ex. 3) 설정한다.

개선을 통해, 향후 서비스 규모 성장에 따라 코드 수정이나 서비스 중단 없이 유연하고 안정적으로 시스템을 확장할 수 있다.
