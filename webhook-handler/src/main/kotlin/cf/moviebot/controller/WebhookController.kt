package cf.moviebot.controller

import cf.moviebot.service.TelegramAPIService
import cf.moviebot.service.UpdatePublisherService
import cf.moviebot.shared.domain.Update
import cf.moviebot.shared.logging.logger
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("webhook")
class WebhookController(
    private val telegramService: TelegramAPIService,
    private val updatePublisher: UpdatePublisherService
) {
    private val queues = mutableMapOf<String, String>()

    @PostMapping(value = ["/{token}"])
    suspend fun handleMO(@PathVariable token: String, @RequestBody update: Update): Map<String, String> {
        logger().info("Received MO. token={}, update={}", token, update)
        coroutineScope {
            launch {
                val userName = queues[token] ?: telegramService.getMe(token)?.username
                updatePublisher.publish(token, userName, update)
                logger().info("Published update. queue=$userName")
            }
        }
        return mapOf("ok" to "true")
    }

    @ExceptionHandler(HttpMessageNotReadableException::class, DecodingException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleError(e: Exception) {
        logger().warn("Unable to read incoming update.", e)
    }
}