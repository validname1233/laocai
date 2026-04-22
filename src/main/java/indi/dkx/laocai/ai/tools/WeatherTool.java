package indi.dkx.laocai.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.web.reactive.function.client.WebClient;

public class WeatherTool {
    @Tool(name = "WeatherTool", description = "Get weather information for a given city")
    public String getWeather(@ToolParam(description = "The city to get weather information for") String city) {

        WebClient webClient = WebClient.builder()
                .baseUrl("https://m5487tujd3.re.qweatherapi.com")
                .defaultHeader("Authorization", "Bearer 48f9211e1c6840ee9f3a5f0334be52c0")
                .build();

        String block = webClient.get()
                .uri(String.format("/geo/v2/city/lookup?location=%s", city))
                .retrieve()
                .bodyToMono(String.class).block();

        String block1 = webClient.get()
                .uri(String.format("/v7/weather/now?=%s", city))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return String.format("Weather in %s is sunny with a temperature of %s degrees Celsius.", city, "25");
    }
}
