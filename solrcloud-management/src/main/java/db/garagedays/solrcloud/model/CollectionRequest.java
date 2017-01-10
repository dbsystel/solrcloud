package db.garagedays.solrcloud.model;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 10.01.17.
 */
public class CollectionRequest {

    private
    MultipartFile file;
    private String name;
    private String email;
    private String collection;
    private Integer docs;
    private Integer docsize;
    private Integer updates;
    private Integer queries;
    private Integer configtype;

    public CollectionRequest(MultipartFile file, String name, String email, String collection, Integer docs, Integer docsize, Integer updates, Integer queries, Integer configtype) {
        this.file = file;
        this.name = name;
        this.email = email;
        this.collection = collection;
        this.docs = docs;
        this.docsize = docsize;
        this.updates = updates;
        this.queries = queries;
        this.configtype = configtype;
    }
}
