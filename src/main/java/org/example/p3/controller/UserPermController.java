package org.example.p3.controller;

import org.example.p3.model.DonationTypeModel;
import org.example.p3.model.UserModel;
import org.example.p3.service.UserService;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@PreAuthorize("hasAnyAuthority('USER')")
public class UserPermController {

    private final RestTemplate restTemplate;


    public UserPermController() {
        this.restTemplate = new RestTemplate();
    }

    public <T> T fetchFromApi(String endpoint, ParameterizedTypeReference<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<T> response = restTemplate.exchange(
                "http://localhost:8080/api/" + endpoint,
                HttpMethod.GET,
                entity,
                responseType
        );
        return response.getBody();
    }

    public HttpStatus sendParamsToApi(String endpoint, HttpMethod method, Map<String, ?> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/api/" + endpoint);

        if (params != null) {
            params.forEach((key, value) -> {
                if (value != null) {
                    if (value instanceof Collection) {
                        builder.queryParam(key, ((Collection<?>) value).toArray());
                    } else {
                        builder.queryParam(key, value);
                    }
                }
            });
        }

        return restTemplate.exchange(
                builder.build().toUriString(),
                method,
                null,
                HttpStatus.class
        ).getBody();
    }

    public <T> T fetchFromApi(String endpoint, Class<T> responseType) {
        return restTemplate.getForObject("http://localhost:8080/api/" + endpoint, responseType);
    }

    @GetMapping("/chat")
    public String Chat(Model model) {return "chatConnector";}

    @GetMapping("/players")
    public String Players(Model model, Principal principal) {
        return "players";
    }

    @GetMapping("/donate")
    public String DonationPage(Model model, Principal principal){
        model.addAttribute("donation_types", fetchFromApi("donationType/findPage/1", new ParameterizedTypeReference<List<DonationTypeModel>>() {}));
        model.addAttribute("pages", fetchFromApi("donationType/pages", Integer.TYPE));
        return "playerDonate";
    }

    @PostMapping("/donate/do")
    public String DonationPage(Model model, Principal principal, @RequestParam int type_id){
        String username;
        username = principal.getName();
        int user_id = (int) fetchFromApi("user/findName/%s".formatted(username), new ParameterizedTypeReference<List<UserModel>>() {}).get(0).getId();
        sendParamsToApi("donation/", HttpMethod.PUT, Map.of("date", LocalDate.now(), "typeID", type_id, "userID", user_id));
        return "redirect:/donate";
    }

    @PostMapping("/sendApp")
    public String SendApplication(Model model, Principal principal, @RequestParam String text){
        String username;
        username = principal.getName();
        int user_id = (int) fetchFromApi("user/findName/%s".formatted(username), new ParameterizedTypeReference<List<UserModel>>() {}).get(0).getId();
        sendParamsToApi("application/", HttpMethod.PUT, Map.of("text", text, "date", LocalDate.now(), "status", "На рассмотрении", "userID", user_id));
        return "redirect:/players";
    }
}
