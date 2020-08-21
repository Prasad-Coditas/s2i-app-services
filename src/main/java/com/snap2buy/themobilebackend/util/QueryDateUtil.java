package com.snap2buy.themobilebackend.util;


import com.snap2buy.themobilebackend.model.InputObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class QueryDateUtil {
	
	public String getISOWeekID(String startDate, String endDate,String frequency)  throws Exception{
		
		
		return null;
	}

    private static String dateFormatter(Calendar cal,DateTime week) throws ParseException{
        DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
        Date date = (Date)formatter.parse(week.toDate().toString());

        cal.setTime(date);


        String month;
        if(Integer.valueOf((cal.get(Calendar.MONTH) + 1))<10){
            month="0"+String.valueOf((cal.get(Calendar.MONTH) + 1));

        }else{
            month=String.valueOf((cal.get(Calendar.MONTH) + 1));
        }

        String dateNum;
        if(Integer.valueOf(cal.get(Calendar.DATE))<10){
            dateNum="0"+String.valueOf(cal.get(Calendar.DATE));

        }else{
            dateNum=String.valueOf(cal.get(Calendar.DATE));
        }

        String yearNum;
        yearNum=String.valueOf( cal.get(Calendar.YEAR));
        String formatedDate =  yearNum+month+dateNum;

        System.out.println("formatedDate : " + formatedDate);
        return formatedDate;
    }
	
	public static void setBaseDates(InputObject input) throws Exception {
        DateFormat format = new SimpleDateFormat("yyyyMMdd");
        DateFormat monthFormat = new SimpleDateFormat("yyyyMM");

        Calendar cal = Calendar.getInstance();
        if (input.getFrequency().equals("DAILY")) {

            input.setPrevStartTime(input.getStartDate());
            input.setPrevEndTime(input.getStartDate());

            input.setStartTime(input.getEndDate());
            input.setEndTime(input.getEndDate());
        } else if (input.getFrequency().equals("WEEKLY")) {

            cal.setTime(format.parse(input.getStartDate()));
            DateTime preWeek = new DateTime(cal);
            DateTime weekStart = preWeek.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay();
            DateTime weekEnd = preWeek.withDayOfWeek(DateTimeConstants.SUNDAY).withTimeAtStartOfDay();

            input.setPrevStartTime(dateFormatter(cal, weekStart));
            input.setPrevEndTime(dateFormatter(cal, weekEnd));


            cal.setTime(format.parse(input.getEndDate()));
            DateTime currWeek = new DateTime(cal);
            DateTime currweekStart = currWeek.withDayOfWeek(DateTimeConstants.MONDAY).withTimeAtStartOfDay();
            DateTime currweekEnd = currWeek.withDayOfWeek(DateTimeConstants.SUNDAY).withTimeAtStartOfDay();

            input.setStartTime(dateFormatter(cal, currweekStart));
            input.setEndTime(dateFormatter(cal, currweekEnd));

        } else if (input.getFrequency().equals("MONTHLY")) {

            cal.setTime(format.parse(input.getStartDate()));
            DateTime prevMonth = new DateTime(cal);
            DateTime premonthStart = prevMonth.withDayOfMonth(1).withTimeAtStartOfDay();
            input.setPrevStartTime(dateFormatter(cal, premonthStart));

            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.DATE, -1);
            DateTime prevmonthEnd = new DateTime(cal);
            input.setPrevEndTime(dateFormatter(cal, prevmonthEnd));


            cal.setTime(format.parse(input.getEndDate()));
            DateTime currMonth = new DateTime(cal);
            DateTime currmonthStart = currMonth.withDayOfMonth(1).withTimeAtStartOfDay();
            input.setStartTime(dateFormatter(cal, currmonthStart));

            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.DATE, -1);
            DateTime currmonthEnd = new DateTime(cal);
            input.setEndTime(dateFormatter(cal, currmonthEnd));
        }
    }

}
