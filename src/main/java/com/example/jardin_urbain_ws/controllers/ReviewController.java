package com.example.jardin_urbain_ws.controllers;

import com.example.jardin_urbain_ws.Review;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping(path = "review", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class ReviewController {

    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#";
    private final String RDF_FILE = "data/sementique_finale.rdf";

    public ReviewController() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    @GetMapping()
    public String getReviews() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" +
                "SELECT ?Review ?user ?content ?date\n" +
                "WHERE {\n" +
                "    ?Review a ont:Review .\n" +
                "    ?Review ont:user ?user .\n" +
                "    ?Review ont:content ?content .\n" +
                "    ?Review ont:date ?date .\n" +
                "}";

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ResultSetFormatter.outputAsJSON(outputStream, results);
        String json = new String(outputStream.toByteArray());
        JSONObject j = new JSONObject(json);

        JSONArray jsonArray = j.getJSONObject("results").getJSONArray("bindings");
        JSONArray transformedArray = new JSONArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            JSONObject transformedItem = new JSONObject();

            transformedItem.put("uri", item.getJSONObject("Review").getString("value"));
            transformedItem.put("user", item.getJSONObject("user").getString("value"));
            transformedItem.put("content", item.getJSONObject("content").getString("value"));
            transformedItem.put("date", item.getJSONObject("date").getString("value"));

            transformedArray.put(transformedItem);
        }
        return transformedArray.toString();
    }

    @PostMapping
    public ResponseEntity<String> addReview(@RequestBody Review review) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Individual reviewIndividual = ontModel.createIndividual(NAMESPACE + "Review_" + System.currentTimeMillis(), ontModel.getOntClass(NAMESPACE + "Review"));
        reviewIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "user"), review.getUser());
        reviewIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "content"), review.getContent());
        reviewIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "date"), review.getDate());

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the review.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Review added successfully.");
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteReview(@RequestParam("URI") String reviewURI) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Individual reviewIndividual = ontModel.getIndividual(reviewURI);

        if (reviewIndividual != null) {
            reviewIndividual.remove();

            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the review.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Review deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Review not found.");
        }
    }

    @PutMapping
    public ResponseEntity<String> addReviewToBlog(@RequestParam("reviewURI") String reviewURI, @RequestParam("blogURI") String blogURI) {

        // Create an ontology model for inference
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        // Retrieve the blog resource
        Resource blogResource = model.getResource(blogURI);

        // Define the property for associating a review with a blog
        Property ReceivesProperty = model.createProperty(NAMESPACE + "receives");

        // Remove any existing `hasReview` property from the blog to avoid duplication
        blogResource.removeAll(ReceivesProperty);

        // Add the `hasReview` property to link the blog with the review URI
        blogResource.addProperty(ReceivesProperty, model.createResource(reviewURI));

        // Save the updated model back to RDF
        try (FileOutputStream out = new FileOutputStream(RDF_FILE)) {
            ontModel.write(out, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving updated RDF model.");
        }

        return ResponseEntity.ok("Review added to blog successfully.");
    }

}
