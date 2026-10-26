package com.ispw.uniride;

import com.ispw.uniride.bean.RegistrationBean;
import com.ispw.uniride.bean.RideBean;
import com.ispw.uniride.bean.UserBean;
import com.ispw.uniride.exceptions.InvalidInputException;

/**
 * Costruzione dei Bean di input usata dai test: replica ciò che fanno le Boundary, che compilano
 * ogni Bean tramite i suoi setter di validazione sintattica.
 * @author Alessio Carta
 */
final class TestBeans {

    static UserBean user(String username, String password) throws InvalidInputException {
        UserBean bean = new UserBean();
        bean.setUsername(username);
        bean.setPassword(password);
        return bean;
    }

    static RegistrationBean registration(String username, String password, String fullName,
                                         String homeLocation, String phoneNumber) throws InvalidInputException {
        RegistrationBean bean = new RegistrationBean();
        bean.setUsername(username);
        bean.setPassword(password);
        bean.setFullName(fullName);
        bean.setHomeLocation(homeLocation);
        bean.setPhoneNumber(phoneNumber);
        return bean;
    }

    static RideBean ride(String departure, String destination, String date, String departureTime,
                         int totalSeats, double basePrice) throws InvalidInputException {
        RideBean bean = new RideBean();
        bean.setDeparture(departure);
        bean.setDestination(destination);
        bean.setDate(date);
        bean.setDepartureTime(departureTime);
        bean.setTotalSeats(String.valueOf(totalSeats));
        bean.setBasePrice(String.valueOf(basePrice));
        return bean;
    }

    private TestBeans() {
    }
}
