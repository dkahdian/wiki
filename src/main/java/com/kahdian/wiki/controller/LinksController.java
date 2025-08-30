package com.kahdian.wiki.controller;

import com.kahdian.wiki.service.DataParserService;
import com.kahdian.wiki.service.MediaWikiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class LinksController {
    
    private static final Logger logger = LoggerFactory.getLogger(LinksController.class);
    
    private final MediaWikiService mediaWikiService;
    private final DataParserService dataParserService;
    
    public LinksController(MediaWikiService mediaWikiService, 
                          DataParserService dataParserService) {
        this.mediaWikiService = mediaWikiService;
        this.dataParserService = dataParserService;
    }
    
    @GetMapping("/links/{title}")
    public ResponseEntity<Map<String, Object>> getLinkedArticles(@PathVariable String title) {
        
        logger.info("Received request for links in article: {}", title);
        
        if (title == null || title.trim().isEmpty()) {
            logger.warn("Empty title provided");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // Get article links directly from MediaWiki API
            logger.debug("Fetching article links for: {}", title);
            Map<String, Object> apiResponse = mediaWikiService.getArticleLinks(title);
            
            if (apiResponse == null) {
                logger.warn("Failed to retrieve links data for article: {}", title);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Article not found: " + title);
                return ResponseEntity.notFound().build();
            }
            
            logger.debug("API response retrieved, parsing links");
            
            // Parse links from the API response
            logger.debug("Parsing links from API response");
            List<String> linkedTitles = dataParserService.parseLinksFromApiResponse(apiResponse);
            logger.debug("Found {} linked articles", linkedTitles.size());
            
            logger.info("Successfully processed {} linked articles for: {}", linkedTitles.size(), title);
            
            Map<String, Object> result = new HashMap<>();
            result.put("title", title);
            result.put("linkedArticles", linkedTitles);
            result.put("count", linkedTitles.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error processing links for article: {}", title, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch links for article: " + title);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
