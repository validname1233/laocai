package indi.dkx.laocai.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageSubType {
    NORMAL("normal"),
    STICKER("sticker");

    @JsonValue
    private final String value;
}
