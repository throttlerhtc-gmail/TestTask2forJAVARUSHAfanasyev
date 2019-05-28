package com.space.service;


import com.querydsl.core.types.Predicate;
import com.space.model.Ship;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;


@Service
public class ShipDAO {

    @Autowired
    ShipRepository shipRepository;

    /*to save an ship*/
    public Ship save(Ship ship) {
        return shipRepository.save(ship);
    }


    /* search all ships*/
    public List<Ship> findAllShips() {
        return shipRepository.findAll();
    }

    /* count by some param */
    public long countShips(Predicate predicate) {
        return shipRepository.count(predicate);
    }


    /*get an ship by id*/
    public Ship findOne(Long id) {
        return shipRepository.findById(id).get();      // !!!!!!
    }


    /*delete an ship*/
    public void delete(Ship ship) {
        shipRepository.delete(ship);
    }

    /*business logic*/

    public Double rateShip(Ship ship) {
        Calendar calendar = Calendar.getInstance();
        if(ship == null) return null;
        Double k = ship.getUsed() ? 0.5d : 1.0d ;
        calendar.clear();
        calendar.setTime(ship.getProdDate());
        Double rate = (80 * ship.getSpeed() * k)/(3020 - calendar.get(Calendar.YEAR));
        return ((int)Math.round(100 * rate)) / 100.0;
    }
}