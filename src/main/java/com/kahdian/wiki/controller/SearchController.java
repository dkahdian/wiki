package com.kahdian.wiki.controller;

import com.kahdian.wiki.service.DataParserService;
import com.kahdian.wiki.service.MediaWikiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SearchController {
    
    private final MediaWikiService mediaWikiService;
    private final DataParserService dataParserService;
    
    public SearchController(MediaWikiService mediaWikiService, DataParserService dataParserService) {
        this.mediaWikiService = mediaWikiService;
        this.dataParserService = dataParserService;
    }
    
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Map<String, Object> wikipediaResponse = mediaWikiService.search(query, limit);
            Map<String, Object> parsedResults = dataParserService.parseSearchResults(wikipediaResponse);
            return ResponseEntity.ok(parsedResults);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
