package d32.minid.mfa_totp_minid.controllers;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.fasterxml.jackson.databind.DatabindContext;
import d32.minid.mfa_totp_minid.repository.UserRepository;
import d32.minid.mfa_totp_minid.security.SessionHandler;
import d32.minid.mfa_totp_minid.user.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;

@Controller
public class LoginController {
    @Autowired
    private UserRepository userRepository;
    private DatabindContext session;

    @GetMapping("/")
    public String login(@RegisteredOAuth2AuthorizedClient("idporten") OAuth2AuthorizedClient authorizedClient, HttpSession session) {
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        System.out.println("access token: " + accessToken.getTokenValue());
        SessionHandler sessionHandler = new SessionHandler(session);
        if(!sessionHandler.exists(session) || !sessionHandler.hasAttribute(session)) {
            System.out.println("Logincontroller");
            return "login";
        }
        return "redirect:/mfa";
    }
    @PostMapping("/login")
    public String loginPost(String pid, String password, HttpSession session, Model model) {
        System.out.println("post login with pid: " + pid + " and password: " + password);
        User user = userRepository.findByPid(pid);
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
