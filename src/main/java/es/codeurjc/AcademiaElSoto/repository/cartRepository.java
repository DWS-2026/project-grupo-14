package es.codeurjc.AcademiaElSoto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.AcademiaElSoto.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
