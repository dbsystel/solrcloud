package db.garagedays.solrcloud.service;

import db.garagedays.solrcloud.exception.InstanceInitException;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 09.01.17.
 */
@Service
public class ApplicationService {

    private static final String zkHost = "localhost:9983";

    private Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    public SolrInstance create(File file) throws InstanceInitException {

        try {
            //unzip file
            Path temp = Files.createTempDirectory(String.valueOf(System.nanoTime()));
            ZipFile zipFile = new ZipFile(file);
            zipFile.extractAll(temp.toFile().getAbsolutePath());

            logger.info("File unziped to temporary directory " + temp.toFile().getAbsolutePath());

            String configName = temp.getFileName().toString();
            String collectionName = temp.getFileName().toString();

            CloudSolrClient.Builder builder = new  CloudSolrClient.Builder();
            builder.withZkHost(zkHost);

            CloudSolrClient client = builder.build();
            client.uploadConfig(findPath(temp), configName);

            logger.info("Uploaded configuration '{}' to zookeeper {}",configName, zkHost);

            NamedList<Object> response = client.request(CollectionAdminRequest.createCollection(collectionName, configName, 2, 1));

            logger.info("Created collection '{}' with configuration '{}'", collectionName, configName);

            return new SolrInstance();

        } catch (ZipException | IOException | SolrServerException e) {
            throw new InstanceInitException("Cannot initialize collection", e);
        }
    }

    private Path findPath(Path tempFolder) throws IOException {
        List<Path> files = Files.find(tempFolder,
                Integer.MAX_VALUE,
                (filePath, fileAttr) -> filePath.getFileName().toString().equals("solrconfig.xml"))
        .collect(Collectors.toList());

        switch(files.size()) {
            case 0: throw new IOException("Zip file does not contain solrconfig.xml");
            case 1: return files.get(0).getParent();
            default: throw new IOException("Zip file does contain more then one solrconfig.xml");
        }
    }

}
