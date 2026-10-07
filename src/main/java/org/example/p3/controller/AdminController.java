package org.example.p3.controller;

import org.example.p3.model.*;
import org.example.p3.service.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.*;

@Controller
@PreAuthorize("hasAnyAuthority('ADMIN')")
public class AdminController {

    private final RestTemplate restTemplate;

    public AdminController() {
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

    public <T> T fetchFromApi(String endpoint, Class<T> responseType) {
        return restTemplate.getForObject("http://localhost:8080/api/" + endpoint, responseType);
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

    @GetMapping("/adminPanel")
    public String adminPanel(Model model){return "adminPanel";}

    @GetMapping("/applicationList")
    public String applicationList(Model model){
        model.addAttribute("applications", fetchFromApi("application/findPage/1", new ParameterizedTypeReference<List<ApplicationModel>>() {}));
        model.addAttribute("users", fetchFromApi("user/all", new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("pages", fetchFromApi("application/pages", Integer.class));
        return "applicationList";
    }

    @PostMapping("/applicationListPage")
    public String applicationListPage(Model model, @RequestParam int page){
        model.addAttribute("applications", fetchFromApi("application/findPage/%d".formatted(page), new ParameterizedTypeReference<List<ApplicationModel>>() {}));
        model.addAttribute("users", fetchFromApi("user/all", new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("pages", fetchFromApi("application/pages", Integer.class));
        return "applicationList";
    }

    @PostMapping("/applicationListSID")
    public String applicationListID(Model model, @RequestParam int id){
        model.addAttribute("applications", fetchFromApi("application/findID/%d".formatted(id), new ParameterizedTypeReference<List<ApplicationModel>>() {}));
        model.addAttribute("users", fetchFromApi("user/all", new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("pages", fetchFromApi("application/pages", Integer.class));
        return "applicationList";
    }

    @PostMapping("/applications/add")
    public String addApplication(Model model, @RequestParam LocalDate application_date, @RequestParam String status, @RequestParam String text, @RequestParam int account_id){
        sendParamsToApi("application/", HttpMethod.PUT, Map.of("date", application_date, "status", status, "text", text, "account_id", account_id));
        return "redirect:/applicationList";
    }

    @PostMapping("/applications/update")
    public String updateApplication(Model model, @RequestParam int id, @RequestParam LocalDate application_date, @RequestParam String status, @RequestParam String text, @RequestParam int account_id){
        sendParamsToApi("application/", HttpMethod.POST, Map.of("id", id, "date", application_date, "status", status, "text", text, "account_id", account_id));
        return "redirect:/applicationList";
    }

    @PostMapping("/applications/delete")
    public String deleteApplication(Model model, @RequestParam int id){
        sendParamsToApi("application/%d".formatted(id), HttpMethod.DELETE, Map.of());
        return "redirect:/applicationList";
    }

    @GetMapping("/paymentList")
    public String paymentList(Model model){
        model.addAttribute("payments", fetchFromApi("payment/findPage/1", new ParameterizedTypeReference<List<PaymentModel>>() {}));
        model.addAttribute("users", fetchFromApi("user/all", new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("pages", fetchFromApi("payment/pages", Integer.class));
        return "paymentList";
    }

    @PostMapping("/paymentListPage")
    public String paymentListPage(Model model, @RequestParam int page){
        model.addAttribute("payments", fetchFromApi("payment/findPage/%d".formatted(page), new ParameterizedTypeReference<List<PaymentModel>>() {}));
        model.addAttribute("users", fetchFromApi("user/all", new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("pages", fetchFromApi("payment/pages", Integer.class));
        return "paymentList";
    }

    @PostMapping("/paymentListSID")
    public String paymentListID(Model model, @RequestParam int id){
        model.addAttribute("payments", fetchFromApi("payment/findID/%d".formatted(id), new ParameterizedTypeReference<List<PaymentModel>>() {}));
        model.addAttribute("users", fetchFromApi("user/all", new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("pages", fetchFromApi("payment/pages", Integer.class));
        return "paymentList";
    }

    @PostMapping("/payments/add")
    public String addPayment(Model model, @RequestParam LocalDate payment_date, @RequestParam double cost, @RequestParam int account_id){
        sendParamsToApi("payment/", HttpMethod.PUT, Map.of("date", payment_date, "cost", cost, "user_id", account_id));
        return "redirect:/paymentList";
    }

    @PostMapping("/payments/update")
    public String updatePayment(Model model, @RequestParam int id, @RequestParam LocalDate payment_date, @RequestParam double cost, @RequestParam int account_id){
        sendParamsToApi("payment/", HttpMethod.POST, Map.of("id", id, "date", payment_date, "cost", cost, "user_id", account_id));
        return "redirect:/paymentList";
    }

    @PostMapping("/payments/delete")
    public String deletePayment(Model model, @RequestParam int id){
        sendParamsToApi("payment/%d".formatted(id), HttpMethod.DELETE, Map.of());
        return "redirect:/paymentList";
    }

    @GetMapping("/accountList")
    public String accountList(Model model){
        model.addAttribute("accounts", fetchFromApi("account/findPage/1", new ParameterizedTypeReference<List<AccountModel>>() {}));
        model.addAttribute("pages", fetchFromApi("account/pages", Integer.class));
        return "accountList";}

    @PostMapping("/accountListPage")
    public String accountListPage(Model model, @RequestParam int page){
        model.addAttribute("accounts", fetchFromApi("account/findPage/%d".formatted(page), new ParameterizedTypeReference<List<AccountModel>>() {}));
        model.addAttribute("pages", fetchFromApi("account/pages", Integer.class));
        return "accountList";
    }

    @PostMapping("/accountListSID")
    public String accountListID(Model model, @RequestParam int id){
        model.addAttribute("accounts", fetchFromApi("account/findID/%d".formatted(id), new ParameterizedTypeReference<List<AccountModel>>() {}));
        model.addAttribute("pages", fetchFromApi("account/pages", Integer.class));
        return "accountList";
    }

    @PostMapping("/accountListName")
    public String accountListID(Model model, @RequestParam String name){
        model.addAttribute("accounts", fetchFromApi("account/findName/%s".formatted(name), new ParameterizedTypeReference<List<AccountModel>>() {}));
        model.addAttribute("pages", fetchFromApi("account/pages", Integer.class));
        return "accountList";
    }

    @PostMapping("/accounts/add")
    public String addAccount(@RequestParam String username, @RequestParam LocalDate join_date){
        sendParamsToApi("account/", HttpMethod.PUT, Map.of("name", username, "date", join_date));
        return "redirect:/accountList";
    }

    @PostMapping("/accounts/update")
    public String updateAccount(@RequestParam int id, @RequestParam String username, @RequestParam LocalDate join_date){
        sendParamsToApi("account/", HttpMethod.POST, Map.of("id", id, "name", username, "date", join_date));
        return "redirect:/accountList";
    }

    @PostMapping("/accounts/delete")
    public String deleteAccount(@RequestParam int id){
        sendParamsToApi("account/%d".formatted(id), HttpMethod.DELETE, Map.of());
        return "redirect:/accountList";
    }

    @GetMapping("/userList")
    public String userList(Model model){
        model.addAttribute("users", fetchFromApi("user/findPage/1", new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("accounts", fetchFromApi("account/all", new ParameterizedTypeReference<List<AccountModel>>() {}));
        model.addAttribute("seasons", fetchFromApi("season/all", new ParameterizedTypeReference<List<SeasonModel>>() {}));
        model.addAttribute("pages", fetchFromApi("user/pages", Integer.class));
        model.addAttribute("roles", RoleEnum.values());
        return "UserList";
    }
    @PostMapping("/userListSID")
    public String userListID(Model model, @RequestParam int id){
        model.addAttribute("users", fetchFromApi("user/findId/%d".formatted(id), new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("accounts", fetchFromApi("account/all", new ParameterizedTypeReference<List<AccountModel>>() {}));
        model.addAttribute("seasons", fetchFromApi("season/all", new ParameterizedTypeReference<List<SeasonModel>>() {}));
        model.addAttribute("pages", fetchFromApi("user/pages", Integer.class));
        model.addAttribute("roles", RoleEnum.values());
        return "UserList";
    }

    @PostMapping("/userListName")
    public String userListID(Model model, @RequestParam String name){
        model.addAttribute("users", fetchFromApi("user/findName/%s".formatted(name), new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("accounts", fetchFromApi("account/all", new ParameterizedTypeReference<List<AccountModel>>() {}));
        model.addAttribute("seasons", fetchFromApi("season/all", new ParameterizedTypeReference<List<SeasonModel>>() {}));
        model.addAttribute("pages", fetchFromApi("user/pages", Integer.class));
        model.addAttribute("roles", RoleEnum.values());
        return "UserList";
    }
    @PostMapping("/userListPage")
    public String userListPage(Model model, @RequestParam int page){
        model.addAttribute("users", fetchFromApi("user/findPage/%d".formatted(page), new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("accounts", fetchFromApi("account/all", new ParameterizedTypeReference<List<AccountModel>>() {}));
        model.addAttribute("seasons", fetchFromApi("season/all", new ParameterizedTypeReference<List<SeasonModel>>() {}));
        model.addAttribute("pages", fetchFromApi("user/pages", Integer.class));
        model.addAttribute("roles", RoleEnum.values());
        return "UserList";
    }

    @PostMapping("/users/add")
    public String addUser(@RequestParam String username, @RequestParam String password, @RequestParam int account_id, @RequestParam String[] roles){
        List<RoleEnum> roleList = new java.util.ArrayList<>();
        if (roles!= null){
            for (String role : roles){
                roleList.add(RoleEnum.valueOf(role));
            }
        }
        else{
            roleList = List.of(RoleEnum.USER);
        }
        sendParamsToApi("user/", HttpMethod.PUT, Map.of("name", username, "password", password, "roles", roleList, "accountId", account_id));
        return "redirect:/userList";
    }

    @PostMapping("/users/update")
    public String updateUser(@RequestParam int id, @RequestParam String username, @RequestParam String password, @RequestParam int account_id, @RequestParam String[] roles){
        List<RoleEnum> roleList = new java.util.ArrayList<>();
        if (roles!= null){
            for (String role : roles){
                roleList.add(RoleEnum.valueOf(role));
            }
        }
        else {
            roleList = List.of(RoleEnum.USER);
        }
        sendParamsToApi("user/", HttpMethod.POST, Map.of("id", id, "name", username, "password", password, "roles", roleList, "accountId", account_id));
        return "redirect:/userList";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam int id){
        sendParamsToApi("user/%d".formatted(id), HttpMethod.DELETE, Map.of());
        return "redirect:/userList";
    }

    @PostMapping("/users/addSeason")
    public String addUserSeason(@RequestParam int id, @RequestParam int s_id){
        sendParamsToApi("user/addSeason/%d/%d".formatted(id, s_id), HttpMethod.GET, Map.of());
        return "redirect:/userList";
    }

    @PostMapping("/users/removeSeason")
    public String removeUserSeason(@RequestParam int id, @RequestParam int s_id){
        sendParamsToApi("user/deleteSeason/%d/%d".formatted(id, s_id), HttpMethod.GET, Map.of());
        return "redirect:/userList";
    }
    @GetMapping("/donationTypeList")
    public String donationTypeList(Model model){
        model.addAttribute("donation_types", fetchFromApi("donationType/findPage/1", new ParameterizedTypeReference<List<DonationTypeModel>>() {}));
        model.addAttribute("pages", fetchFromApi("donationType/pages", Integer.class));
        return "donationTypeList";}

    @PostMapping("/donationTypeListPage")
    public String DonationTypeListPage(Model model, @RequestParam int page){
        model.addAttribute("donation_types", fetchFromApi("donationType/findPage/%d".formatted(page), new ParameterizedTypeReference<List<DonationTypeModel>>() {}));
        model.addAttribute("pages", fetchFromApi("donationType/pages", Integer.class));
        return "donationTypeList";
    }

    @PostMapping("/donationTypeListSID")
    public String DonationTypeListID(Model model, @RequestParam int id){
        model.addAttribute("donation_types", fetchFromApi("donationType/findId/%d".formatted(id), new ParameterizedTypeReference<List<DonationTypeModel>>() {}));
        model.addAttribute("pages", fetchFromApi("donationType/pages", Integer.class));
        return "donationTypeList";
    }

    @PostMapping("/donationTypes/add")
    public String addDonationType(@RequestParam String name, @RequestParam int cost){
        sendParamsToApi("donationType/", HttpMethod.PUT, Map.of("name", name, "cost", cost));
        return "redirect:/donationTypeList";
    }

    @PostMapping("/donationTypes/update")
    public String updateDonationType(@RequestParam int id, @RequestParam String name, @RequestParam int cost){
        sendParamsToApi("donationType/", HttpMethod.POST, Map.of("id", id, "name", name, "cost", cost));
        return "redirect:/donationTypeList";
    }

    @PostMapping("/donationTypes/delete")
    public String deleteDonationType(@RequestParam int id){
        sendParamsToApi("donationType/%d".formatted(id), HttpMethod.DELETE, Map.of());
        return "redirect:/donationTypeList";
    }

    @GetMapping("/donationList")
    public String donationList(Model model){
        model.addAttribute("donations", fetchFromApi("donation/findPage/1", new ParameterizedTypeReference<List<DonationModel>>() {}));
        model.addAttribute("users", fetchFromApi("user/all", new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("pages", fetchFromApi("donation/pages", Integer.class));
        model.addAttribute("donation_types", fetchFromApi("donationType/all", new ParameterizedTypeReference<List<DonationTypeModel>>() {}));
        return "donationList";
    }

    @PostMapping("/donationListPage")
    public String donationListPage(Model model, @RequestParam int page){
        model.addAttribute("donations", fetchFromApi("donation/findPage/%d".formatted(page), new ParameterizedTypeReference<List<DonationModel>>() {}));
        model.addAttribute("users", fetchFromApi("user/all", new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("pages", fetchFromApi("donation/pages", Integer.class));
        model.addAttribute("donation_types", fetchFromApi("donationType/all", new ParameterizedTypeReference<List<DonationTypeModel>>() {}));
        return "donationList";
    }

    @PostMapping("/donationListSID")
    public String donationListID(Model model, @RequestParam int id){
        model.addAttribute("donations", fetchFromApi("donation/findId/%d".formatted(id), new ParameterizedTypeReference<List<DonationModel>>() {}));
        model.addAttribute("users", fetchFromApi("user/all", new ParameterizedTypeReference<List<UserModel>>() {}));
        model.addAttribute("pages", fetchFromApi("donation/pages", Integer.class));
        model.addAttribute("donation_types", fetchFromApi("donationType/all", new ParameterizedTypeReference<List<DonationTypeModel>>() {}));
        return "donationList";
    }

    @PostMapping("/donations/add")
    public String addDonation(Model model, @RequestParam LocalDate date, @RequestParam int user_id, @RequestParam int type_id){
        sendParamsToApi("donation/", HttpMethod.PUT, Map.of("date", date, "userID", user_id, "typeID", type_id));
        return "redirect:/donationList";
    }

    @PostMapping("/donations/update")
    public String updateDonation(Model model, @RequestParam int id, @RequestParam LocalDate date, @RequestParam int user_id, @RequestParam int type_id){
        sendParamsToApi("donation/", HttpMethod.POST, Map.of("id", id, "date", date, "userID", user_id, "typeID", type_id));
        return "redirect:/donationList";
    }

    @PostMapping("/donations/delete")
    public String deleteDonation(Model model, @RequestParam int id){
        sendParamsToApi("donation/%d".formatted(id), HttpMethod.DELETE, Map.of());
        return "redirect:/donationList";
    }

    @GetMapping("/seasonList")
    public String seasonList(Model model){
        model.addAttribute("seasons", fetchFromApi("season/findPage/1", new ParameterizedTypeReference<List<SeasonModel>>() {}));
        model.addAttribute("pages", fetchFromApi("season/pages", Integer.class));
        return "seasonList";
    }

    @PostMapping("/seasonListPage")
    public String seasonListPage(Model model, @RequestParam int page){
        model.addAttribute("seasons", fetchFromApi("season/findPage/%d".formatted(page), new ParameterizedTypeReference<List<SeasonModel>>() {}));
        model.addAttribute("pages", fetchFromApi("season/pages", Integer.class));
        return "seasonList";
    }

    @PostMapping("/seasonListSID")
    public String seasonListID(Model model, @RequestParam int id){
        model.addAttribute("seasons", fetchFromApi("season/findID/%d".formatted(id), new ParameterizedTypeReference<List<SeasonModel>>() {}));
        model.addAttribute("pages", fetchFromApi("season/pages", Integer.class));
        return "seasonList";
    }

    @PostMapping("/seasons/add")
    public String addSeason(@RequestParam LocalDate start_date, @RequestParam LocalDate end_date, @RequestParam String description, @RequestParam String image){
        sendParamsToApi("season/", HttpMethod.PUT, Map.of("startDate", start_date, "endDate", end_date, "desc", description, "img", image));
        return "redirect:/seasonList";
    }

    @PostMapping("/seasons/update")
    public String updateSeason(@RequestParam int id, @RequestParam LocalDate start_date, @RequestParam LocalDate end_date, @RequestParam String description, @RequestParam String image){
        sendParamsToApi("season/", HttpMethod.POST, Map.of("id", id, "startDate", start_date, "endDate", end_date, "desc", description, "img", image));
        return "redirect:/seasonList";
    }

    @PostMapping("/seasons/delete")
    public String deleteSeason(@RequestParam int id){
        sendParamsToApi("season/%d".formatted(id), HttpMethod.DELETE, Map.of());
        return "redirect:/seasonList";
    }
}
