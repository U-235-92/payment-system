package aq.project.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

import java.io.IOException;

@Configuration
@ImportHttpServices(
        group = "external-test-service",
        types = aq.project.client.WebhookClientApi.class
)
@RequiredArgsConstructor
public class ClientConfiguration {

    @Value("${app.service.external-test-service.uri}")
    private String externalTestServiceBaseUri;

    @Bean
    public RestClientHttpServiceGroupConfigurer restClientHttpServiceGroupConfigurer() {
        return groups -> {
            groups.forEachGroup((group, clientBuilder, factoryBuilder) -> {
                switch(group.name()) {
                    case "external-test-service" -> clientBuilder
                            .baseUrl(externalTestServiceBaseUri)
                            .defaultStatusHandler(this::isErrorResponse, this::onExternalTestServiceErrorResponse)
                            .build();
                }
            });
        };
    }

    private boolean isErrorResponse(HttpStatusCode statusCode) {
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }

    private void onExternalTestServiceErrorResponse(
            HttpRequest request,
            ClientHttpResponse response
    ) throws IOException {
        String externalTestServiceResponseBody = new String(response.getBody().readAllBytes());

        HttpStatusCode responseStatusCode = response.getStatusCode();

        if(responseStatusCode.is4xxClientError())
            throw new HttpClientErrorException(responseStatusCode, externalTestServiceResponseBody);

        if(responseStatusCode.is5xxServerError())
            throw new HttpServerErrorException(responseStatusCode, externalTestServiceResponseBody);
    }
}