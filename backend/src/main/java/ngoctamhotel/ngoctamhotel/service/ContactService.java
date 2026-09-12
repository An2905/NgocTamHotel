package ngoctamhotel.ngoctamhotel.service;

import org.springframework.stereotype.Service;

import ngoctamhotel.ngoctamhotel.dto.request.ContactRequest;
import ngoctamhotel.ngoctamhotel.model.ContactMessage;
import ngoctamhotel.ngoctamhotel.repository.ContactMessageRepository;

@Service
public class ContactService {
    private final ContactMessageRepository contactMessageRepository;

    public ContactService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    public ContactMessage submitContact(ContactRequest request) {
        return contactMessageRepository.create(
                request.fullName().trim(),
                request.email().trim().toLowerCase(),
                request.phone(),
                request.subject(),
                request.message().trim());
    }
}
