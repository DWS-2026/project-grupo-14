package es.codeurjc.AcademiaElSoto.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import es.codeurjc.AcademiaElSoto.model.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
