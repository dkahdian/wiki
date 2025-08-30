# Wikipedia API Service

A Spring Boot REST API for searching Wikipedia articles and exploring article connections through a graph-based approach.

## Features

- **Article Search**: Search for Wikipedia articles using query terms
- **Link Exploration**: Get all Wikipedia articles linked from a specific article
- **Filtered Results**: Smart filtering removes disambiguation pages, special pages, and non-ASCII content

## API Endpoints

### Health Check
```
GET /
```
Returns server status and timestamp.

### Search Articles
```
GET /search?q={query}&limit={limit}
```
- `q` (required): Search query string
- `limit` (optional): Number of results to return (default: 10)

Returns Wikipedia articles matching the search query.

### Get Article Links
```
GET /links/{title}
```
- `title`: Wikipedia article title (spaces allowed)

Returns all Wikipedia articles linked from the specified article, filtered to include only main namespace articles with ASCII titles.

## Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.6+ (or use included Maven wrapper)

### Running the Application

#### Development Mode (JVM)
```bash
./mvnw spring-boot:run
```

#### Production Build
```bash
./mvnw clean package
java -jar target/wiki-0.0.1-SNAPSHOT.jar
```

#### Native Image (GraalVM)
```bash
./mvnw native:compile -Pnative
./target/wiki
```

### Example Usage

Search for articles:
```bash
curl "http://localhost:8080/search?q=Albert%20Einstein&limit=5"
```

Get links from an article:
```bash
curl "http://localhost:8080/links/Albert_Einstein"
```

## Architecture

The application follows a layered architecture:

- **Controllers**: Handle HTTP requests and responses
- **Services**: Business logic and external API integration
- **MediaWikiService**: Interfaces with Wikipedia's API
- **DataParserService**: Processes and filters Wikipedia responses

## Technology Stack

- Spring Boot 3.5.5
- Spring Modulith
- Java 21
- Maven
- GraalVM Native Image support

## API Documentation

See `wikiapispec.md` for detailed API specifications and usage examples.