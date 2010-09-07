package de.uka.iti.pseudo.proof.serialisation;

import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import junit.framework.TestCase;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

public class TestValidXSD extends TestCase {

    // test whether the proof xsd is a valid xsd itself.
    public void testValidXSD() throws Exception {
        
        InputStream is = getClass().getResourceAsStream("proof.xsd");
        assertNotNull("proof.xsd not found", is);
        
        InputStream xsdStream = getClass().getResourceAsStream("XMLSchema.xsd");
        assertNotNull("XMLSchema.xsd not found", xsdStream);
        
        Source[] xsdSources = { new StreamSource(xsdStream) };
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        SchemaFactory schemaFactory = 
            SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

        factory.setSchema(schemaFactory.newSchema(xsdSources));

        SAXParser parser = factory.newSAXParser();
        
        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new ErrHandler());
        reader.parse(new InputSource(is));
    }
    
    private static class ErrHandler implements ErrorHandler {

        @Override
        public void error(SAXParseException exception) throws SAXException {
            exception.printStackTrace();
            fail("Parser error: " + exception);
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            exception.printStackTrace();
            fail("Parser fatal: " + exception);
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            exception.printStackTrace();
            fail("Parser warning: " + exception);
        }};
    
}
