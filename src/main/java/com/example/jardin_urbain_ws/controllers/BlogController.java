package com.example.jardin_urbain_ws.controllers;
import com.example.jardin_urbain_ws.Blog;
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
@RequestMapping(path = "blog", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class BlogController {

    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#";
    private final String RDF_FILE = "data/blog_data.rdf";

    public BlogController() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    @GetMapping()
    public String getBlogs() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/9naydel/ontologies/2024/9/untitled-ontology-10#>\n" +
                "SELECT ?Blog ?title ?date ?content\n" +
                "WHERE {\n" +
                "    ?Blog a ont:Blog .\n" +
                "    ?Blog ont:title ?title .\n" +
                "    ?Blog ont:date ?date .\n" +
                "    ?Blog ont:content ?content .\n" +
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

            transformedItem.put("uri", item.getJSONObject("Blog").getString("value"));
            transformedItem.put("title", item.getJSONObject("title").getString("value"));
            transformedItem.put("date", item.getJSONObject("date").getString("value"));
            transformedItem.put("content", item.getJSONObject("content").getString("value"));

            transformedArray.put(transformedItem);
        }
        return transformedArray.toString();
    }

    @PostMapping
    public ResponseEntity<String> addBlog(@RequestBody Blog blog) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Individual blogIndividual = ontModel.createIndividual(NAMESPACE + "Blog_" + System.currentTimeMillis(), ontModel.getOntClass(NAMESPACE + "Blog"));
        blogIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "title"), blog.getTitle());
        blogIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "date"), blog.getDate());
        blogIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "content"), blog.getContent());

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the blog.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Blog added successfully.");
    }

    @DeleteMapping()
    public ResponseEntity<String> deleteBlog(@RequestParam("URI") String blogURI) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Individual blogIndividual = ontModel.getIndividual(blogURI);

        if (blogIndividual != null) {
            blogIndividual.remove();

            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the blog.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Blog deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Blog not found.");
        }
    }
}
