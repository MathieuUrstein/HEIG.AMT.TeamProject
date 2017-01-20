package ch.heigvd.gamification.web.api;

import ch.heigvd.gamification.dao.ApplicationRepository;
import ch.heigvd.gamification.dto.CredentialsDTO;
import ch.heigvd.gamification.exception.ConflictException;
import ch.heigvd.gamification.model.Application;
import ch.heigvd.gamification.util.JWTUtils;
import ch.heigvd.gamification.util.URIs;
import ch.heigvd.gamification.validator.FieldsRequiredAndNotEmptyValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(URIs.REGISTER)
public class RegisterEndpoint implements RegisterApi {
    private final ApplicationRepository applicationRepository;

    @Autowired
    public RegisterEndpoint(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new FieldsRequiredAndNotEmptyValidator(CredentialsDTO.class));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity register(@Valid @RequestBody CredentialsDTO credentials) {
        Application app = applicationRepository.findByName(credentials.getName());

        if (app != null) {
            // The name of a gamified application must be unique.
            throw new ConflictException("name");
        }

        app = new Application();
        app.setName(credentials.getName());
        app.setPassword(credentials.getPassword());

        applicationRepository.save(app);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Authorization", JWTUtils.generateToken(app.getName()))
                .build();
    }
}
