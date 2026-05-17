error id: file:///C:/Users/ushen/Desktop/UT/firstYear/ESI/ESI_project_2026/sportlink-project/search-service/src/main/java/com/sportlink/search/SearchServiceApplication.java:_empty_/WebClient#builder#
file:///C:/Users/ushen/Desktop/UT/firstYear/ESI/ESI_project_2026/sportlink-project/search-service/src/main/java/com/sportlink/search/SearchServiceApplication.java
empty definition using pc, found symbol in pc: _empty_/WebClient#builder#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 568
uri: file:///C:/Users/ushen/Desktop/UT/firstYear/ESI/ESI_project_2026/sportlink-project/search-service/src/main/java/com/sportlink/search/SearchServiceApplication.java
text:
```scala
package com.sportlink.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class SearchServiceApplication {

    /*
     * WebClient bean used by the Search Service to make synchronous calls to
     * the Booking Service (facility data lookup).
     */
    @Bean
    public WebClient.Builder getWebClientBuilder() {
        return WebClient.b@@uilder();
    }

    public static void main(String[] args) {
        DatabaseInitializer.initialize("searchservice_db");
        SpringApplication.run(SearchServiceApplication.class, args);
    }

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/WebClient#builder#