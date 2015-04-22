package nz.co.gregs.properties.examples;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import nz.co.gregs.properties.PropertyDefinition;
import nz.co.gregs.properties.adapt.AdaptableType;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gregorygraham
 */
public class DBDate extends AdaptableType {

	private Date date = null;
	private PropertyDefinition wrapper;

	public DBDate() {
		super();
	}

	public DBDate(Date date) {
		this();
		this.date = date;
	}

	public Date getValue() {
		return date;
	}

	public void setValue(Date object) {
		this.date = object;
	}

	public void setValue(String object) throws ParseException {
		this.date =new SimpleDateFormat().parse(object);
	}

	@Override
	public void setValue(Object object) {
		try {
			setValue(object.toString());
		} catch (ParseException ex) {
			Logger.getLogger(DBDate.class.getName()).log(Level.SEVERE, null, ex);
			throw new RuntimeException(ex);
		}
	}	
}
