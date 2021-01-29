package last_working_bot;

public class IntCycler {
	public int data;
	public IntCycler last;
	public IntCycler next;
	public IntCycler(int data, IntCycler other) {  // adds new int to the cycle
		this.data = data;
		if (other!=null) {
			last = other.last;
			next = other;
			last.next = this;
			next.last = this;
		}
		else {
			last = this;
			next = this;
		}
	}
	public IntCycler pop() {
		if (next==this) {
			return null;
		}
		last.next = next;
		next.last = last;
		return next;
	}
}
