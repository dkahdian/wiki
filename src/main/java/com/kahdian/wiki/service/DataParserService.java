package com.kahdian.wiki.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataParserService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataParserService.class);
    
    public DataParserService() {
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseSearchResults(Map<String, Object> wikipediaResponse) {
        logger.debug("Parsing Wikipedia search results");
        
        if (wikipediaResponse == null) {
            logger.warn("Wikipedia response is null");
            return createEmptySearchResult();
        }
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> query = (Map<String, Object>) wikipediaResponse.get("query");
            if (query == null) {
                logger.warn("No query section found in Wikipedia response");
                return createEmptySearchResult();
            }
            
            Map<String, Object> searchInfo = (Map<String, Object>) query.get("searchinfo");
            if (searchInfo != null) {
                Object totalHits = searchInfo.get("totalhits");
                if (totalHits != null) {
                    result.put("totalhits", totalHits);
                    logger.debug("Found totalhits: {}", totalHits);
                } else {
                    result.put("totalhits", 0);
                }
            } else {
                result.put("totalhits", 0);
            }
            
            List<Map<String, Object>> searchResults = (List<Map<String, Object>>) query.get("search");
            List<String> titles = new ArrayList<>();
            
            if (searchResults != null) {
                for (Map<String, Object> searchResult : searchResults) {
                    Object title = searchResult.get("title");
                    if (title != null) {
                        titles.add(title.toString());
                    }
                }
                logger.debug("Extracted {} titles from search results", titles.size());
            }
            
            result.put("search", titles);
            
        } catch (Exception e) {
            logger.error("Error parsing Wikipedia search results: {}", e.getMessage(), e);
            return createEmptySearchResult();
        }
        
        logger.debug("Successfully parsed search results with {} titles", 
                     result.get("search") instanceof List ? ((List<?>) result.get("search")).size() : 0);
        return result;
    }
    
    
    public List<String> parseLinksFromApiResponse(Map<String, Object> apiResponse) {
        logger.debug("Parsing links from MediaWiki API response");
        
        if (apiResponse == null) {
            logger.warn("API response is null");
            return new ArrayList<>();
        }
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> parse = (Map<String, Object>) apiResponse.get("parse");
            if (parse == null) {
                logger.warn("No parse section found in API response");
                return new ArrayList<>();
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> links = (List<Map<String, Object>>) parse.get("links");
            if (links == null) {
                logger.warn("No links found in API response");
                return new ArrayList<>();
            }
            
            logger.debug("Found {} raw links in API response", links.size());
            
            List<String> filteredTitles = new ArrayList<>();
            
            for (Map<String, Object> link : links) {
                Object titleObj = link.get("*");
                if (titleObj != null) {
                    String title = titleObj.toString();
                    if (isValidArticleTitle(title)) {
                        filteredTitles.add(title);
                    }
                }
            }
            
            // Sort and remove duplicates
            List<String> result = filteredTitles.stream()
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            
            logger.debug("Filtered to {} valid article titles", result.size());
            return result;
            
        } catch (Exception e) {
            logger.error("Error parsing links from API response: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    private boolean isValidArticleTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        
        // Filter out titles containing "Wikipedia" or "disambiguation"
        String lowerTitle = title.toLowerCase();
        if (lowerTitle.contains("wikipedia") || lowerTitle.contains("disambiguation")) {
            return false;
        }
        
        // Filter out titles with numbers
        if (title.matches(".*[0-9].*")) {
            return false;
        }
        
        // Filter out titles with colons (typically namespace prefixes)
        if (title.contains(":")) {
            return false;
        }
        
        // Filter out titles with dots (typically file extensions)
        if (title.contains(".")) {
            return false;
        }
        
        // Filter out non-ASCII characters
        if (!title.chars().allMatch(c -> c < 128)) {
            return false;
        }
        
        return true;
    }
    
    private Map<String, Object> createEmptySearchResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalhits", 0);
        result.put("search", new ArrayList<String>());
        return result;
    }
}
