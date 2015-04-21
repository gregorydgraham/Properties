package nz.co.gregs.properties;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class DBDate implements AdaptableType {

	private Date date = null;
	private PropertyWrapperDefinition wrapper;

	public DBDate() {
		super();
	}

	public DBDate(Date date) {
		this();
		this.date = date;
	}

	public Date getLiteralValue() {
		return date;
	}

	public Object getOperator() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void adaptTo(AdaptableType source) {
		this.setValue(source.getValue());
	}

	public Object getValue() {
		return date;
	}

	public void setValue(Date object) {
		this.date = object;
	}

	public void setValue(String object) throws ParseException {
		this.date =new SimpleDateFormat().parse(object);
	}

	public void setValue(Object object) {
		try {
			setValue(object.toString());
		} catch (ParseException ex) {
			Logger.getLogger(DBDate.class.getName()).log(Level.SEVERE, null, ex);
			throw new RuntimeException(ex);
		}
	}

	public void setPropertyWrapper(PropertyWrapperDefinition propertyWrapperDefn) {
		this.wrapper = propertyWrapperDefn;
	}

	public boolean isNull() {
		return date == null;
	}

	public void clear() {
		date = null;
	}

	
}
