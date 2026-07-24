package aq.project.utils.migration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter @Setter
@RequiredArgsConstructor
@Profile({ "dev-shard", "prod" })
@ConfigurationProperties(prefix = "application")
class Datasource {

    private List<String> datasource;
}
