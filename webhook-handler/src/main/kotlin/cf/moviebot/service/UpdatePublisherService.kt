package cf.moviebot.service

import cf.moviebot.shared.domain.Update
import org.springframework.amqp.core.Exchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class UpdatePublisherService(
    private val rabbitTemplate: RabbitTemplate,
    private val webhookExchange: Exchange,
    private val registrationService: QueueRegistrationService
) {
    fun publish(token: String, queueName: String?, updates: Update) {
        val queue = queueName ?: "default"
        registrationService.registerBotQueue(queue)
        rabbitTemplate.convertAndSend(webhookExchange.name, queue, updates) {
            it.messageProperties.headers["token"] = token
            it
        }
    }
}