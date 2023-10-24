package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		customerRepository2.save(customer);
		//Save the customer in database
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		Optional<Customer>customer= customerRepository2.findById(customerId);
		if(!customer.isPresent()){

		}
		else {
			customerRepository2.delete(customer.get());
		}
		// Delete customer without using deleteById function


	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> drivers=driverRepository2.findAll();
		Driver lower_id=null;
		//int min=Integer.MAX_VALUE;
		for(Driver driver:drivers){
			if(driver.getCab().isAvailable()){
		//		min=driver.getDriverId();
				lower_id=driver;
				break;
			}

		}
		if(lower_id==null){
			throw new Exception("No cab available!");
		}
		int bill=lower_id.getCab().getPerKmRate()*distanceInKm;
		Cab cab=lower_id.getCab();
		cab.setAvailable(false);
		TripBooking tripBooking=new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setTripStatus(TripStatus.CONFIRMED);

		tripBooking.setDistanceInKm(distanceInKm);

		Optional <Customer > customer = customerRepository2.findById(customerId);
		Customer customer1=customer.get();
		customer1.getTripBookings().add(tripBooking);
		lower_id.getTripBookings().add(tripBooking);
		tripBooking.setCustomer(customer1);
		tripBooking.setDriver(lower_id);
		tripBooking.setBill(bill);
		customerRepository2.save(customer1);//---
		tripBookingRepository2.save(tripBooking);
	    return tripBooking;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> tripBooking=tripBookingRepository2.findById(tripId);
		if(!tripBooking.isPresent()){
			return;
		}
		TripBooking tripBooking1=tripBooking.get();
		tripBooking1.setTripStatus(TripStatus.CANCELED);
		tripBooking1.setBill(0);//--
		Driver driver=tripBooking1.getDriver();
		Cab cab =driver.getCab();
		cab.setAvailable(true);
		driverRepository2.save(driver);
		tripBookingRepository2.save(tripBooking1);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> tripBooking=tripBookingRepository2.findById(tripId);
		if(!tripBooking.isPresent()){
			return;
		}
		TripBooking tripBooking1=tripBooking.get();
		tripBooking1.setTripStatus(TripStatus.COMPLETED);
		Driver driver=tripBooking1.getDriver();
		Cab cab =driver.getCab();
		cab.setAvailable(true);
        driverRepository2.save(driver);
		tripBookingRepository2.save(tripBooking1);
	}
}
