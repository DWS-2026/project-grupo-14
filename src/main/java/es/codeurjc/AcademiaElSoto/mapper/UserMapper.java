package es.codeurjc.AcademiaElSoto.mapper;

import org.mapstruct.Mapper;

import org.mapstruct.MappingTarget;
import java.util.Collection;
import java.util.List;

import es.codeurjc.AcademiaElSoto.dto.UserBasicDto;
import es.codeurjc.AcademiaElSoto.dto.UserRequestDto;
import es.codeurjc.AcademiaElSoto.dto.UserResponseDto;
import es.codeurjc.AcademiaElSoto.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    
    UserResponseDto toDTO(User user);

    
    List<UserResponseDto> toDTOs(Collection<User> users);
    
    UserBasicDto toBasicDTO(User user);

    
    User toEntity(UserRequestDto dto);

    
    void updateEntity(UserRequestDto dto, @MappingTarget User user);
}