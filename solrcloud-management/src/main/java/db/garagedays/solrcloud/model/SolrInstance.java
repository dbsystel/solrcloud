package db.garagedays.solrcloud.model;

import org.apache.http.client.utils.URIBuilder;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 09.01.17.
 */
public class SolrInstance {

    private String host,name;

    public SolrInstance(String host, String name, CollectionAdminResponse response) {
        this.host = host;
        this.name = name;
    }

    public URI getBaseUrl() throws IOException {
        try {
            return new URIBuilder().setHost(host).setPath(name).build();
        } catch (URISyntaxException e) {
            throw new IOException("Cannot create collection URL",e);
        }
    }
}
