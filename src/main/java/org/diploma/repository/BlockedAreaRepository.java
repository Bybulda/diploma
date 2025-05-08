package org.diploma.repository;

import org.diploma.entity.BlockedArea;
import org.diploma.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockedAreaRepository extends JpaRepository<BlockedArea, Long> {
    List<BlockedArea> findByRouteId(Long routeId);
    List<BlockedArea> findByRoute(Route route);
}