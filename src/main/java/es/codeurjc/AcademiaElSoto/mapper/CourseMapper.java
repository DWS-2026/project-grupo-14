package es.codeurjc.AcademiaElSoto.mapper;

import es.codeurjc.AcademiaElSoto.dto.CourseRequestDto;
import es.codeurjc.AcademiaElSoto.dto.CourseResponseDto;
import es.codeurjc.AcademiaElSoto.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    
    CourseResponseDto toDTO(Course course);

   
    Course toEntity(CourseRequestDto dto);

    
    List<CourseResponseDto> toDTOs(Collection<Course> courses);

    
    void updateEntity(CourseRequestDto dto, @MappingTarget Course course);
}