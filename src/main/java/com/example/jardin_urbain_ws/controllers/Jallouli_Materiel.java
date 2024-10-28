package com.example.jardin_urbain_ws.controllers;

import com.example.jardin_urbain_ws.Materiel;
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
@RequestMapping(path = "materiel", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class Jallouli_Materiel {

    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#";
    private final String RDF_FILE = "data/sementique_finale.rdf"; // Adjust path as needed

    public Jallouli_Materiel() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    @GetMapping()
    public String getMateriels() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" +
                "SELECT ?materiel ?name ?type ?quantity\n" +
                "WHERE {\n" +
                "    ?materiel a ont:Materiel .\n" +
                "    ?materiel ont:name ?name .\n" +
                "    ?materiel ont:type ?type .\n" +
                "    ?materiel ont:quantity ?quantity .\n" +
                "}";

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);

        return new String(outputStream.toByteArray());
    }

    @PostMapping
    public ResponseEntity<String> addMateriel(@RequestBody Materiel materiel) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual materielIndividual = ontModel.createIndividual(NAMESPACE + "Materiel_" + System.currentTimeMillis(),
                ontModel.getOntClass(NAMESPACE + "Materiel"));
        materielIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "name"), materiel.getName());
        materielIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "type"), materiel.getType());
        materielIndividual.addLiteral(ontModel.getDatatypeProperty(NAMESPACE + "quantity"), materiel.getQuantity());

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the material.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Material added successfully.");
    }

    @PutMapping
    public ResponseEntity<String> updateMateriel(@RequestParam("URI") String materielURI, @RequestBody Materiel updatedMateriel) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual materielIndividual = ontModel.getIndividual(materielURI);

        if (materielIndividual != null) {
            // Update properties
            materielIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "name"));
            materielIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "name"), updatedMateriel.getName());

            materielIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "type"));
            materielIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "type"), updatedMateriel.getType());

            materielIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "quantity"));
            materielIndividual.addLiteral(ontModel.getDatatypeProperty(NAMESPACE + "quantity"), updatedMateriel.getQuantity());

            // Save changes to RDF file
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update the material.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Material updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Material not found.");
        }
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteMateriel(@RequestParam("URI") String materielURI) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual materielIndividual = ontModel.getIndividual(materielURI);

        if (materielIndividual != null) {
            materielIndividual.remove();

            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the material.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Material deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Material not found.");
        }
    }
}
