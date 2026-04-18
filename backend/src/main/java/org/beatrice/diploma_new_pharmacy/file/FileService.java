package org.beatrice.diploma_new_pharmacy.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
// TODO replace try..catch with something else
public class FileService {

    @Value("${media.path:media}")
    private String mediaPath;

    public String saveFile(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");

        Path uploadDir = Paths.get(mediaPath);
        if (!Files.exists(uploadDir)) {
            try {
                Files.createDirectories(uploadDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null) fileName = UUID.randomUUID().toString();

        Path filePath = uploadDir.resolve(fileName);
        try {
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileName;
    }
}
