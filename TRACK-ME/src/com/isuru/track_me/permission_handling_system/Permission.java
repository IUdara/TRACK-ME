package com.isuru.track_me.permission_handling_system;

//DateTime(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour)
//Constructs an instance from datetime field values using ISOChronology in the default time zone.

//DateTime(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour, DateTimeZone zone)
//Constructs an instance from datetime field values using ISOChronology in the specified time zone.

import org.joda.time.DateTime;
import org.joda.time.Period;

public class Permission {
	private DateTime permissionStart;
	private DateTime permissionEnd;
	private Period updatePeriod;
	private Place destination;
	private String owner;
	private String permissionCode;

	public Permission(int[][] dateTime, String owner) {
		super();
		this.permissionStart = new DateTime(dateTime[0][0], dateTime[0][1],
				dateTime[0][2], dateTime[1][0], dateTime[1][1]); // use sting
																	// arrays as
																	// input.
		// construct new DataTime using new
		this.permissionEnd = new DateTime(dateTime[2][0], dateTime[2][1],
				dateTime[2][2], dateTime[3][0], dateTime[3][1]);
		this.owner = owner;
		this.updatePeriod = null;
		this.destination = null;
	}

	public DateTime getPermissionStart() {
		return permissionStart;
	}

	public void setPermissionStart(DateTime startingTime) {
		this.permissionStart = startingTime;
	}

	public DateTime getPermissionEnd() {
		return permissionEnd;
	}

	public void setPermissionEnd(DateTime endingTime) {
		this.permissionEnd = endingTime;
	}

	public Place getDestination() {
		return destination;
	}

	public void setDestination(Place destination) {
		this.destination = destination;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setUpdatePeriod(Period updatePeriod) {
		this.updatePeriod = updatePeriod;
	}

	public Period getUpdatePeriod() {
		return updatePeriod;
	}
	
	public String getPermissionCode() {
		return permissionCode;
	}

	public void setPermissionCode(String permissionCode) {
		this.permissionCode = permissionCode;
	}


	public Boolean getIsPeriodic() {
		Boolean isPeriodic;
		if (updatePeriod != null) {
			isPeriodic = true;
		} else {
			isPeriodic = false;
		}
		return isPeriodic;
	}

	public Boolean getIsDestinated() {
		Boolean isDestinated;
		if (destination != null) {
			isDestinated = true;
		} else {
			isDestinated = false;
		}
		return isDestinated;
	}
}
