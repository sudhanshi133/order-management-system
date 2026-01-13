package com.ecommerce.service;

public class NotificationService {
    /**
     * Simulates sending email - takes 200ms
     */
    public void sendEmail(String email, String subject, String body) {
        try {
            System.out.println("[Email] Sending to " + email + ": " + subject);
            Thread.sleep(200);
            System.out.println("[Email] Sent to " + email);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Simulates sending SMS - takes 150ms
     */
    public void sendSMS(String phone, String message) {
        try {
            System.out.println("[SMS] Sending to " + phone + "...");
            Thread.sleep(150);
            System.out.println("[SMS] Sent to " + phone);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

