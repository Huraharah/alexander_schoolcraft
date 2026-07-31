public class Customer {
	public String name;
	private String phone;
	private int seat;
	private boolean highchair;
	
	public Customer(String name, String phone) {
		super();
		this.name = name;
		this.phone = phone;
	}

	public Customer(String name, String phone, int seat, boolean highchair) {
		super();
		this.name = name;
		this.phone = phone;
		this.seat = seat;
		this.highchair = highchair;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getSeat() {
		return seat;
	}

	public void setSeat(int seat) {
		this.seat = seat;
	}

	public boolean isHighchair() {
		return highchair;
	}

	public void setHighchair(boolean highchair) {
		this.highchair = highchair;
	}
	
	public String toString() {
	    return this.name + ", party of " + this.seat + ", contact number: " + this.phone + ".";
	}

	public boolean equals(Customer c) {
	    String nameFirst = this.name;
	    String nameCompare = c.name;
	    int shortLength = 0;
	    int longName = 0;
	    boolean numMatch = true;
	    
	    if (nameFirst.length() < nameCompare.length()) {
		shortLength = nameFirst.length();
		longName = -1;
	    }
	    else if (nameFirst.length() > nameCompare.length()) {
		shortLength = nameCompare.length();
		longName = 1;
	    }
	    else if (nameFirst.length() == nameCompare.length()) {
		shortLength = nameFirst.length();
		longName = 0;
	    }
	    
	    char[] name1 = new char[shortLength];
	    char[] nameC = new char[shortLength];
	    
	    for (int i = 0; i < shortLength; i ++) {
		name1[i] = nameFirst.charAt(i);
		nameC[i] = nameCompare.charAt(i);
	    }
	    
	    for (int j = 0; j < shortLength; j ++) {
		if (name1[j] > nameC [j]) {
		    return false;
		}
		else if (name1[j] < nameC [j]) {
		    return false;
		}
	    }
	    
	    for (int k = 0; k < 10; k ++) {
		if (this.phone.charAt(k) != c.phone.charAt(k)) {
		    numMatch = false;
		}
	    }
	    if (longName == 0 && numMatch == true) {
		return true;
	    }
	    else {
		return false;
	    }
	}

	public String getName() {
	    return name;
	}


	public void setName(String name) {
	    this.name = name;
	}

	public int compareTo(Customer c) {
	    if (this.seat > c.seat) {
		return 1;
	    }
	    else if (this.seat < c.seat) {
		return -1;
	    }
	    else {
		return 0;
	    }
	}
}
