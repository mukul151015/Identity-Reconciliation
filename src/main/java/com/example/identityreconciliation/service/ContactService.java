package com.example.identityreconciliation.service;

import com.example.identityreconciliation.dto.ConsolidatedContactResponse;
import com.example.identityreconciliation.dto.ContactInfo;
import com.example.identityreconciliation.dto.IdentifyRequest;
import com.example.identityreconciliation.model.Contact;
import com.example.identityreconciliation.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Transactional
    public ConsolidatedContactResponse identifyContact(IdentifyRequest request) {
        if (request.getEmail() == null && request.getPhoneNumber() == null) {
            throw new IllegalArgumentException("Either email or phoneNumber must be provided.");
        }

        Optional<Contact> primaryContactOptional = contactRepository.findByEmailOrPhoneNumber(request.getEmail(), request.getPhoneNumber());

        Contact primaryContact;
        if (primaryContactOptional.isPresent()) {
            primaryContact = primaryContactOptional.get();
        } else {
            primaryContact = createNewPrimaryContact(request.getEmail(), request.getPhoneNumber());
        }

        return constructConsolidatedContact(primaryContact);
    }

    private ConsolidatedContactResponse constructConsolidatedContact(Contact contact) {
        ConsolidatedContactResponse response = new ConsolidatedContactResponse();
        response.setContact(constructContactInfo(contact));
        return response;
    }

    private ContactInfo constructContactInfo(Contact contact) {
        ContactInfo contactInfo = new ContactInfo();
        contactInfo.setPrimaryContactId(contact.getId());
        contactInfo.setEmails(new ArrayList<>());
        contactInfo.setPhoneNumbers(new ArrayList<>());

        List<Contact> linkedContacts = contactRepository.findByLinkedContact(contact);
        for (Contact linkedContact : linkedContacts) {
            if (linkedContact.getEmail() != null) {
                contactInfo.getEmails().add(linkedContact.getEmail());
            }
            if (linkedContact.getPhoneNumber() != null) {
                contactInfo.getPhoneNumbers().add(linkedContact.getPhoneNumber());
            }
        }

        return contactInfo;
    }
    private Contact createNewPrimaryContact(String email, String phoneNumber) {
        Contact newPrimaryContact = new Contact();
        newPrimaryContact.setEmail(email);
        newPrimaryContact.setPhoneNumber(phoneNumber);
        newPrimaryContact.setLinkPrecedence("primary");

        final Contact finalNewPrimaryContact = contactRepository.save(newPrimaryContact);

        Optional<Contact> existingContactOptional = contactRepository.findByEmailOrPhoneNumber(email, phoneNumber);

        existingContactOptional.ifPresent(existingContact -> {
            existingContact.setLinkedContact(finalNewPrimaryContact);
            contactRepository.save(existingContact);
        });

        return finalNewPrimaryContact;
    }

}
