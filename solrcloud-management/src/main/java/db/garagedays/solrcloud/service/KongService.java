package db.garagedays.solrcloud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 11.01.17.
 */
@Service
public class KongService {

    private Logger logger = LoggerFactory.getLogger(KongService.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Value("${kong.host:localhost:1234}")
    private String kongHost = "localhost:1234";

    public void registerCollectionEndpoint(String name) throws Exception {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(kongHost + "/apis");

        HashMap<String, Object> json = new HashMap<>();
        json.put("name", "search-" + name);
        json.put("request_host", "saas.poc.aws.db.de");
        json.put("request_path", "/solr/" + name);
        json.put("strip_request_path", false);
        json.put("preserve_host", false);
        json.put("upstream_url", "http://saas.poc.aws.db.de");

        try {
            StringEntity requestEntity = new StringEntity(
                    mapper.writeValueAsString(json),
                    ContentType.APPLICATION_JSON);

            post.setEntity(requestEntity);

            CloseableHttpResponse response = httpclient.execute(post);

            if(response.getStatusLine().getStatusCode() >= 400) {
                logger.warn("Error ({}): {}", response.getStatusLine().getStatusCode(), IOUtils.toString(response.getEntity().getContent()));
                throw new Exception("Cannot talk to Kong");
            }

            response.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Cannot talk to Kong",e);
        } finally {
            httpclient.close();
        }

    }

    public void activateKeyPlugin(String name) throws Exception {

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(kongHost + "/apis/search-"+name+"/plugins");

        HashMap<String, Object> json = new HashMap<>();
        json.put("name", "key-auth");

        try {
            StringEntity requestEntity = new StringEntity(
                    mapper.writeValueAsString(json),
                    ContentType.APPLICATION_JSON);

            post.setEntity(requestEntity);

            CloseableHttpResponse response = httpclient.execute(post);

            if(response.getStatusLine().getStatusCode() >= 400) {
                logger.warn("Error ({}): {}",response.getStatusLine().getStatusCode(), IOUtils.toString(response.getEntity().getContent()));
                throw new Exception("Cannot talk to Kong");
            }

            response.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Cannot talk to Kong",e);
        } finally {
            httpclient.close();
        }

    }

    public void createUser(String user) throws Exception {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(kongHost + "/consumers");

        HashMap<String, Object> json = new HashMap<>();
        json.put("username", user);

        try {
            StringEntity requestEntity = new StringEntity(
                    mapper.writeValueAsString(json),
                    ContentType.APPLICATION_JSON);

            post.setEntity(requestEntity);

            CloseableHttpResponse response = httpclient.execute(post);

            if(response.getStatusLine().getStatusCode() >= 400 && response.getStatusLine().getStatusCode() != 409) {
                logger.warn("Error ({}): {}",response.getStatusLine().getStatusCode(), IOUtils.toString(response.getEntity().getContent()));
                throw new Exception("Cannot create user");
            }

            response.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Cannot create user",e);
        } finally {
            httpclient.close();
        }
    }

    public String createApiKey(String user) throws Exception {

        String key = UUID.randomUUID().toString();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost post = new HttpPost(kongHost + "/consumers/"+user+"/key-auth/");

        HashMap<String, Object> json = new HashMap<>();
        json.put("key", key);

        try {

            StringEntity requestEntity = new StringEntity(
                    mapper.writeValueAsString(json),
                    ContentType.APPLICATION_JSON);

            post.setEntity(requestEntity);

            CloseableHttpResponse response = httpclient.execute(post);

            if(response.getStatusLine().getStatusCode() >= 400) {
                logger.warn("Error ({}): {}",response.getStatusLine().getStatusCode(), IOUtils.toString(response.getEntity().getContent()));
                throw new Exception("Cannot create user");
            }

            response.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Cannot create user",e);
        } finally {
            httpclient.close();
        }

        return key;
    }

}
