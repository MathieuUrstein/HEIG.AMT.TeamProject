package ch.heigvd.gamification.web.api;

import ch.heigvd.gamification.dao.ApplicationRepository;
import ch.heigvd.gamification.dao.BadgeRepository;
import ch.heigvd.gamification.dto.BadgeDTO;
import ch.heigvd.gamification.exception.ConflictException;
import ch.heigvd.gamification.exception.NotFoundException;
import ch.heigvd.gamification.model.Application;
import ch.heigvd.gamification.model.Badge;
import ch.heigvd.gamification.util.URIs;
import ch.heigvd.gamification.validator.BadgeDTOValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(URIs.BADGES)
public class BadgesEndpoint {
    // TODO : endpoint for images (badges)

    private final BadgeRepository badgeRepository;
    private final ApplicationRepository applicationRepository;

    @Autowired
    public BadgesEndpoint(BadgeRepository badgeRepository, ApplicationRepository applicationRepository) {
        this.badgeRepository = badgeRepository;
        this.applicationRepository = applicationRepository;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setValidator(new BadgeDTOValidator());
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<BadgeDTO> getBadges(@RequestAttribute("application") Application app) {
        return badgeRepository.findByApplicationName(app.getName())
                .stream()
                .map(this::toBadgeDTO)
                .collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{badgeId}")
    public BadgeDTO getBadge(@RequestAttribute("application") Application app, @PathVariable long badgeId) {
        Badge badge = badgeRepository
                .findByApplicationNameAndId(app.getName(), badgeId)
                .orElseThrow(NotFoundException::new);

        return toBadgeDTO(badge);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity addBadge(@Valid @RequestBody BadgeDTO badgeDTO, @RequestAttribute("application") Application application) {
        // TODO : image with a url

        try {
            Application app = applicationRepository.findByName(application.getName());
            Badge badge = new Badge();

            badge.setName(badgeDTO.getName());
            badge.setImage(badgeDTO.getImage());
            badge.setApplication(app);
            app.addBadge(badge);

            badgeRepository.save(badge);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().path("/{id}")
                    .buildAndExpand(badge.getId()).toUri();

            return ResponseEntity.created(location).build();
        }
        catch (DataIntegrityViolationException e) {
            // The name of a badge must be unique in a gamified application.
            throw new ConflictException("name");
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{badgeId}")
    public ResponseEntity deleteBadge(@RequestAttribute("application") Application app, @PathVariable long badgeId) {
        Badge badge = badgeRepository
                .findByApplicationNameAndId(app.getName(), badgeId)
                .orElseThrow(NotFoundException::new);

        badgeRepository.delete(badge);

        return ResponseEntity.ok().build();
    }

    private BadgeDTO toBadgeDTO(Badge badge) {
        return new BadgeDTO(badge.getName(), badge.getImage());
    }
}
