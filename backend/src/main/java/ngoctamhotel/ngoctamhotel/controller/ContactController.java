package ngoctamhotel.ngoctamhotel.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import ngoctamhotel.ngoctamhotel.dto.request.ContactRequest;
import ngoctamhotel.ngoctamhotel.service.ContactService;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> submitContact(@Valid @RequestBody ContactRequest request) {
        contactService.submitContact(request);
        return Map.of("message", "Gửi liên hệ thành công! Chúng tôi sẽ phản hồi sớm nhất.");
    }
}
