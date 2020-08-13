package com.example;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import javax.print.attribute.standard.Media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@SpringBootApplication
public class ReactiveHttpApplication {
	
	
	//functional reactive style end points
	@Bean
	  RouterFunction<ServerResponse> routes(GreetingService gs) {
	    return RouterFunctions.route()
	        .GET("/wishing/{name}", r -> ServerResponse.ok().body(gs.greetOnce(new GreetingRequest(r.pathVariable("name"))), GreetingResponse.class))
	        .GET("/wishings/{name}", r -> ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
	        											.body(gs.greetMany(new GreetingRequest(r.pathVariable("name"))), GreetingResponse.class))
	        .build();
	  }

	public static void main(String[] args) {
		SpringApplication.run(ReactiveHttpApplication.class, args);
	}

}

//Spring MVC style end points
@RestController
@RequiredArgsConstructor
class GreetingRestController{
	
	private final GreetingService greetingService;
	
	@GetMapping("/greeting/{name}")
	public Mono<GreetingResponse> greetings(@PathVariable String name){
		return greetingService.greetOnce(new GreetingRequest(name));
	}
	
}

@Service
class GreetingService{
	
	private GreetingResponse greet(String name) {
		return new GreetingResponse("Hello " +name +" @"+ Instant.now());
	}
	
	Flux<GreetingResponse> greetMany(GreetingRequest request){
		return Flux.fromStream(Stream.generate(() -> greet(request.getName()))).delayElements(Duration.ofSeconds(1));
	}
	
	Mono<GreetingResponse> greetOnce(GreetingRequest request){
		return Mono.just(greet(request.getName()));
	}
	
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingResponse{
	private String message;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class GreetingRequest{
	private String name;
}
