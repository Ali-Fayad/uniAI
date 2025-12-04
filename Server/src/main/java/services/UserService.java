package services;
import dto.ResponseDto;
import dto.SignInDto;
import dto.SignUpDto;
import exception.InvalidEmailOrPassword;
import lombok.AllArgsConstructor;
import model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import repository.UserRepository;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public ResponseDto signUp(SignUpDto userDto)
    {
        User user = User.builder().username(userDto.getUsername())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .build();

        User saved = userRepository.save(user);

        ResponseDto result = ResponseDto.builder()
                .username( saved.getUsername())
                .firstName( saved.getFirstName())
                .lastName( saved.getLastName())
                .email( saved.getEmail())
                .isVerified( saved.isVerified())
                .isTwoFacAuth( saved.isTwoFacAuth())
                .build();

        return result;
    }

    public ResponseDto signIn(SignInDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()) == null
                || !passwordEncoder.matches(userDto.getPassword(),
                userRepository.findByEmail(userDto.getEmail()).getPassword()))
            throw new InvalidEmailOrPassword();
        User user = userRepository.findByEmail(userDto.getEmail());
        ResponseDto result = ResponseDto.builder()
                .username( user.getUsername())
                .firstName( user.getFirstName())
                .lastName( user.getLastName())
                .email( user.getEmail())
                .isVerified( user.isVerified())
                .isTwoFacAuth( user.isTwoFacAuth())
                .build();
        return result;
    }

}

//controlelr
// Client --> Controller --> Service --> Repository --> Database;