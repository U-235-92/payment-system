package aq.project.configurations;

import aq.project.ApiClient;
import aq.project.client.TransactionApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfiguration {

    @Value("${service.individuals-api.uri}")
    private String individualsApiServiceBaseUri;

    @Bean
    public TransactionApi transactionApi() {
        RestClient restClient = RestClient.builder()
                .baseUrl(individualsApiServiceBaseUri)
                .build();
        ApiClient apiClient = new ApiClient(restClient);
        apiClient.setBasePath(individualsApiServiceBaseUri);
        return new TransactionApi(apiClient);
    }
}
