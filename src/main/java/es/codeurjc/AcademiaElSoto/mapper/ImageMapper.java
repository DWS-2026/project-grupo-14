package es.codeurjc.AcademiaElSoto.mapper;

import org.mapstruct.Mapper;

import es.codeurjc.AcademiaElSoto.model.Image;
import es.codeurjc.AcademiaElSoto.dto.ImageDTO;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    
    ImageDTO toDTO(Image image);

    Image toEntity(ImageDTO imageDTO);
}
