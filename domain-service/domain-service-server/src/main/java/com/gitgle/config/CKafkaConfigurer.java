package com.gitgle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kafka")
public class CKafkaConfigurer {

    private String bootstrapServers;

    private String saslJaasConfig;

    public String getBootstrapServers(){
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers){
        this.bootstrapServers = bootstrapServers;
    }

    public String getSaslJaasConfig(){
        return saslJaasConfig;
    }

    public void setSaslJaasConfig(String saslJaasConfig){
        this.saslJaasConfig = saslJaasConfig;
    }
}