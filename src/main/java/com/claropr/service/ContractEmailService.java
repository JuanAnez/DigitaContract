package com.claropr.service;

import com.claropr.model.SalesContractPayload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
public class ContractEmailService {

    @Value("${email.from:noreply@claro.pr}")
    private String fromEmail;

    @Value("${email.from.name:Claro Puerto Rico}")
    private String fromName;

    private final JavaMailSender mailSender;

    public ContractEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendContractEmail(SalesContractPayload contractData, byte[] pdfBytes, String customerSignature, String consultantSignature) 
            throws MessagingException, UnsupportedEncodingException {
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Configurar remitente
        helper.setFrom(fromEmail, fromName);
        
        // Configurar destinatario
        helper.setTo(contractData.getCustomer().getEmail());
        
        // Configurar asunto
        String subject = "Contrato de Servicio Móvil - " + contractData.getContractUid();
        helper.setSubject(subject);

        // Configurar contenido del email
        String emailContent = buildEmailContent(contractData);
        helper.setText(emailContent, true);

        // Adjuntar PDF
        String fileName = "Contrato_" + contractData.getContractUid() + ".pdf";
        helper.addAttachment(fileName, new javax.mail.util.ByteArrayDataSource(pdfBytes, "application/pdf"));

        // Enviar email
        mailSender.send(message);
    }

    private String buildEmailContent(SalesContractPayload contractData) {
        StringBuilder content = new StringBuilder();
        
        content.append("<html><body>");
        content.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        
        // Header
        content.append("<div style='background-color: #E60012; color: white; padding: 20px; text-align: center;'>");
        content.append("<h1 style='margin: 0;'>Claro Puerto Rico</h1>");
        content.append("</div>");
        
        // Contenido principal
        content.append("<div style='padding: 20px;'>");
        content.append("<h2>Estimado/a ").append(contractData.getCustomer().getFullName()).append(",</h2>");
        
        content.append("<p>Le enviamos adjunto su contrato de servicio móvil pospago con los siguientes detalles:</p>");
        
        content.append("<div style='background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;'>");
        content.append("<h3>Resumen del Contrato</h3>");
        content.append("<p><strong>Número de Contrato:</strong> ").append(contractData.getContractUid()).append("</p>");
        content.append("<p><strong>Número de Cuenta:</strong> ").append(contractData.getBan()).append("</p>");
        content.append("<p><strong>Plan:</strong> ").append(contractData.getPlanAndServices().getPlan().getName()).append("</p>");
        content.append("<p><strong>Mensualidad:</strong> $").append(contractData.getTotals().getEstimatedMonthly()).append("</p>");
        content.append("<p><strong>Fecha de Activación:</strong> ").append(formatDate(contractData.getSaleInfo().getSaleDate())).append("</p>");
        content.append("</div>");
        
        content.append("<p>El contrato ha sido firmado digitalmente y está listo para su uso.</p>");
        
        content.append("<p>Si tiene alguna pregunta, no dude en contactarnos al 787-775-0000 o visitar www.claropr.com</p>");
        
        content.append("<p>Gracias por elegir Claro Puerto Rico.</p>");
        
        content.append("<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #ccc;'>");
        content.append("<p style='font-size: 12px; color: #666;'>");
        content.append("Este es un mensaje automático, por favor no responda a este email.<br>");
        content.append("Claro Puerto Rico - R114 Centro Atención Cliente<br>");
        content.append("Plaza Rio Hondo 60 Ave. Rio Hondo, Bayamon, PR 00961");
        content.append("</p>");
        content.append("</div>");
        
        content.append("</div>");
        content.append("</div>");
        content.append("</body></html>");
        
        return content.toString();
    }

    private String formatDate(String dateString) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MM/dd/yyyy");
            return outputFormat.format(inputFormat.parse(dateString));
        } catch (Exception e) {
            return dateString;
        }
    }
}



