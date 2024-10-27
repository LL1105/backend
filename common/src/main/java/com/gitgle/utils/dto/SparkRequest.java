package com.gitgle.utils.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SparkRequest implements Serializable {

    private String model;

    private String user;

    private List<SparkRequestMessage> messages;

    private Double temperature;

    @JsonProperty("top_k")
    private Integer topK;

    private Integer stream;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @JsonProperty("presence_penalty")
    private Integer presencePenalty;

    @JsonProperty("frequency_penalty")
    private Integer frequencyPenalty;

    @JsonProperty("response_format")
    private String responseFormat;

    @JsonProperty("suppress_plugin")
    private String suppressPlugin;

}
