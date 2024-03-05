package com.springboot.learning.backend.api.integration.retry;

import com.springboot.learning.backend.api.integration.exception.MicroserviceCalledException;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

@Component
public class RetryStrategy {

    @Bean
    public RetryBackoffSpec retryBackoff() {

        return Retry.backoff(3, Duration.ofSeconds(1)).jitter(0.75)
            .filter(MicroserviceCalledException.class::isInstance)
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                new MicroserviceCalledException("The maximum number of attempts has been reached"));
    }
}
