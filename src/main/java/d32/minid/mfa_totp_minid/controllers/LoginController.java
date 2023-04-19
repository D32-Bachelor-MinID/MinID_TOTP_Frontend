package d32.minid.mfa_totp_minid.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import d32.minid.mfa_totp_minid.repository.UserRepository;
import d32.minid.mfa_totp_minid.security.SessionHandler;
import d32.minid.mfa_totp_minid.user.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Controller
@PropertySource("classpath:application.properties")
public class LoginController {
    @Value("${rest.api.url.user}")
    private String BASE_URL;
    @Autowired
    RestTemplate template;
    //private UserRepository userRepository;

    @GetMapping("/")
    public String login(@RegisteredOAuth2AuthorizedClient("idporten") OAuth2AuthorizedClient authorizedClient, HttpSession session) {
        SessionHandler sessionHandler = new SessionHandler(session);
        if(!sessionHandler.exists(session) || !sessionHandler.hasAttribute(session)) {
            return "login";
        }
        return "redirect:/mfa";
    }
    @PostMapping("/login")
    public String loginPost(String pid, String password, HttpSession session, Model model) {
        //User user = userRepository.findByPid(pid);
        User user = template.getForEntity(BASE_URL + "/" + pid, User.class).getBody();
        System.out.println(user);
        if (Objects.nonNull(user) && BCrypt.verifyer().verify(password.toCharArray(), user.getPassword()).verified) {
            session.setAttribute("PID", pid);
            return "redirect:/mfa";
        }
        session.invalidate();
        model.addAttribute("loginError", "Invalid username or password");
        return "login";
    }
}
