package com.nikhilnishad.househunt.service;

import com.nikhilnishad.househunt.model.EmailDetails;

public interface EmailService {
 
    String sendSimpleMail(EmailDetails details);

    String sendMailWithAttachment(EmailDetails details);
}