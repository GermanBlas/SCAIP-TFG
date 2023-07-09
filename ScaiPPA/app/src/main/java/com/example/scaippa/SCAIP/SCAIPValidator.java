package com.example.scaippa.SCAIP;

import org.w3c.dom.Document;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class SCAIPValidator {


    /**
     * Validation method.
     * Base code/example from: http://docs.oracle.com/javase/1.5.0/docs/api/javax/xml/validation/package-summary.html
     *
     * @param xmlFilePath       The xml file we are trying to validate.
     * @param xmlSchemaFilePath The schema file we are using for the validation. This method assumes the schema file is valid.
     * @return True if valid, false if not valid or bad parse.
     */
    public static boolean validate(String xmlFilePath, String xmlSchemaFilePath) {

        // parse an XML document into a DOM tree
        DocumentBuilder parser = null;
        Document document;

        // Try the validation, we assume that if there are any issues with the validation
        // process that the input is invalid.
        try {
            // validate the DOM tree
            parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = parser.parse(new File(xmlFilePath));

            // create a SchemaFactory capable of understanding WXS schemas
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // load a WXS schema, represented by a Schema instance
            Source schemaFile = new StreamSource(new File(xmlSchemaFilePath));
            Schema schema = factory.newSchema(schemaFile);

            // create a Validator instance, which can be used to validate an instance document
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(document));
        } catch (Exception e) {
            // Catches: SAXException, ParserConfigurationException, and IOException.
            return false;
        }

        return true;
    }

}
