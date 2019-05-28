package com.space.controller;


import com.querydsl.core.types.dsl.BooleanExpression;
import com.space.model.QShip;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import com.space.service.ShipDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;


@EnableSpringDataWebSupport
@RestController
@RequestMapping("/rest")                          // !!
public class ShipController {

    @Autowired
    ShipDAO shipDAO;
    @Autowired
    ShipRepository shipRepository;


    /* to create ship*/
    @PostMapping("/ships")
    public Ship createShip(@RequestBody Ship shipData) {
        if (shipData == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);                     // 400

        String name = shipData.getName();
        String planet = shipData.getPlanet();
        ShipType type = shipData.getShipType();
        Date date = shipData.getProdDate();
        Double speed = shipData.getSpeed();
        Integer crewSize = shipData.getCrewSize();

        if (type == null )
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);                     // 400
        if (date == null )
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);                     // 400
        Boolean b = true;
        if (date.getTime() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);                     // 400
        if (speed == null )
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);                     // 400
        b &= speed > 0 && speed < 1;
        if (name == null )
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);                     // 400
        b &= name.length() > 0 && name.length() < 51 && name.matches(".*\\w.*");
        if (planet == null )
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);                     // 400
        b &= planet.length() > 0 && planet.length() < 51 && planet.matches(".*\\w.*");

        if (crewSize == null )
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);                     // 400
        b &= crewSize > 0 && crewSize < 10000;
        Double k = shipData.getUsed() ? 0.5d : 1.0d ;
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(date);
        if (!b)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (calendar.get(Calendar.YEAR) < 2800 || calendar.get(Calendar.YEAR) > 3019) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);                     // 400
        }
        shipData.setRating(shipDAO.rateShip(shipData));
        return shipDAO.save(shipData);
    }


    /*get all ships*/
    @GetMapping("/ships")
    @ResponseBody
    public List<Ship> findAllShipsByQuerydsl(@RequestParam(name = "name", required = false, defaultValue = "") String name,
                                             @RequestParam(name = "planet", required = false, defaultValue = "") String planet,
                                             @RequestParam(name = "shipType", required = false) ShipType shipType,
                                             @RequestParam(name = "isUsed", required = false) Boolean isUsed,
                                             @RequestParam(name = "order", required = false, defaultValue = "ID") ShipOrder order,
                                             @RequestParam(name = "after", required = false, defaultValue = "26192235600000") Long after,
                                             @RequestParam(name = "before", required = false, defaultValue = "33134734799999") Long before,
                                             @RequestParam(name = "minRating", required = false, defaultValue = "0") Double minRating,
                                             @RequestParam(name = "maxRating", required = false, defaultValue = Double.MAX_VALUE + "") Double maxRating,
                                             @RequestParam(name = "minCrewSize", required = false, defaultValue = "0") Integer minCrewSize,
                                             @RequestParam(name = "maxCrewSize", required = false, defaultValue = Integer.MAX_VALUE + "") Integer maxCrewSize,
                                             @RequestParam(name = "minSpeed", required = false, defaultValue = "0") Double minSpeed,
                                             @RequestParam(name = "maxSpeed", required = false, defaultValue = Double.MAX_VALUE + "") Double maxSpeed,
                                             @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int page,
                                             @RequestParam(name = "pageSize", required = false, defaultValue = "3") int size) {
        QShip ship = QShip.ship;
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(new Date(before));
        int yB = calendar.get(1);
        calendar.clear();
        calendar.set(1, yB);
        Long b = calendar.getTimeInMillis();
        calendar.clear();
        calendar.setTime(new Date(after));
        int yA = calendar.get(1);
        calendar.clear();
        calendar.set(1, yA);
        Long a = calendar.getTimeInMillis();
        BooleanExpression shipNameRequest = null;
        BooleanExpression shipPlanetRequest = null;
        BooleanExpression shipTypeRequest = null;
        BooleanExpression shipUsedRequest = null;
        if (name != null) {
            shipNameRequest = ship.name.contains(name);
        }
        if (planet != null) {
            shipPlanetRequest = ship.planet.contains(planet);
        }
        if (shipType != null) {
            shipTypeRequest = ship.shipType.eq(shipType);
        }
        if (isUsed != null) {
            shipUsedRequest = ship.isUsed.eq(isUsed);
        }
        BooleanExpression shipSpeedInRange = ship.speed.between(minSpeed, maxSpeed);
        BooleanExpression shipCrewSizeInRange = ship.crewSize.between(minCrewSize, maxCrewSize);
        BooleanExpression shipRatingInRange = ship.rating.between(minRating, maxRating);
        BooleanExpression shipProdDateInRange = ship.prodDate.between(new Date(a), new Date(b));
        return shipRepository.findAll(shipSpeedInRange.and(shipCrewSizeInRange).and(shipRatingInRange)
                        .and(shipProdDateInRange).and(shipNameRequest).and(shipPlanetRequest).and(shipTypeRequest).and(shipUsedRequest),
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, order.getFieldName()))).getContent();
    }

    /*counting*/
    @GetMapping("/ships/count")
    public Integer getShipsCount(@RequestParam(name = "name", required = false, defaultValue = "") String name,
                                 @RequestParam(name = "planet", required = false, defaultValue = "") String planet,
                                 @RequestParam(name = "shipType", required = false) ShipType shipType,
                                 @RequestParam(name = "isUsed", required = false) Boolean isUsed,
                                 @RequestParam(name = "after", required = false, defaultValue = "26192235600000") Long after,
                                 @RequestParam(name = "before", required = false, defaultValue = "33134734799999") Long before,
                                 @RequestParam(name = "minRating", required = false, defaultValue = "0") Double minRating,
                                 @RequestParam(name = "maxRating", required = false, defaultValue = Double.MAX_VALUE + "") Double maxRating,
                                 @RequestParam(name = "minCrewSize", required = false, defaultValue = "1") Integer minCrewSize,
                                 @RequestParam(name = "maxCrewSize", required = false, defaultValue = "9999") Integer maxCrewSize,
                                 @RequestParam(name = "minSpeed", required = false, defaultValue = "0") Double minSpeed,
                                 @RequestParam(name = "maxSpeed", required = false, defaultValue = Double.MAX_VALUE + "") Double maxSpeed,
                                 @RequestParam(name = "pageNumber", defaultValue = "0") int page,
                                 @RequestParam(name = "pageSize", defaultValue = "3") int size) {
        QShip ship = QShip.ship;
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(new Date(before));
        int yB = calendar.get(1);
        calendar.clear();
        calendar.set(1, yB);
        Long b = calendar.getTimeInMillis();
        calendar.clear();
        calendar.setTime(new Date(after));
        int yA = calendar.get(1);
        calendar.clear();
        calendar.set(1, yA);
        Long a = calendar.getTimeInMillis();
        BooleanExpression shipNameRequest = null;
        BooleanExpression shipPlanetRequest = null;
        BooleanExpression shipTypeRequest = null;
        BooleanExpression shipUsedRequest = null;
        if (name != null) {
            shipNameRequest = ship.name.contains(name);
        }
        if (planet != null) {
            shipPlanetRequest = ship.planet.contains(planet);
        }
        if (shipType != null) {
            shipTypeRequest = ship.shipType.eq(shipType);
        }
        if (isUsed != null) {
            shipUsedRequest = ship.isUsed.eq(isUsed);
        }
        BooleanExpression shipSpeedInRange = ship.speed.between(minSpeed, maxSpeed);
        BooleanExpression shipCrewSizeInRange = ship.crewSize.between(minCrewSize, maxCrewSize);
        BooleanExpression shipRatingInRange = ship.rating.between(minRating, maxRating);
        BooleanExpression shipProdDateInRange = ship.prodDate.between(new Date(a), new Date(b));
        return (int) shipRepository.count(shipSpeedInRange.and(shipCrewSizeInRange).and(shipRatingInRange)
                .and(shipProdDateInRange).and(shipNameRequest).and(shipPlanetRequest).and(shipTypeRequest).and(shipUsedRequest));
    }

    /*get ship by shipid*/
    @GetMapping("/ships/{id}")
    public ResponseEntity<Ship> getShipById(@PathVariable(value = "id") Long shipid) {
        if (shipid <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);                     // 400
        }
        Ship ship = null;
        try {
            ship = shipDAO.findOne(shipid);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);                        // 404
        }

        return new ResponseEntity<Ship>(ship, HttpStatus.OK);
    }


    /*update ship by shipid*/
    @PostMapping("ships/{id}")
    public ResponseEntity<Ship> updateShip(@PathVariable(value = "id") Long shipid,
                                           @RequestBody(required = false) Ship shipDetails) {
//        System.out.println(shipDetails);
        if (shipid <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Ship ship = null;
        try {
            ship = shipDAO.findOne(shipid);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        String name = shipDetails.getName();
        String planet = shipDetails.getPlanet();
        ShipType type = shipDetails.getShipType();
        Date date = shipDetails.getProdDate();
        Boolean isUsed = ship.getUsed();
        Boolean isUsedDetails = shipDetails.getUsed();
        Double speed = shipDetails.getSpeed();
        Integer crewSize = shipDetails.getCrewSize();
        Calendar calendar = Calendar.getInstance();
        Double k = ship.getUsed() ? 0.5d : 1.0d ;
        Boolean allIsNull = true;

        if (name != null && (name.length() == 0 || name.length() > 50 || !name.matches(".*\\w.*"))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if (name != null) {
            ship.setName(name);
            allIsNull = false;
        }
        if (planet != null && (planet.length() == 0 || planet.length() > 50 || !planet.matches(".*\\w.*"))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if (planet != null) {
            ship.setPlanet(planet);
            allIsNull = false;
        }
        if (type != null) {
            ship.setShipType(type);
            allIsNull = false;
        }

        if (date != null) {
            allIsNull = false;
            calendar.clear();
            calendar.setTime(date);
            if (calendar.get(Calendar.YEAR) < 2800 || calendar.get(Calendar.YEAR) > 3019) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                ship.setProdDate(date);
            }
        } else calendar.setTime(ship.getProdDate());
        if (isUsed != null ) {
            allIsNull = false;
            ship.setUsed(isUsed);
            k = isUsed ? 0.5d : 1.0d ;
        }
        if (speed != null && (speed < 0 || speed > 1)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if (speed != null) {
            allIsNull = false;
            ship.setSpeed(speed);
        }
        if (crewSize != null && (crewSize < 0 || crewSize > 10000)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if (crewSize != null) {
            allIsNull = false;
            ship.setCrewSize(crewSize);
        }
        if(allIsNull) {
            return new ResponseEntity<>(ship, HttpStatus.OK);
        }
        ship.setRating(shipDAO.rateShip(ship));

        ship = shipDAO.save(ship);
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    /*Delete ship*/
    @DeleteMapping("/ships/{id}")
    public void deleteShip(@PathVariable(value = "id") Long shipid) {
        if (shipid <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Ship ship = null;
        try {
            ship = shipDAO.findOne(shipid);
            shipDAO.delete(ship);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}