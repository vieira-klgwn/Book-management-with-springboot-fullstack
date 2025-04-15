package vector.spring_boot_sec_vieira.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vector.spring_boot_sec_vieira.config.JwtService;
import vector.spring_boot_sec_vieira.token.Token;
import vector.spring_boot_sec_vieira.token.TokenRepository;
import vector.spring_boot_sec_vieira.token.TokenType;
import vector.spring_boot_sec_vieira.user.Role;
import vector.spring_boot_sec_vieira.user.User;
import org.springframework.http.HttpHeaders.*;
import vector.spring_boot_sec_vieira.user.UserRepository;

import java.io.IOException;



@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register (RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken: " + request.getEmail());
        }
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName((request.getLastName()))
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(request.getRole() != null ? request.getRole() : Role.USER) // Default to USER
                .build();
        var savedUser = userRepository.save(user);
        var token = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser,token);
        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();


    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail()).orElse(null);
        var token = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user,token);
        revokeAllUserToken(user);
        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();

    }

    private void revokeAllUserToken(User user) {
        var validTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validTokens.isEmpty()) {
            return;
        }
        validTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validTokens);


    }

    private void saveUserToken(User savedUser, String jwttoken) {
        var token =  Token.builder()
                .token(jwttoken)
                .user(savedUser)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);

    }

    public void refreshToken (HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String email;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        refreshToken = authHeader.substring(7);
        email = jwtService.extractUsername(refreshToken);
        if(email != null) {
            var user = userRepository.findByEmail(email).orElse(null);
            if (jwtService.isTokenValid(refreshToken,user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserToken(user);
                saveUserToken(user,refreshToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }

        }


    }




}
