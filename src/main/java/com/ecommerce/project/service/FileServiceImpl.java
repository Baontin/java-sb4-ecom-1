package com.ecommerce.project.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        // get original filename
        String originalFilename = file.getOriginalFilename();

        // generate a unique file name
        String randomId = UUID.randomUUID().toString();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = randomId.concat(extension);
        String filePath = path + File.separator + filename;

        // check if path exists and create
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // upload to server
        /* file.getInputStream() gives access to the raw bytes of that image.
           (the content of the uploaded image as a stream of bytes)
        *  Files.copy() reads those bytes and writes them into a new file at images/...jpg.
        * */
        Files.copy(file.getInputStream(), Paths.get(filePath));

        // return filename
        return filename;
    }
}
