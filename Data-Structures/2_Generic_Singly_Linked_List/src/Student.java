public class Student implements Comparable<Object>{
	private String studentName;
	private String studentID;
	public Student(String ID) {
		studentID = ID;
	 }

	public Student(Student s){
		this.studentName=s.studentName;
		this.studentID = s.studentID;
	  }

	public Student(String name, String ID) {
		studentName = name;
		studentID = ID;
	}

	public void setStudentName(String name)
	{
		studentName = name;
	}

	public String getStudentName() {
		return studentName;
	}

	public String getStudentID() {
		return studentID;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		Student s= (Student) obj;
		if(this.studentID.equals(s.studentID)) return true;
		return false;
	}

	@Override
	public String toString() {
		return "[" + studentName + ", " + studentID + "]";
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		Student another=(Student) o;
		return this.studentID.compareTo(another.studentID);
	}
}
