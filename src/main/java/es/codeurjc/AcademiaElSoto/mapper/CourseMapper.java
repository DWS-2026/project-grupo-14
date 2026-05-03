package es.codeurjc.AcademiaElSoto.mapper;

import es.codeurjc.AcademiaElSoto.dto.CourseBasicDto;
import es.codeurjc.AcademiaElSoto.dto.CourseRequestDto;
import es.codeurjc.AcademiaElSoto.dto.CourseResponseDto;
import es.codeurjc.AcademiaElSoto.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    @Mapping(target = "students", ignore = true) 
    CourseResponseDto toDTO(Course course);

    @Mapping(target = "students", ignore = true) 
    Course toEntity(CourseRequestDto dto);

    
    CourseBasicDto toBasicDTO(Course course); 

    List<CourseResponseDto> toDTOs(Collection<Course> courses);

    @Mapping(target = "students", ignore = true) 
    void updateEntity(CourseRequestDto dto, @MappingTarget Course course);
}