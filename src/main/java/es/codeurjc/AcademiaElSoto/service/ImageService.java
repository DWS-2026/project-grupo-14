package es.codeurjc.AcademiaElSoto.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.AcademiaElSoto.repository.ImageRepository;
import es.codeurjc.AcademiaElSoto.model.Image;

@Service
public class ImageService {

    @Autowired
    private ImageRepository imageRepository;

    // Folder on disk where images will be saved
    private static final Path IMAGES_FOLDER = Paths.get("imagenes_guardadas").toAbsolutePath().normalize();

    public ImageService() throws IOException {
        // Create the folder on startup if it doesn't exist
        Files.createDirectories(IMAGES_FOLDER);
    }

    public Image getImage(long id) {
        return imageRepository.findById(id).orElseThrow();
    }

    private String getSafeExtension(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "";
        }

        String cleanName = originalName.replace("\\", "/");
        cleanName = Paths.get(cleanName).getFileName().toString();

        int dotIndex = cleanName.lastIndexOf(".");
        if (dotIndex == -1) {
            return "";
        }

        String extension = cleanName.substring(dotIndex).toLowerCase();

        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)$")) {
            throw new IllegalArgumentException("Invalid image extension");
        }

        return extension;
    }

    private Path safeImagePath(String fileName) {
        Path path = IMAGES_FOLDER.resolve(fileName).normalize();

        if (!path.startsWith(IMAGES_FOLDER)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        return path;
    }

    // ADD IMAGE (To disk and DB)
    public Image createImage(MultipartFile file) throws IOException {
        Image image = new Image();

        // Save first to let the DB assign an ID (1, 2, 3...)
        image = imageRepository.save(image);

        // Get the original name and prepend the ID to avoid overwriting (e.g.,
        // 1_photo.jpg)
        String extension = getSafeExtension(file.getOriginalFilename());
        String fileName = "image_" + image.getId() + extension;

        // Save physically on disk
        Path filePath = safeImagePath(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update the entity with the file name and save again
        image.setFileName(fileName);
        return imageRepository.save(image);
    }

    // GET IMAGE (From disk to display)
    public Resource getImageFile(long id) throws MalformedURLException {
        Image image = imageRepository.findById(id).orElseThrow();

        if (image.getFileName() != null) {
            Path filePath = safeImagePath(image.getFileName());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Image file not found");
            }

            return resource;
        } else {
            throw new RuntimeException("Image file not found");
        }
    }

    // UPDATE IMAGE
    public void replaceImageFile(long id, MultipartFile file) throws IOException {
        Image image = imageRepository.findById(id).orElseThrow();

        // If it already had an image, delete it from disk to avoid clutter
        if (image.getFileName() != null) {
            Path oldPath = safeImagePath(image.getFileName());
            Files.deleteIfExists(oldPath);
        }

        // Save the new one with its original name
        String extension = getSafeExtension(file.getOriginalFilename());
        String newFileName = "image_" + id + extension;

        Path filePath = safeImagePath(newFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        image.setFileName(newFileName);
        imageRepository.save(image);
    }

    // DELETE IMAGE (From DB and disk)
    public Image deleteImage(long id) {
        Image image = imageRepository.findById(id).orElseThrow();

        // Delete from physical disk
        if (image.getFileName() != null) {
            try {
                Path filePath = safeImagePath(image.getFileName());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Basic error handling in case the file was already gone
                e.printStackTrace();
            }
        }

        // Delete from database
        imageRepository.deleteById(id);
        return image;
    }
}