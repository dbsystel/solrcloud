package db.garagedays.solrcloud.service;

import db.garagedays.solrcloud.model.ConfigClass;
import db.garagedays.solrcloud.model.ConfigurationClassProperties;
import db.garagedays.solrcloud.model.SolrInstance;
import db.garagedays.solrcloud.exception.InstanceInitException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 09.01.17.
 */
@Service
public class SolrCloudService {

    @Value("${zk.host:localhost:9983}")
    private final String zkHost = "localhost:9983";

    private Logger logger = LoggerFactory.getLogger(SolrCloudService.class);

    private ConfigurationClassProperties properties;

    @Autowired
    public SolrCloudService(ConfigurationClassProperties properties) {
        this.properties = properties;
    }

    public SolrInstance createCollection(String collectionName, String configName, String configclass) throws InstanceInitException {
        try (final CloudSolrClient client = createSolrClient()) {
            return createCollection(client, collectionName, configName, configclass);
        } catch (IOException | SolrServerException e) {
            throw new InstanceInitException("Cannot initialize collection", e);
        }
    }

    public SolrInstance createCollection(String collectionName, Path confDir, String configclass) throws InstanceInitException {
        try (final CloudSolrClient client = createSolrClient()) {
            client.connect();
            final List<String> configs = client.getZkStateReader().getConfigManager().listConfigs();

            if (configs.contains(collectionName)) {
                throw new InstanceInitException("Configuration '" + collectionName + "' already exists!");
            }
            client.uploadConfig(findPath(confDir), collectionName);
            logger.info("Uploaded configuration '{}' to zookeeper {}", collectionName, zkHost);

            return createCollection(client, collectionName, collectionName, configclass);

        } catch (IOException | SolrServerException e) {
            throw new InstanceInitException("Cannot initialize collection", e);
        }
    }

    private SolrInstance createCollection(CloudSolrClient client, String collectionName, String configName, String configclass) throws SolrServerException, IOException {
        if (listConfigSets(client).contains(configName)) {
            if (!listCollections(client).contains(collectionName)) {
                if (properties.getConfigs().containsKey(configclass)) {
                    final ConfigClass configClass = properties.getConfigs().get(configclass);

                    final NamedList<Object> response = client.request(CollectionAdminRequest.createCollection(collectionName, configName, configClass.getShards(), configClass.getReplicas()));
                    logger.info("Created collection '{}' with configuration '{}'", collectionName, configName);
                    return new SolrInstance(response);

                } else throw new IOException("ConfigClass " + configclass + " is not defined");
            } else {
                throw new IOException("Collection with name '" + collectionName + "' already exists!");
            }
        } else {
            throw new IOException("No configuration with name '" + configName + "' found!");
        }
    }

    private CloudSolrClient createSolrClient() {
        return new CloudSolrClient.Builder()
                .withZkHost(zkHost)
                .build();
    }

    private Path findPath(Path tempFolder) throws IOException, InstanceInitException {
        List<Path> files = Files.find(tempFolder,
                Integer.MAX_VALUE,
                (filePath, fileAttr) -> filePath.getFileName().toString().equals("solrconfig.xml"))
                .collect(Collectors.toList());

        switch(files.size()) {
            case 0: throw new InstanceInitException("Config does not contain solrconfig.xml");
            case 1: return files.get(0).getParent();
            default: throw new InstanceInitException("Config contains more then one solrconfig.xml");
        }
    }

    public List<String> listCollections() {
        try (final CloudSolrClient solrClient = createSolrClient()) {
            return listCollections(solrClient);
        } catch (IOException | SolrServerException e) {
            logger.error("Error talking to Solr: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<String> listCollections(CloudSolrClient solrClient) throws SolrServerException, IOException {
        final NamedList<Object> response = solrClient.request(CollectionAdminRequest.listCollections());
        return response.getAll("collections").stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public List<String> listConfigSets() {
        try (final CloudSolrClient solrClient = createSolrClient()) {
            return listConfigSets(solrClient);
        } catch (IOException e) {
            logger.error("Error talking to Solr: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<String> listConfigSets(CloudSolrClient solrClient) throws IOException {
        solrClient.connect();
        return solrClient.getZkStateReader().getConfigManager().listConfigs();
    }
}
