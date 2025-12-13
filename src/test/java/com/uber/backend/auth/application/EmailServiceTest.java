package com.uber.backend.auth.application;

import com.uber.backend.auth.application.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test suite for EmailService.
 * Tests email sending functionality with verification codes.
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Default behavior: mail sending succeeds
    }

    @Test
    void givenValidEmailAndCode_whenSendVerificationEmail_thenSuccess() {
        // Arrange
        String recipientEmail = "test@example.com";
        String verificationCode = "123456";

        // Act
        emailService.sendVerificationEmail(recipientEmail, verificationCode);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        assertEquals(recipientEmail, capturedMessage.getTo()[0]);
        assertEquals("Verify Your Email - MJT Rides", capturedMessage.getSubject());
        assertNotNull(capturedMessage.getText());
        assertTrue(capturedMessage.getText().contains(verificationCode));
        assertTrue(capturedMessage.getText().contains("MJT Rides"));
        assertTrue(capturedMessage.getText().contains("15 minutes"));
    }

    @Test
    void givenMultipleCodes_whenSendVerificationEmail_thenEachMessageContainsCorrectCode() {
        // Arrange
        String email = "user@example.com";
        String code1 = "111111";
        String code2 = "222222";

        // Act
        emailService.sendVerificationEmail(email, code1);
        emailService.sendVerificationEmail(email, code2);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());

        var messages = messageCaptor.getAllValues();
        assertTrue(messages.get(0).getText().contains(code1));
        assertTrue(messages.get(1).getText().contains(code2));
    }

    @Test
    void givenMailSenderThrowsException_whenSendVerificationEmail_thenRuntimeExceptionThrown() {
        // Arrange
        String recipientEmail = "test@example.com";
        String verificationCode = "123456";

        doThrow(new RuntimeException("SMTP server unavailable"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> emailService.sendVerificationEmail(recipientEmail, verificationCode)
        );

        assertEquals("Failed to send verification email", exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    void givenValidCode_whenSendVerificationEmail_thenMessageContainsExpectedContent() {
        // Arrange
        String recipientEmail = "test@example.com";
        String verificationCode = "987654";

        // Act
        emailService.sendVerificationEmail(recipientEmail, verificationCode);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        String emailText = message.getText();

        // Check for key content
        assertTrue(emailText.contains("Welcome to MJT Rides"));
        assertTrue(emailText.contains("verify your email address"));
        assertTrue(emailText.contains("Your verification code is: " + verificationCode));
        assertTrue(emailText.contains("This code will expire in 15 minutes"));
        assertTrue(emailText.contains("The MJT Rides Team"));
    }

    @Test
    void givenDifferentEmails_whenSendVerificationEmail_thenEachSentToCorrectRecipient() {
        // Arrange
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";
        String code = "123456";

        // Act
        emailService.sendVerificationEmail(email1, code);
        emailService.sendVerificationEmail(email2, code);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(messageCaptor.capture());

        var messages = messageCaptor.getAllValues();
        assertEquals(email1, messages.get(0).getTo()[0]);
        assertEquals(email2, messages.get(1).getTo()[0]);
    }
}
