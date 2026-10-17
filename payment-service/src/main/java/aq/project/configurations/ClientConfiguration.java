package aq.project.configurations;

import aq.project.dto.PaymentProviderServiceErrorDto;
import aq.project.exceptions.ClientHttpException;
import aq.project.exceptions.ServiceHttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Configuration
@ImportHttpServices(
        group = "payment-provider-service",
        types = aq.project.payment_provider_service.PaymentApiClient.class
)
@RequiredArgsConstructor
public class ClientConfiguration {

    @Value("${service.payment-provider-service.uri}")
    private String paymentProviderServiceBaseUri;

    private final ObjectMapper objectMapper;

    @Bean
    public RestClientHttpServiceGroupConfigurer restClientHttpServiceGroupConfigurer() {
        return groups -> {
            groups.forEachGroup((group, clientBuilder, factoryBuilder) -> {
                if(group.name().equals("payment-provider-service"))
                    clientBuilder
                        .baseUrl(paymentProviderServiceBaseUri)
                        .defaultStatusHandler(this::isErrorResponse, this::onPaymentProviderServiceErrorResponse)
                        .build();
            });
        };
    }

    private boolean isErrorResponse(
            HttpStatusCode statusCode
    ) {
        return statusCode.is4xxClientError()
                || statusCode.is5xxServerError();
    }

    private void onPaymentProviderServiceErrorResponse(
            HttpRequest request,
            ClientHttpResponse response
    ) throws IOException {
        PaymentProviderServiceErrorDto errorDto = objectMapper
                .readValue(response.getBody(), PaymentProviderServiceErrorDto.class);

        HttpStatusCode responseStatusCode = response.getStatusCode();

        if(responseStatusCode.is4xxClientError())
            throw new ClientHttpException(responseStatusCode.value(), errorDto.getMessage());

        if(responseStatusCode.is5xxServerError())
            throw new ServiceHttpException(responseStatusCode.value(), errorDto.getMessage());
    }
}
