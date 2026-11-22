package ai_service.dto;

import java.util.List;

public record AskGuideResponse(
        String question,
        String answer,
        List<String> citedChunkTitles
) {}
