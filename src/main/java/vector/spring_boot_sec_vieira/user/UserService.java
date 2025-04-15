package vector.spring_boot_sec_vieira.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void changePassword (ChangePasswordRequest request, Principal connectedUser) {
            var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

            if (!passwordEncoder.matches(request.getCurrentPassword(),user.getPassword())){
                throw new IllegalStateException("Wrong password");
            }

            if(!request.getNewPassword().equals(request.getConfirmationPassword())){
                throw new IllegalStateException("Passwords do not match");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

    }
}
