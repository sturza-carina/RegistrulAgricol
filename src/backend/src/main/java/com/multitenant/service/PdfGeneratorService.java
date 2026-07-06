package com.multitenant.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfGeneratorService {

    private final TemplateEngine templateEngine;

    @Autowired
    public PdfGeneratorService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generatePdfFromHtml(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process("pdf/" + templateName, context);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            
            // Register Roboto font to support Romanian diacritics
            builder.useFont(() -> PdfGeneratorService.class.getResourceAsStream("/fonts/Roboto-Regular.ttf"), 
                    "Roboto", 400, PdfRendererBuilder.FontStyle.NORMAL, true);
            builder.useFont(() -> PdfGeneratorService.class.getResourceAsStream("/fonts/Roboto-Bold.ttf"), 
                    "Roboto", 700, PdfRendererBuilder.FontStyle.NORMAL, true);

            // In a real app with external CSS or images, you need a base URI.
            // For simple inline CSS, we can just use a dummy base URI.
            builder.withHtmlContent(htmlContent, "classpath:/templates/pdf/");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF from template: " + templateName, e);
        }
    }
}
