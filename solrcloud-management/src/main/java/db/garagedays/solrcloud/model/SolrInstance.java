package db.garagedays.solrcloud.model;

import org.apache.solr.common.util.NamedList;

import java.net.URI;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 09.01.17.
 */
public class SolrInstance {

    public SolrInstance(NamedList<Object> solrCreateResponse) {

    }

    public URI getBaseUrl() {
        return URI.create("http://localhost:9090/");
    }
}
