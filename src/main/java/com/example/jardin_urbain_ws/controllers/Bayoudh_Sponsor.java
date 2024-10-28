package com.example.jardin_urbain_ws.controllers;

import com.example.jardin_urbain_ws.Sponsor;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping(path = "sponsor", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class Bayoudh_Sponsor {

    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#";
    private final String RDF_FILE = "data/sementique_finale.rdf"; // Adjust path as needed

    public Bayoudh_Sponsor() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    @GetMapping()
    public String getSponsors() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" +
                "SELECT ?sponsor ?name ?industry ?phone\n" +
                "WHERE {\n" +
                "    ?sponsor a ont:Sponsor .\n" +
                "    ?sponsor ont:name ?name .\n" +
                "    ?sponsor ont:industry ?industry .\n" +
                "    ?sponsor ont:phone ?phone .\n" +
                "}";

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);

        return new String(outputStream.toByteArray());
    }

    @PostMapping
    public ResponseEntity<String> addSponsor(@RequestBody Sponsor sponsor) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual sponsorIndividual = ontModel.createIndividual(NAMESPACE + "Sponsor_" + System.currentTimeMillis(),
                ontModel.getOntClass(NAMESPACE + "Sponsor"));
        sponsorIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "name"), sponsor.getName());
        sponsorIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "industry"), sponsor.getIndustry());
        sponsorIndividual.addLiteral(ontModel.getDatatypeProperty(NAMESPACE + "phone"), sponsor.getPhone());

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the sponsor.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Sponsor added successfully.");
    }

    @PutMapping
    public ResponseEntity<String> updateSponsor(@RequestParam("URI") String sponsorURI, @RequestBody Sponsor updatedSponsor) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual sponsorIndividual = ontModel.getIndividual(sponsorURI);

        if (sponsorIndividual != null) {
            // Update properties
            sponsorIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "name"));
            sponsorIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "name"), updatedSponsor.getName());

            sponsorIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "industry"));
            sponsorIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "industry"), updatedSponsor.getIndustry());

            sponsorIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "phone"));
            sponsorIndividual.addLiteral(ontModel.getDatatypeProperty(NAMESPACE + "phone"), updatedSponsor.getPhone());

            // Save changes to RDF file
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update the sponsor.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Sponsor updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sponsor not found.");
        }
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteSponsor(@RequestParam("URI") String sponsorURI) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual sponsorIndividual = ontModel.getIndividual(sponsorURI);

        if (sponsorIndividual != null) {
            sponsorIndividual.remove();

            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the sponsor.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Sponsor deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sponsor not found.");
        }
    }
}
