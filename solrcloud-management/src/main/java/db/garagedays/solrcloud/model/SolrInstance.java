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

    private String host,name,type,key;

    public SolrInstance(String host, String name, String type, CollectionAdminResponse response,String key) {
        this.host = host;
        this.name = name;
        this.type = type;
        this.key = key;
    }

    public URI getUrl() throws IOException {
        try {
            return new URI(host + '/' + name);//TODO
        } catch (URISyntaxException e) {
            throw new IOException("Cannot create collection URL",e);
        }
    }

    public String getHost() {
        return host;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }
}
