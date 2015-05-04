package nz.co.gregs.properties.examples;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class DateProperty extends AdaptableType<Date> {

	public DateProperty() {
		super();
	}

	public DateProperty(Date date) {
		super(date);
	}

	public void setValue(String object) throws ParseException {
		setValue(new SimpleDateFormat().parse(object));
	}
}
