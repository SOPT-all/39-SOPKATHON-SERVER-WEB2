package org.sopt.sopkathon.domain.place.repository;

import java.util.List;
import org.sopt.sopkathon.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {

  List<Place> findAllByActiveTrue();
}
