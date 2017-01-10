package db.garagedays.solrcloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Thomas Kurz (tkurz@apache.org)
 * @since 09.01.17.
 */
@RestController
public class AdminWebservice {

    @Autowired
    ApplicationService service;

    @PostMapping("/create")
    public String handleFileUpload(
            @RequestParam("file") MultipartFile file) throws IOException {

        service.create(convert(file));

        return "You successfully uploaded " + file.getOriginalFilename() + "!";
    }

    public File convert(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

}
