package com.kahdian.wiki.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class MediaWikiService {
    
    private static final Logger logger = LoggerFactory.getLogger(MediaWikiService.class);
    private static final String WIKIPEDIA_API_BASE = "https://en.wikipedia.org/w/api.php";
    private final RestTemplate restTemplate;
    
    public MediaWikiService() {
        this.restTemplate = new RestTemplate();
        // Configure RestTemplate with proper User-Agent header as required by Wikipedia
        this.restTemplate.setInterceptors(List.of((request, body, execution) -> {
            request.getHeaders().add("User-Agent", "KahdianBot/1.0 (https://github.com/dkahdian/wiki; david@kahdian.com)");
            return execution.execute(request, body);
        }));
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> search(String query, int limit) {
        URI uri = UriComponentsBuilder.fromUriString(WIKIPEDIA_API_BASE)
                .queryParam("action", "query")
                .queryParam("list", "search")
                .queryParam("srsearch", query)
                .queryParam("srlimit", limit)
                .queryParam("format", "json")
                .build()
                .toUri();
        
        logger.debug("Making Wikipedia search request: {}", uri);
        return restTemplate.getForObject(uri, Map.class);
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> getArticleLinks(String title) {
        URI uri = UriComponentsBuilder.fromUriString(WIKIPEDIA_API_BASE)
                .queryParam("action", "parse")
                .queryParam("page", title)
                .queryParam("section", "0")
                .queryParam("prop", "links")
                .queryParam("format", "json")
                .build()
                .toUri();
        
        logger.debug("Making Wikipedia links request: {}", uri);
        
        try {
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            logger.debug("Successfully retrieved links data for article: {}", title);
            return response;
        } catch (Exception e) {
            logger.error("Error fetching Wikipedia article links for title '{}': {}", title, e.getMessage());
            return null;
        }
    }
}
