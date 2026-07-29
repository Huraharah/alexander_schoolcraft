public class TestDriver {

	public static void main(String[] args) {
		Customer jacob = new Customer ("Jacob", "1115551111", 3, false);//
		Customer mike = new Customer ("Mike", "2225552222" ,2, false);//
		Customer anna = new Customer ("Anna", "3335553333" ,5, true);//
		Customer emily = new Customer ("Emily", "4445554444", 4, false);//
		Customer owen = new Customer ("Owen", "5555555555", 2, false);
		Customer sara = new Customer ("Sara", "6665556666", 3, false);
		Customer chris = new Customer ("Chris", "7775557777", 4, true);
		Customer romana = new Customer ("Romana", "8885558888", 2, false);
		Customer jose = new Customer ("Jose", "9995559999", 1, false);//
		Customer phillip = new Customer ("Phillip", "1015551010", 3, true);
		Customer alex = new Customer ("Alex", "1116661111", 2, false);
		Customer lauren = new Customer ("Lauren", "1215551212", 4, false);
		Customer jenson = new Customer ("Jenson", "5555555555", 2, false);
		Customer mishelle = new Customer ("Mishelle", "1415551414", 1, true);
		Customer anne = new Customer ("Anne", "1515551515", 3, true);
		Customer matt = new Customer ("Matt", "1615551616", 2, true);
		Customer jackie = new Customer ("Jackie", "1715551717", 4, false);
		Customer zane = new Customer ("Zane", "1815551818", 1, false);
		Customer bonnie = new Customer ("Bonnie", "1915551919", 7, false);
		Customer pablo = new Customer ("Pablo", "2025552020", 3, true);
		
		Restaurant r= new Restaurant (15);
		
		r.addCustomer(jacob);
		r.addCustomer(mike);
		r.addCustomer(anna);
		r.addCustomer(emily);
		r.addCustomer(owen);
		r.addCustomer(sara);
		r.addCustomer(jose);
		r.addCustomer(chris);
		r.addCustomer(mike);
		r.addCustomer(sara);
		r.addCustomer(romana);
		r.addCustomer(phillip);
		r.addCustomer(alex);
		r.addCustomer(lauren);
		r.addCustomer(jenson);
		r.addCustomer(mishelle);
		r.addCustomer(anne);
		r.addCustomer(matt);
		r.addCustomer(jackie);
		r.remove(emily);
		r.remove(phillip);
		r.addCustomer(bonnie);
		r.remove(anna);
		r.addCustomer(pablo);
		r.remove(matt);
		r.addCustomer(zane);
		
		if (r.searchList(jacob) == 0) {
		    System.out.println(jacob + " is seated at a table.");
		}
		
		if (r.searchList(jenson) == 1) {
		    System.out.println(jenson + " is currently on the wait list.");
		}
	}

}
