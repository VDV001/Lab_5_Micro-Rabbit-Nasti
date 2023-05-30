package app.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Producer
{
    @Autowired
    private RabbitTemplate template;
    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.outRoutingKey.name}")
    private String routingKey;
    @Value("${rabbitmq.outQueue.name}")
    private String queue;

    @Bean
    public Queue outQueue()
    {
        return new Queue(queue, true, false, false);
    }
    @Bean
    public TopicExchange exchange()
    {
        return new TopicExchange(exchange);
    }
    @Bean
    public Binding binding(Queue outQueue, TopicExchange exchange)
    {
        return BindingBuilder.bind(outQueue).to(exchange).with(routingKey);
    }

    public void produce(String message)
    {
        template.convertAndSend(exchange, routingKey, message);
        log.info("message send: " + message);
    }
}
