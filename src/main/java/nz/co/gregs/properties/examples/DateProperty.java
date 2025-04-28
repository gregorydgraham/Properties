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
 * a Property for values stored as Dates.
 *
 * @author Gregory Graham
 */
public class DateProperty extends AdaptableType<Date> {

  /**
   * A Property for handling Date values.
   *
   * <p>
   * Yes, this is java.util.Date. We've been doing this for a while :D</p>
   *
   */
  public DateProperty() {
    super();
  }

  /**
   * A Property for handling Date values.
   *
   * <p>
   * Yes, this is java.util.Date. We've been doing this for a while :D</p>
   *
   * @param date the initial value to use
   */
  public DateProperty(Date date) {
    super(date);
  }

  /**
   * Uses SimpleDateFormat to attempt to set date value.
   *
   * @param object the date value maybe
   * @throws ParseException in most cases
   */
  public void setValue(String object) throws ParseException {
    setValue(new SimpleDateFormat().parse(object));
  }
}
