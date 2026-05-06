package es.codeurjc.AcademiaElSoto.mapper;

import es.codeurjc.AcademiaElSoto.dto.CourseBasicDto;
import es.codeurjc.AcademiaElSoto.dto.CourseRequestDto;
import es.codeurjc.AcademiaElSoto.dto.CourseResponseDto;
import es.codeurjc.AcademiaElSoto.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    CourseResponseDto toDTO(Course course);

    Course toEntity(CourseRequestDto dto);

    @Mapping(target = "title", source = "courseName")
    CourseBasicDto toBasicDTO(Course course);

    List<CourseResponseDto> toDTOs(Collection<Course> courses);

    void updateEntity(CourseRequestDto dto, @MappingTarget Course course);
}