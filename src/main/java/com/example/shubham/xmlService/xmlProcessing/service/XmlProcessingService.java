package com.example.shubham.xmlService.xmlProcessing.service;

import com.example.shubham.xmlService.xmlProcessing.entity.XmlPayloadRecord;
import com.example.shubham.xmlService.xmlProcessing.repository.XmlPayloadRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class XmlProcessingService {

    private final XmlPayloadRepository repository;

    public XmlProcessingService(XmlPayloadRepository repository) {
        this.repository = repository;
    }

    public XmlPayloadRecord validateAndSaveXml(MultipartFile file) throws Exception {
        String xmlContent = new String(file.getBytes(), StandardCharsets.UTF_8);

        // 1. Validate XML against XSD
        validateAgainstXsd(xmlContent, "xsd/order.xsd");

        // 2. Save to SQL Server request_payload field
        XmlPayloadRecord record = new XmlPayloadRecord();
        record.setFileName(file.getOriginalFilename());
        record.setRequestPayload(xmlContent);
        record.setStatus("UPLOADED_AND_VALIDATED");

        return repository.save(record);
    }

    public XmlPayloadRecord processAndSaveResponse(Long id) throws Exception {
        // 1. Fetch record from DB
        XmlPayloadRecord record = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found with ID: " + id));

        // 2. Parse XML from request_payload
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(record.getRequestPayload())));

        // 3. Perform sample operation: uppercase CustomerName & append <ProcessedAt> tag
        Element root = doc.getDocumentElement();
        var customerNodes = root.getElementsByTagName("CustomerName");
        if (customerNodes.getLength() > 0) {
            String originalName = customerNodes.item(0).getTextContent();
            customerNodes.item(0).setTextContent(originalName.toUpperCase());
        }

        Element processedTag = doc.createElement("ProcessedAt");
        processedTag.setTextContent(LocalDateTime.now().toString());
        root.appendChild(processedTag);

        // 4. Convert modified DOM back to XML String and save to response_payload
        String responseXml = documentToString(doc);
        record.setResponsePayload(responseXml);
        record.setStatus("PROCESSED");

        return repository.save(record);
    }

    public String getRequestPayload(Long id) {
        XmlPayloadRecord record = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found with ID: " + id));

        if (record.getResponsePayload() == null || record.getResponsePayload().isBlank()) {
            throw new IllegalStateException("Response payload is not yet available. Process record ID " + id + " first.");
        }

        return record.getRequestPayload();
    }

    public String getResponsePayload(Long id) {
        XmlPayloadRecord record = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Record not found with ID: " + id));

        if (record.getResponsePayload() == null || record.getResponsePayload().isBlank()) {
            throw new IllegalStateException("Response payload is not yet available. Process record ID " + id + " first.");
        }

        return record.getResponsePayload();
    }

    private void validateAgainstXsd(String xmlContent, String xsdPath) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        ClassPathResource xsdResource = new ClassPathResource(xsdPath);
        Schema schema = factory.newSchema(xsdResource.getURL());
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new StringReader(xmlContent)));
    }

    private String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
}