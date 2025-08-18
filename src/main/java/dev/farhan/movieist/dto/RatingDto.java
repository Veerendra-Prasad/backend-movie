package dev.farhan.movieist.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RatingDto {
    @Field("Source")
    @JsonProperty("Source")
    private String source;

    @Field("Value")
    @JsonProperty("Value")
    private String value;

    @Override
    public String toString() {
        return "RatingDto{" +
                "source='" + source + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
