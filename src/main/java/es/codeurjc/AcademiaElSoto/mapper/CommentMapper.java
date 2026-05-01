package es.codeurjc.AcademiaElSoto.mapper;

import es.codeurjc.AcademiaElSoto.dto.CommentRequestDto;
import es.codeurjc.AcademiaElSoto.dto.CommentResponseDto;

import es.codeurjc.AcademiaElSoto.model.Comment;


import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    
    CommentResponseDto toDTO(Comment comment);

   
    Comment toEntity(CommentRequestDto dto);

    
    List<CommentResponseDto> toDTOs(Collection<Comment> comments);

    
    void updateEntity(CommentRequestDto dto, @MappingTarget Comment comment);
}
