package org.diploma.repository;

import org.diploma.entity.Route;
import org.diploma.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByUserId(Long userId);

    List<Route> findByUser(User user);
}
