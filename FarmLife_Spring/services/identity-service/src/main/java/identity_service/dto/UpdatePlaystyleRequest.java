package dto;

import java.util.List;
import java.util.Map;

public record UpdatePlaystyleRequest(
        List<String> preferredCropCodes,
        List<String> preferredAnimalCodes,
        String playStyleTag,
        Map<String, Object> tags
) {}
