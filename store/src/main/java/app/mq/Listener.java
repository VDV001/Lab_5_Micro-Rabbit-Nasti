package app.mq;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Listener
{
    @Autowired
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
        String backMessage;
        try
        {
            double value = Double.parseDouble(message);
            if (value >= 0)
                backMessage = String.valueOf(value);
            else
                backMessage = "Not enough product in stock";
        }
        catch (NumberFormatException e)
        {
            backMessage = "Count is not double";
        }
        catch (NullPointerException e)
        {
            backMessage = "Message is empty";
        }
        producer.produce(backMessage);
    }
}
