package es.codeurjc.AcademiaElSoto.restcontroller;

import java.net.URI;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

import es.codeurjc.AcademiaElSoto.dto.CourseRequestDto;
import es.codeurjc.AcademiaElSoto.dto.CourseResponseDto;
import es.codeurjc.AcademiaElSoto.mapper.CourseMapper;
import es.codeurjc.AcademiaElSoto.model.Course;
import es.codeurjc.AcademiaElSoto.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import es.codeurjc.AcademiaElSoto.model.Image; // Tu modelo Image
import es.codeurjc.AcademiaElSoto.dto.ImageDTO; // Tu DTO
import es.codeurjc.AcademiaElSoto.mapper.ImageMapper; // Tu Mapper de Image
import es.codeurjc.AcademiaElSoto.service.ImageService; // Tu Servicio de Image


@RestController // <-- La etiqueta mágica
@RequestMapping("/api/courses") // <-- Todas las rutas empezarán por /api/courses
public class CourseRestController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseMapper mapper;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageMapper imageMapper;

    @GetMapping
    public Page<CourseResponseDto> getCourses(Pageable pageable) {
        
        return courseService.findAll(pageable).map(mapper::toDTO);
    }

    // Cuando alguien busque un curso específico: /api/courses/1
    @GetMapping("/{id}")
    public CourseResponseDto getCourseById(@PathVariable Long id) {
        Course course = courseService.findById(id).orElseThrow();
        return mapper.toDTO(course); 
    }

    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(@Valid @RequestBody CourseRequestDto courseRequestDto) {

        
        Course course = mapper.toEntity(courseRequestDto);

        Course savedCourse = courseService.save(course);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCourse.getId())
                .toUri();

        return ResponseEntity.created(location).body(mapper.toDTO(savedCourse));
    }

    @PutMapping("/{id}")
    public CourseResponseDto updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequestDto courseRequestDto) {

        Course existingCourse = courseService.findById(id).orElseThrow();

        
        mapper.updateEntity(courseRequestDto, existingCourse);

        Course updatedCourse = courseService.save(existingCourse);
        return mapper.toDTO(updatedCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        Course existingCourse = courseService.findById(id).orElseThrow();
        courseService.deleteById(existingCourse.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Object> getCourseImage(@PathVariable Long id) throws Exception {
        Course course = courseService.findById(id).orElseThrow();
        Blob image = course.getImageFile();

        if (image == null) {
            throw new NoSuchElementException();
        }

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .body(new InputStreamResource(image.getBinaryStream()));
    }

    record BooksResponse(List<Book> items) {
	}

	record Book(VolumeInfo volumeInfo) {
	}

	record VolumeInfo(String title) {
	}

    @GetMapping("/{id}/recommended-books")
    public List<String> getRecommendedBooks(@PathVariable Long id) {
        // 1. Buscamos el curso en nuestra base de datos para saber su nombre
        Course course = courseService.findById(id).orElseThrow();
        String courseName = course.getCourseName(); 

        // 2. Consultamos a Google Books usando el nombre del curso como filtro
        RestClient restClient = RestClient.create();
        
        // Usamos el record o clase BooksResponse que ya tienes definida
        BooksResponse data = restClient.get()
                .uri("https://www.googleapis.com/books/v1/volumes?q=intitle:" + courseName)
                .retrieve()
                .body(BooksResponse.class);

        // 3. Extraemos solo los títulos para no complicar el JSON de respuesta
        List<String> titles = new ArrayList<>();
        if (data != null && data.items() != null) {
            for (Book book : data.items()) {
                titles.add(book.volumeInfo().title());
            }
        }

        return titles;
    }

    @PostMapping("/{id}/images/")
    public ResponseEntity<ImageDTO> addImageToCourse(@PathVariable Long id, @RequestParam MultipartFile imageFile) 
            throws IOException {

        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 1. Creamos la entidad Image a partir del archivo
        // Nota: Asegúrate de que imageService tenga el método createImage que acepte InputStream
        Image image = imageService.createImage(imageFile.getInputStream());

        // 2. Asociamos la imagen al curso
        courseService.addImageToCourse(id, image);

        // 3. Generamos la URI de la nueva imagen (apuntando al endpoint de descarga)
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/courses/{id}/image")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).body(imageMapper.toDTO(image));
    }

    @DeleteMapping("/{courseId}/images/{imageId}")
    public ResponseEntity<ImageDTO> deleteCourseImage(@PathVariable Long courseId, @PathVariable Long imageId) 
            throws IOException {

        // 1. Obtenemos la imagen para poder devolver el DTO al final
        Image image = imageService.getImage(imageId); // Asegúrate de que este método exista en su service
        
        // 2. La desvinculamos del curso (lógica del CourseService)
        courseService.removeImageCourse(courseId, image);
        
        // 3. La eliminamos físicamente/BD
        imageService.deleteImage(imageId);

        return ResponseEntity.ok(imageMapper.toDTO(image));
    }
    
}