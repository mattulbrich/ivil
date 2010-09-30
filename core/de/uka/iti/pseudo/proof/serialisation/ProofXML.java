/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof.serialisation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.RuleApplicationMaker;

/**
 * This class can be used to save and load {@link Proof}s to XML documents.
 * 
 * It implements both the import and the export interface.
 * 
 * Import is however handled by {@link SAXHandler} which constructs
 * {@link RuleApplicationMaker} from the XML content.
 * 
 * Export is handled using a {@link XMLWriter}, a very simple XML export
 * facility.
 */

public class ProofXML implements ProofImport, ProofExport {
    
    public boolean acceptsInput(InputStream is)  throws IOException {
        return true;
    }

    public void importProof(InputStream is, Proof proof, Environment env)  throws IOException, ProofException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            factory.setSchema(makeSchema());
            SAXParser parser = factory.newSAXParser();
            SAXHandler handler = new SAXHandler(env, proof, true);
            parser.parse(is, handler);
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }
    
    private Schema makeSchema() throws IOException, SAXException {
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance("http://www.w3.org/2001/XMLSchema");
        InputStream is = getClass().getResourceAsStream("proof.xsd");
        if(is == null)
            throw new FileNotFoundException("XSD resource proof.xsd not available");
        
        Schema schema = schemaFactory
                .newSchema(new Source[] { new StreamSource(is) });
        
        return schema;
    }

    public void exportProof(OutputStream os, Proof proof, Environment env)
            throws IOException {
        
        OutputStreamWriter writer = new OutputStreamWriter(os);
        
        XMLOutput out = new XMLOutput(writer);
        out.export(proof);
        
        writer.flush();
    }

    public String getFileExtension() {
        return "pxml";
    }

    public String getName() {
        return "PXML -- proof XML file";
    }

}

