package com.claropr.service;

import com.claropr.model.SalesContractPayload;
import com.claropr.service.ContractService;
import com.claropr.service.PrefillService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.BaseFont;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Service
public class ContractPdfService {

    @Autowired
    private ContractService contractService;
    
    @Autowired
    private PrefillService prefillService;

    public byte[] generateContractPdf(String contractUid, Map<String, String> signatures) throws DocumentException, IOException {
        try {
            // Obtener los datos del contrato por UID desde el sistema de ventas
            SalesContractPayload contractData = prefillService.getPrefill(contractUid);
            String customerSignature = signatures.get("customerSignature");
            String consultantSignature = signatures.get("consultantSignature");
            
            return generateContractPdf(contractData, customerSignature, consultantSignature);
        } catch (Exception e) {
            throw new IOException("Error generating contract PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generateContractPdf(SalesContractPayload contractData, String customerSignature, String consultantSignature) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();

        // Configurar fuente
        BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        Font titleFont = new Font(baseFont, 16, Font.BOLD, BaseColor.RED);
        Font headerFont = new Font(baseFont, 12, Font.BOLD, BaseColor.WHITE);
        Font normalFont = new Font(baseFont, 10, Font.NORMAL);
        Font boldFont = new Font(baseFont, 10, Font.BOLD);

        // Header con logo de Claro
        addHeader(document, titleFont, normalFont);
        
        // Título del contrato
        Paragraph title = new Paragraph("CONTRATO ÚNICO DE SERVICIO MÓVIL POSPAGO", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Información del Cliente
        addClientInformation(document, contractData, headerFont, normalFont, boldFont);
        
        // Equipos y Accesorios
        addEquipmentTable(document, contractData, headerFont, normalFont, boldFont);
        
        // Productos y Servicios
        addServicesSection(document, contractData, headerFont, normalFont, boldFont);
        
        // Totales
        addTotalsSection(document, contractData, headerFont, normalFont, boldFont);
        
        // Firmas
        addSignaturesSection(document, customerSignature, consultantSignature, contractData, headerFont, normalFont, boldFont);

        document.close();
        return outputStream.toByteArray();
    }

    private void addHeader(Document document, Font titleFont, Font normalFont) throws DocumentException {
        // Logo y información de la empresa
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{2, 1});
        
        // Columna izquierda - Información de la empresa
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPadding(5);
        
        Paragraph leftContent = new Paragraph();
        leftContent.add(new Chunk("R114 CENTRO ATENCION CLIENTE\n", normalFont));
        leftContent.add(new Chunk("PLAZA RIO HONDO 60 AVE. RIO HONDO, BAYAMON, PR, 00961", normalFont));
        leftCell.addElement(leftContent);
        headerTable.addCell(leftCell);
        
        // Columna derecha - Contacto
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPadding(5);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        Paragraph rightContent = new Paragraph();
        rightContent.add(new Chunk("787-775-0000\n", normalFont));
        rightContent.add(new Chunk("www.claropr.com", normalFont));
        rightCell.addElement(rightContent);
        headerTable.addCell(rightCell);
        
        document.add(headerTable);
        document.add(new Paragraph(" ")); // Espacio
    }

    private void addClientInformation(Document document, SalesContractPayload contractData, Font headerFont, Font normalFont, Font boldFont) throws DocumentException {
        // Header de sección
        PdfPTable headerTable = new PdfPTable(1);
        PdfPCell headerCell = new PdfPCell(new Phrase("Información del Cliente", headerFont));
        headerCell.setBackgroundColor(BaseColor.RED);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(8);
        headerTable.addCell(headerCell);
        document.add(headerTable);

        // Información del cliente - Layout como en el contrato original
        PdfPTable clientTable = new PdfPTable(2);
        clientTable.setWidthPercentage(100);
        clientTable.setWidths(new float[]{1, 2});

        addClientRow(clientTable, "Nombre Completo:", contractData.getCustomer().getFullName(), normalFont, boldFont);
        addClientRow(clientTable, "Tipo de ID:", contractData.getCustomer().getIdType(), normalFont, boldFont);
        addClientRow(clientTable, "Número de ID:", contractData.getCustomer().getIdNumber(), normalFont, boldFont);
        addClientRow(clientTable, "Email:", contractData.getCustomer().getEmail(), normalFont, boldFont);
        addClientRow(clientTable, "Teléfono:", contractData.getCustomer().getPhone(), normalFont, boldFont);
        addClientRow(clientTable, "Dirección:", formatAddress(contractData.getCustomer().getBillingAddress()), normalFont, boldFont);
        addClientRow(clientTable, "Fecha de Compra:", formatDate(contractData.getSaleInfo().getSaleDate()), normalFont, boldFont);
        addClientRow(clientTable, "Número de Cuenta:", contractData.getBan(), normalFont, boldFont);

        document.add(clientTable);
        document.add(new Paragraph(" "));
    }

    private void addClientRow(PdfPTable table, String label, String value, Font normalFont, Font boldFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont));
        labelCell.setPadding(5);
        labelCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addEquipmentTable(Document document, SalesContractPayload contractData, Font headerFont, Font normalFont, Font boldFont) throws DocumentException {
        // Header de sección
        PdfPTable headerTable = new PdfPTable(1);
        PdfPCell headerCell = new PdfPCell(new Phrase("Información de Equipos y Accesorios", headerFont));
        headerCell.setBackgroundColor(BaseColor.RED);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(8);
        headerTable.addCell(headerCell);
        document.add(headerTable);

        // Tabla de equipos - Layout como en el contrato original
        PdfPTable equipmentTable = new PdfPTable(7);
        equipmentTable.setWidthPercentage(100);
        equipmentTable.setWidths(new float[]{1, 1, 2, 2, 1, 1, 1});

        // Headers
        String[] headers = {"Código", "Cantidad", "Descripción", "Núm. Serie", "Precio", "Impuestos", "Pagos diferidos"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            equipmentTable.addCell(cell);
        }

        // Datos de equipos - Usar datos reales del contrato
        for (SalesContractPayload.Device device : contractData.getDevices()) {
            equipmentTable.addCell(new PdfPCell(new Phrase(device.getSku(), normalFont)));
            equipmentTable.addCell(new PdfPCell(new Phrase("1", normalFont)));
            equipmentTable.addCell(new PdfPCell(new Phrase(device.getDescription(), normalFont)));
            equipmentTable.addCell(new PdfPCell(new Phrase(device.getSerial(), normalFont)));
            equipmentTable.addCell(new PdfPCell(new Phrase("$" + device.getPrice(), normalFont)));
            equipmentTable.addCell(new PdfPCell(new Phrase("$" + device.getTax(), normalFont)));
            equipmentTable.addCell(new PdfPCell(new Phrase(String.valueOf(device.getFinancing().getInstallments()), normalFont)));
        }

        document.add(equipmentTable);
        document.add(new Paragraph(" "));
    }

    private void addServicesSection(Document document, SalesContractPayload contractData, Font headerFont, Font normalFont, Font boldFont) throws DocumentException {
        // Header de sección
        PdfPTable headerTable = new PdfPTable(1);
        PdfPCell headerCell = new PdfPCell(new Phrase("Productos y Servicios", headerFont));
        headerCell.setBackgroundColor(BaseColor.RED);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(8);
        headerTable.addCell(headerCell);
        document.add(headerTable);

        // Plan mensual primario - Usar datos reales del contrato
        Paragraph planTitle = new Paragraph("Plan Mensual Primario", boldFont);
        document.add(planTitle);
        
        Paragraph planDetails = new Paragraph();
        planDetails.add(new Chunk("Servicio: ", boldFont));
        planDetails.add(new Phrase(contractData.getPlanAndServices().getPlan().getName(), normalFont));
        planDetails.add(new Chunk("\nDescripción: ", boldFont));
        planDetails.add(new Phrase(String.join(", ", contractData.getPlanAndServices().getPlan().getFeatures()), normalFont));
        planDetails.add(new Chunk("\nPrecio: $", boldFont));
        planDetails.add(new Phrase(String.valueOf(contractData.getPlanAndServices().getPlan().getBasePrice()), normalFont));
        document.add(planDetails);

        // Servicios adicionales - Usar datos reales del contrato
        if (!contractData.getPlanAndServices().getAddons().isEmpty()) {
            Paragraph addonsTitle = new Paragraph("\nServicios Adicionales:", boldFont);
            document.add(addonsTitle);
            
            for (SalesContractPayload.PlanAndServices.Addon addon : contractData.getPlanAndServices().getAddons()) {
                Paragraph addonDetails = new Paragraph();
                addonDetails.add(new Chunk("• " + addon.getDescription() + ": $", normalFont));
                addonDetails.add(new Phrase(String.valueOf(addon.getPrice()), normalFont));
                document.add(addonDetails);
            }
        }

        document.add(new Paragraph(" "));
    }

    private void addTotalsSection(Document document, SalesContractPayload contractData, Font headerFont, Font normalFont, Font boldFont) throws DocumentException {
        // Mensualidad total aproximada - Header rojo
        PdfPTable totalHeaderTable = new PdfPTable(1);
        PdfPCell totalHeaderCell = new PdfPCell(new Phrase("Mensualidad total aproximada", headerFont));
        totalHeaderCell.setBackgroundColor(BaseColor.RED);
        totalHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalHeaderCell.setPadding(8);
        totalHeaderTable.addCell(totalHeaderCell);
        document.add(totalHeaderTable);
        
        // Total en verde - Usar datos reales del contrato
        try {
            Paragraph totalAmount = new Paragraph("$" + contractData.getTotals().getEstimatedMonthly(), new Font(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED), 16, Font.BOLD, BaseColor.GREEN));
            totalAmount.setAlignment(Element.ALIGN_CENTER);
            document.add(totalAmount);
        } catch (IOException e) {
            // Fallback to default font
            Paragraph totalAmount = new Paragraph("$" + contractData.getTotals().getEstimatedMonthly(), new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.GREEN));
            totalAmount.setAlignment(Element.ALIGN_CENTER);
            document.add(totalAmount);
        }

        // Información estimada de próxima factura
        Paragraph nextBillTitle = new Paragraph("\nInformación Estimada de su Próxima Factura", boldFont);
        document.add(nextBillTitle);

        PdfPTable billTable = new PdfPTable(2);
        billTable.setWidthPercentage(100);
        billTable.setWidths(new float[]{2, 1});

        // Usar datos reales del contrato
        addBillRow(billTable, "Cargo prorrateo:", "$" + contractData.getTotals().getNextBillEstimate().getProration(), normalFont, boldFont);
        addBillRow(billTable, "Cargos mes por adelantado:", "$" + contractData.getPlanAndServices().getPlan().getBasePrice(), normalFont, boldFont);
        addBillRow(billTable, "Cargo equipo:", "$" + contractData.getTotals().getNextBillEstimate().getDeviceCharge(), normalFont, boldFont);
        addBillRow(billTable, "Otros cargos/créditos:", "($" + contractData.getTotals().getNextBillEstimate().getOtherChargesCredits().abs() + ")", normalFont, boldFont);
        
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("Estimado Total:", boldFont));
        totalLabelCell.setPadding(5);
        totalLabelCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        billTable.addCell(totalLabelCell);

        PdfPCell totalValueCell = new PdfPCell(new Phrase("$" + contractData.getTotals().getNextBillEstimate().getEstimatedTotal(), boldFont));
        totalValueCell.setPadding(5);
        totalValueCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        billTable.addCell(totalValueCell);

        document.add(billTable);
        document.add(new Paragraph(" "));
    }

    private void addBillRow(PdfPTable table, String label, String value, Font normalFont, Font boldFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, normalFont));
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private void addSignaturesSection(Document document, String customerSignature, String consultantSignature, SalesContractPayload contractData, Font headerFont, Font normalFont, Font boldFont) throws DocumentException {
        // Espacio para firmas
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));

        // Firma del cliente
        Paragraph clientSignatureTitle = new Paragraph("Firma autorizada cliente:", boldFont);
        document.add(clientSignatureTitle);
        
        if (customerSignature != null && !customerSignature.isEmpty()) {
            try {
                // Procesar la firma digital (base64)
                Image signatureImage = processSignatureImage(customerSignature);
                signatureImage.setAlignment(Element.ALIGN_CENTER);
                signatureImage.scaleToFit(200, 100);
                document.add(signatureImage);
            } catch (Exception e) {
                // Fallback si no se puede procesar la imagen
                Paragraph signaturePlaceholder = new Paragraph("[Firma Digital del Cliente]", normalFont);
                signaturePlaceholder.setAlignment(Element.ALIGN_CENTER);
                document.add(signaturePlaceholder);
            }
        } else {
            Paragraph signatureLine = new Paragraph("_________________________", normalFont);
            signatureLine.setAlignment(Element.ALIGN_CENTER);
            document.add(signatureLine);
        }
        
        Paragraph clientName = new Paragraph(contractData.getCustomer().getFullName(), normalFont);
        clientName.setAlignment(Element.ALIGN_CENTER);
        document.add(clientName);
        
        Paragraph clientDate = new Paragraph("Fecha/Hora: " + new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date()), normalFont);
        clientDate.setAlignment(Element.ALIGN_CENTER);
        document.add(clientDate);

        document.add(new Paragraph(" "));

        // Firma del consultor
        Paragraph consultantSignatureTitle = new Paragraph("Firma Consultor", boldFont);
        document.add(consultantSignatureTitle);
        
        if (consultantSignature != null && !consultantSignature.isEmpty()) {
            try {
                // Procesar la firma digital (base64)
                Image signatureImage = processSignatureImage(consultantSignature);
                signatureImage.setAlignment(Element.ALIGN_CENTER);
                signatureImage.scaleToFit(200, 100);
                document.add(signatureImage);
            } catch (Exception e) {
                // Fallback si no se puede procesar la imagen
                Paragraph signaturePlaceholder = new Paragraph("[Firma Digital del Consultor]", normalFont);
                signaturePlaceholder.setAlignment(Element.ALIGN_CENTER);
                document.add(signaturePlaceholder);
            }
        } else {
            Paragraph signatureLine = new Paragraph("_________________________", normalFont);
            signatureLine.setAlignment(Element.ALIGN_CENTER);
            document.add(signatureLine);
        }
        
        Paragraph consultantName = new Paragraph(contractData.getSaleInfo().getSeller().getName(), normalFont);
        consultantName.setAlignment(Element.ALIGN_CENTER);
        document.add(consultantName);
        
        Paragraph consultantId = new Paragraph("Comp Id. Consultor: " + contractData.getSaleInfo().getSeller().getEmployeeId(), normalFont);
        consultantId.setAlignment(Element.ALIGN_CENTER);
        document.add(consultantId);
    }

    private Image processSignatureImage(String signatureBase64) throws Exception {
        // Remover el prefijo data:image/png;base64, si existe
        String base64Data = signatureBase64;
        if (signatureBase64.contains(",")) {
            base64Data = signatureBase64.split(",")[1];
        }
        
        // Decodificar base64 a bytes
        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
        
        // Crear imagen desde bytes
        return Image.getInstance(imageBytes);
    }

    private String formatAddress(SalesContractPayload.Customer.Address address) {
        return address.getLine1() + ", " + address.getCity() + ", " + address.getState() + " " + address.getZip();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy");
            return outputFormat.format(inputFormat.parse(dateString));
        } catch (Exception e) {
            return dateString;
        }
    }
}
