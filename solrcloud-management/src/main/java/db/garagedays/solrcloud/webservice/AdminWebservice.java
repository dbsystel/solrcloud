package db.garagedays.solrcloud.webservice;

import db.garagedays.solrcloud.exception.InstanceInitException;
import db.garagedays.solrcloud.model.SolrInstance;
import db.garagedays.solrcloud.service.SolrCloudService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 09.01.17.
 */
@RestController
public class AdminWebservice {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    SolrCloudService service;

    @PostMapping("/create")
    public ResponseEntity<?> createCollection(
            @RequestParam(value="file") MultipartFile configZip,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("collection") String collectionName,
            @RequestParam("configclass") String configclass,
            @RequestParam("configname") String configName
    ) throws IOException {

        if (StringUtils.isBlank(configName) && configZip == null) {
            return ResponseEntity.badRequest().body("One of 'configName' or 'file' is required");
        } else if (configZip != null) {
            final Path confDir = unpackUpload(configZip);
            try {
                final SolrInstance instance = service.createCollection(collectionName, confDir, configclass);
            return ResponseEntity.created(instance.getBaseUrl()).body(instance);
            } catch (InstanceInitException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
            } finally {
                Files.deleteIfExists(confDir);
            }
        } else {
            try {
                final SolrInstance instance = service.createCollection(collectionName, configName, configclass);
                return ResponseEntity.created(instance.getBaseUrl()).body(instance);
            } catch (InstanceInitException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e);
            }
        }
    }

    @GetMapping("/configurations")
    public List<String> listConfigurations() {
        return service.listConfigSets();
    }

    private Path unpackUpload(MultipartFile configZip) throws IOException {
        final Path tempDir = Files.createTempDirectory(StringUtils.defaultString(configZip.getOriginalFilename(), "collection-config"));
        try {
            try (ZipInputStream zis = new ZipInputStream(configZip.getInputStream())) {
                for (ZipEntry e = zis.getNextEntry(); e != null; e = zis.getNextEntry()) {
                    final String name = e.getName();
                    if (e.isDirectory()) {
                        Files.createDirectories(tempDir.resolve(name));
                    } else {
                        Files.copy(zis, tempDir.resolve(name));
                    }
                }
            }
            log.info("File unziped to temporary directory {}", tempDir);

        } catch (final Throwable t) {
            FileUtils.deleteDirectory(tempDir.toFile());
            throw t;
        }
        return tempDir;
    }

}
