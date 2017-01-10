package db.garagedays.solrcloud.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 10.01.17.
 */
@Component
@ConfigurationProperties(prefix="cloudconfig")
public class ConfigurationClassProperties {

    Map<String,ConfigClass> configs;

    public Map<String, ConfigClass> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, ConfigClass> configs) {
        this.configs = configs;
    }
}
