public class Restaurant {
	private Customer[] currList;
	private Customer[] waitList;
	private int seats;
	private final int WAIT = 10;
	

	public Restaurant( int seats) {
	    this.currList = new Customer[seats];
	    this.waitList = new Customer[WAIT];
	    this.seats = seats;
	}

	protected static void printList (Restaurant r, int n) {
	
	    System.out.println("Currently seated customers:");
	    for (int i = 0; i < n; i++) {
		if (r.currList[i] != null) {
		    System.out.println(i + 1 + ": " + r.currList[i]);
		}
	    }
	    System.out.println('\n' + "Current Wait List:");
	    for (int i = 0; i < n; i++) {
		if (r.waitList[i] != null) {
		    System.out.println(i + 1 + ": " + r.waitList[i]);
		}
	    }
	}

	protected static int checkSeat(Restaurant r) {
	    int counter = 0;
	    for (int i = 0; i < r.currList.length; i++) {
		counter += r.currList[i].getSeat();
	    }
	    return counter;
	}

	public int searchList (Customer c) {
	    int isPresent = -1;
	    
	    for (int i = 0; i < this.currList.length; i ++) {
		if (this.currList[i] != null && this.currList[i].equals(c) == true) {
		   isPresent = 0;
		}
	    }
	    
	    for (int j = 0; j < this.waitList.length; j ++) {
		if (this.waitList[j] != null && this.waitList[j].equals(c) == true) {
		    isPresent = 1;
		}
	    }
	    
	    return isPresent;
	}
	
	public void addCustomer (Customer c) {
	    int seat = c.getSeat();
	    int seatsFilled = 0;
	    int seatsEmpty = seats;
	    int openSeat = 0;
	    int waitCount = 0;
	    boolean custFound = false;

	    for (int g = 0; g < currList.length; g ++) {
		if (currList[g] != null && c.equals(currList[g])) {
		    System.out.println("Customer (" + c.getName() +") all ready seated at table " + g);
		    custFound = true;
		}
		else if (currList[g] == null) {
		    break;
		}
	    }
	    for (int h = 0; h < waitList.length; h ++) {
		if (waitList[h] != null && c.equals(waitList[h])) {
		    System.out.println("Customer (" + c.getName() +") all ready on waitlist in spot " + h);
		    custFound = true;
		}
		else if (waitList[h] == null) {
		    break;
		}
	    }
	    if (!custFound) {
                for (int i = 0; i < currList.length; i ++) {
                    if (currList[i] != null) {
                	seatsFilled += currList[i].getSeat();
                    }
                    else if (currList[i] == null) {
                	openSeat = i;
                	break;
                    }
                }
                	    
                seatsEmpty -= seatsFilled;
                	    
                if (seat < seatsEmpty) {
                    currList[openSeat] = c;
                    System.out.println("Customer (" + c.getName() +") seated in spot " + openSeat);
                }
                	    
                else if (seat > seatsEmpty) {
                    for (int j = 0; j < waitList.length; j ++) {
                	if (waitList[j] != null) {
                	    waitCount ++;
                	}
                	else if (waitList[j] == null) {
                	    break;
                	}
                    }
                		
                    if (waitCount >= waitList.length) {
                	System.out.println("Restaurant is full, unable to seat customer or add to waitlist. Please try again later");
                    }
                    else {
                	waitList[waitCount] = c;
                	System.out.println("All tables filled, adding customer (" + c.getName() +") to waitlist in spot number " + waitCount);
                    }
                 }
	    }
	}

	public void remove(Customer c) {
	    int custLocation = 0;
	    boolean onWaitList = false;
	    boolean atTable = false;
	    int firstOpen = 0;
	    int collapseTo = 0;
	    
	    for (int i = 0; i < WAIT; i ++) {
		if (waitList[i] != null && c.equals(waitList[i])) {
		    custLocation = i;
		    onWaitList = true;
		}
	    }
	    
	    if (onWaitList == true) {
		for (int j = custLocation; j < WAIT; j ++) {
		    if (j + 1 != WAIT) {
			waitList[j] = waitList[j + 1];
		    }
		    else {
			waitList[j] = null;
			System.out.println("Customer " + c.getName() + " has left the waitlist.");
			break;
		    }
		}
	    }
	    
	    for (int k = 0; k < currList.length; k ++) {
		if (currList[k] != null && c.equals(currList[k])) {
		    custLocation = k;
		    atTable = true;
		}
	    }
	    
	    if (atTable == true) {
		for (int l = custLocation; l < currList.length - 1; l ++) {
		    if (currList[l + 1] != null) {
			currList[l] = currList[l + 1];;
		    }
		    else {
			currList[l] = null;
			firstOpen = l;
			System.out.println("Customer " + c.getName() +" has left the restaurant, seating someone from the waitlist.");
			break;
		    }
		}
		currList[currList.length - 1] = null;
		
		for (int m = 0; m < WAIT; m ++) {
		    if (waitList[m] != null && waitList[m].compareTo(c) <= 0 && currList[firstOpen] == null) {
			currList[firstOpen] = waitList[m];
			System.out.println("Now seating " + c.getName() + ", party of " + c.getSeat() +".");
			collapseTo = m;
			break;
		    }
		}
		for (int n = collapseTo; n < WAIT - 1; n ++) {
		    waitList[n] = waitList[n + 1];
		}
		waitList[WAIT - 1] = null;
	    }
	}
}
