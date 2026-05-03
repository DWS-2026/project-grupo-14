package es.codeurjc.AcademiaElSoto.mapper;

import org.mapstruct.Mapper;
import java.util.Collection;
import java.util.List;

import es.codeurjc.AcademiaElSoto.dto.CartRequestDto;
import es.codeurjc.AcademiaElSoto.dto.CartResponseDto;
import es.codeurjc.AcademiaElSoto.model.Cart;

@Mapper(componentModel = "spring")
public interface CartMapper {


    CartResponseDto toDTO(Cart cart);

    List<CartResponseDto> toDTOs(Collection<Cart> carts);

    Cart toEntity(CartRequestDto cartRequestDto);
}