package com.natedennis.data.enumeration;

public enum Duration {
	HOURLY(3600),
	DAILY(86400);
	
	private int seconds;
	
	Duration(Integer seconds) {
        this.seconds= seconds;
    }

	public int seconds() {
		return seconds;
	}
} 