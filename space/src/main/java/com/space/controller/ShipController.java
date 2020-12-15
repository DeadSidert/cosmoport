package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.model.ShipWrapper;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping(value = "/rest")
public class ShipController {

    private final ShipService shipService;

    @Autowired
    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @RequestMapping(value = "/ships", method = RequestMethod.POST, consumes = { "application/json" })
    public ResponseEntity<Ship> createShip(@RequestBody ShipWrapper body){
        if (body.getName() == null || body.getPlanet() == null || body.getShipType() == null
                || body.getProdDate() == null || body.getSpeed() == null || body.getCrewSize() == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

       String name = body.getName();
       String planet = body.getPlanet();

       if (name.length() > 50 || planet.length() > 50){
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
       }

       if (name.isEmpty() || planet.isEmpty()){
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
       }

       long prodDate = body.getProdDate();

       if (prodDate < 0){
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
       }

        Date startDate = null;
        Date lastDate = null;
        try {
            startDate = new SimpleDateFormat( "dd.MM.yyyy" ).parse( "01.01.2800");
            lastDate = new SimpleDateFormat( "dd.MM.yyyy" ).parse( "01.01.3019");
        } catch (ParseException e) {
            e.printStackTrace();
        }

       if (prodDate < startDate.getTime() || prodDate > lastDate.getTime()){
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
       }

       double speed = body.getSpeed();
       if (speed > 0.99 || speed < 0.01 ){
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
       }

       int crewSize = body.getCrewSize();
       if (crewSize < 1 || crewSize > 9999){
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
       }

       ShipType shipType = null;
       if (body.getShipType().equalsIgnoreCase("Military")){
           shipType = ShipType.MILITARY;
       }
       else if (body.getShipType().equalsIgnoreCase("Transport")){
           shipType = ShipType.TRANSPORT;
       }
       else if (body.getShipType().equalsIgnoreCase("Merchant")){
           shipType = ShipType.MERCHANT;
       }

       Ship ship = new Ship(name, planet, shipType, new Date(prodDate), speed, crewSize);

       ship.setUsed(body.isUsed());
       ship.setRating(shipService.checkRating(ship));

       return ResponseEntity.ok(shipService.update(ship));
    }
    @RequestMapping(value = "/ships/{id}", method = {RequestMethod.DELETE})
    public ResponseEntity deleteById(@PathVariable String id){
        if (!shipService.idChecker(id)){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        if (!id.matches("^-?\\d+$")){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
            Long bodyId = Long.parseLong(id);
        if (bodyId <= 0){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
       if (shipService.existById(bodyId)){
           shipService.deleteById(bodyId);
           return new ResponseEntity(HttpStatus.OK);
       }

       return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
    @RequestMapping(value = "/ships/{id}", method = RequestMethod.GET)
    public ResponseEntity<Ship> getShip(@PathVariable String id){
        if (!shipService.idChecker(id)){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        if (!id.matches("^-?\\d+$")){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        Long bodyId = Long.parseLong(id);
        if (bodyId <= 0){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        if (shipService.existById(bodyId)){
            return ResponseEntity.ok(shipService.get(bodyId));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @RequestMapping(value = "/ships/{id}", method = RequestMethod.POST)
    public ResponseEntity<Ship> updateShip(@PathVariable String id, @RequestBody ShipWrapper shipRequired){
        if (!shipService.idChecker(id)){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        if (shipRequired == null){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        if (!id.matches("^-?\\d+$")){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        Long bodyId = Long.parseLong(id);
        if (bodyId <= 0){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        // все действия здесь
        if (shipService.existById(bodyId)){
            Ship ship = shipService.get(bodyId);
            if (shipRequired.getName() != null){
                if (shipRequired.getName().length() < 1 || shipRequired.getName().length() > 50){
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
                ship.setName(shipRequired.getName());
            }
            if (shipRequired.getShipType() != null){
                ShipType shipType = null;
                if (shipRequired.getShipType().equalsIgnoreCase("Military")){
                    shipType = ShipType.MILITARY;
                }
                else if (shipRequired.getShipType().equalsIgnoreCase("Transport")){
                    shipType = ShipType.TRANSPORT;
                }
                else if (shipRequired.getShipType().equalsIgnoreCase("Merchant")){
                    shipType = ShipType.MERCHANT;
                }
                ship.setShipType(shipType);
            }
            if (shipRequired.getPlanet() != null){
                if (shipRequired.getPlanet().length() > 50){
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
                ship.setPlanet(shipRequired.getPlanet());
            }
            if (shipRequired.getProdDate() != null){
                Calendar date = Calendar.getInstance();
                date.setTime(new Date(shipRequired.getProdDate()));
                if (date.get(Calendar.YEAR) < 2800 || date.get(Calendar.YEAR) > 3019) {
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
                ship.setProdDate(new Date(shipRequired.getProdDate()));
            }
            if (shipRequired.getCrewSize() != null){
                if (shipRequired.getCrewSize() < 1 || shipRequired.getCrewSize() > 9999){
                    return new ResponseEntity(HttpStatus.BAD_REQUEST);
                }
                ship.setCrewSize(shipRequired.getCrewSize());
            }
            if (shipRequired.getSpeed() != null && (shipRequired.getSpeed() > 0.01D || shipRequired.getSpeed() < 9999.99D)){
                ship.setSpeed(shipRequired.getSpeed());
            }

            ship.setRating(shipService.checkRating(ship));
            return ResponseEntity.ok(shipService.update(ship));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/ships")
    @ResponseStatus(HttpStatus.OK)
    public List<Ship> getAllExistingShipsList(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
            @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {

        final List<Ship> ships = shipService.getShips(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating);

        final List<Ship> sortedShips = shipService.sortShips(ships, order);

        return shipService.getPage(sortedShips, pageNumber, pageSize);
    }

    @GetMapping("/ships/count")
    @ResponseStatus(HttpStatus.OK)
    public Integer getShipsCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating
    ) {
        return shipService.getShips(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating).size();
    }

}
