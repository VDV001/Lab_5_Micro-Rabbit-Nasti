package app.mq;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Listener
{
    public String getLastMessage()
    {
        return lastMessage;
    }
    private String lastMessage;
    private Producer producer;
    @Value("${rabbitmq.exchange.name}")
    private String exchange;
    @Value("${rabbitmq.inRoutingKey.name}")
    private String routingKey;
    @Value("${rabbitmq.inQueue.name}")
    private String queue;
    @Bean
    public Queue inQueue()
    {
        return new Queue(queue, true, false, false);
    }
    @Bean
    public Binding inBinding(Queue inQueue, TopicExchange exchange)
    {
        return BindingBuilder.bind(inQueue).to(exchange).with(routingKey);
    }
    @RabbitListener(queues = "${rabbitmq.inQueue.name}")
    public void consume(String message)
    {
        log.info("message: " + message);
        lastMessage = message;
    }
}
