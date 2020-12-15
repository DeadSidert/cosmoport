package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ShipService {

    private final ShipRepository shipRepository;

    @Autowired
    public ShipService(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    public Ship update(Ship ship){
        ship.setRating(checkRating(ship));
        return shipRepository.save(ship);
    }

    public Ship get(Long id) {
        return shipRepository.findById(id).get();
    }

    public double checkRating(Ship ship){
        double speed = ship.getSpeed();
        double k = !ship.getUsed() ? 1 : 0.5;

        DateFormat TIMESTAMP = new SimpleDateFormat("yyyy");
        String nowYear = TIMESTAMP.format(new Date(33103209600000L));
        String prodYear = TIMESTAMP.format(ship.getProdDate());
        
        double result = 80*speed*k/(Integer.parseInt(nowYear) - Integer.parseInt(prodYear) +1);
        String lastResult = String.format("%.2f", result).replace(",", ".");

        double rating = Double.parseDouble(lastResult);

        return rating;
    }

    public List<Ship> findAll(){
        return shipRepository.findAll();
    }

    public void deleteById(Long id){
        shipRepository.deleteById(id);
    }

    public long countShip(){
        return shipRepository.count();
    }

    public boolean existById(Long id){
        return shipRepository.existsById(id);
    }

    public boolean idChecker(String id) {
        if (id == null || id.equals("0") || id.equals("")) {
            return false;
        }
        try {
            Long userId = Long.parseLong(id);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public List<Ship> getAllExistingShipsList(Specification<Ship> specification) {
        return shipRepository.findAll(specification);
    }

    public Page<Ship> getAllExistingShipsList(Specification<Ship> specification, Pageable sortedByName) {
        return shipRepository.findAll(specification, sortedByName);
    }

    public Specification<Ship> dateFilter(Long after, Long before) {
        return (root, query, criteriaBuilder) -> {
            if (after == null && before == null) {
                return null;
            }
            if (after == null) {
                Date before1 = new Date(before);
                return criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), before1);
            }
            if (before == null) {
                Date after1 = new Date(after);
                return criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), after1);
            }
            //time difference
            Date before1 = new Date(before - 3600001);
            Date after1 = new Date(after);
            return criteriaBuilder.between(root.get("prodDate"), after1, before1);
        };
    }

    public List<Ship> getShips(
            String name,
            String planet,
            ShipType shipType,
            Long after,
            Long before,
            Boolean isUsed,
            Double minSpeed,
            Double maxSpeed,
            Integer minCrewSize,
            Integer maxCrewSize,
            Double minRating,
            Double maxRating
    ) {
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);
        final List<Ship> list = new ArrayList<>();
        shipRepository.findAll().forEach((ship) -> {
            if (name != null && !ship.getName().contains(name)) return;
            if (planet != null && !ship.getPlanet().contains(planet)) return;
            if (shipType != null && ship.getShipType() != shipType) return;
            if (afterDate != null && ship.getProdDate().before(afterDate)) return;
            if (beforeDate != null && ship.getProdDate().after(beforeDate)) return;
            if (isUsed != null && ship.getUsed().booleanValue() != isUsed.booleanValue()) return;
            if (minSpeed != null && ship.getSpeed().compareTo(minSpeed) < 0) return;
            if (maxSpeed != null && ship.getSpeed().compareTo(maxSpeed) > 0) return;
            if (minCrewSize != null && ship.getCrewSize().compareTo(minCrewSize) < 0) return;
            if (maxCrewSize != null && ship.getCrewSize().compareTo(maxCrewSize) > 0) return;
            if (minRating != null && ship.getRating().compareTo(minRating) < 0) return;
            if (maxRating != null && ship.getRating().compareTo(maxRating) > 0) return;

            list.add(ship);
        });
        return list;
    }

    public List<Ship> sortShips(List<Ship> ships, ShipOrder order) {
        if (order != null) {
            ships.sort((ship1, ship2) -> {
                switch (order) {
                    case ID: return ship1.getId().compareTo(ship2.getId());
                    case SPEED: return ship1.getSpeed().compareTo(ship2.getSpeed());
                    case DATE: return ship1.getProdDate().compareTo(ship2.getProdDate());
                    case RATING: return ship1.getRating().compareTo(ship2.getRating());
                    default: return 0;
                }
            });
        }
        return ships;
    }
    public List<Ship> getPage(List<Ship> ships, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > ships.size()) to = ships.size();
        return ships.subList(from, to);
    }
}
