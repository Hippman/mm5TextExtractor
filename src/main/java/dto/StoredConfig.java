package dto;

import enums.Operations;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class StoredConfig {
    Map<Operations, ConfigLine> values = new HashMap<>();
}
