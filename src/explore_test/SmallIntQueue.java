package explore_test;

public class SmallIntQueue {
	public int[] data;
	public boolean[] present;
	public SmallIntQueue(int size) {
		data = new int[size];
		present = new boolean[size];
	}
}
