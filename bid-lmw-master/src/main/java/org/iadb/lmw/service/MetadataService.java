package org.iadb.lmw.service;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface MetadataService {

    Resource loadFileAsResource(String fileName) throws IOException;

    String storeFile(MultipartFile file) throws IOException;

}
