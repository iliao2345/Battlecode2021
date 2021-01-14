package micro;
import battlecode.common.*;

public class Flag {
	public static RobotController rc;
	public static int[] bits = new int[] {24};
	public static int[] values = new int[] {0};
	
	public static void set() {
		if (Info.type==RobotType.ENLIGHTENMENT_CENTER) {
			bits = new int[] {19, 5};
			values = new int[] {0, Math.min(31, Math.max(0, (int)(10*Math.log(ECInfo.unit_price/10))))};
		}
		if (Info.type==RobotType.POLITICIAN || Info.type==RobotType.MUCKRAKER) {
			if (Role.is_relay_chain) {
				bits = new int[] {1, 8, 5, 6, 4};
				values = new int[] {1, 0, RelayChain.source_dist, (int)(RelayChain.target_dist/3), (int)(((16*RelayChain.target_angle/Math.PI/2)+0.5)%16)};
			}
			else {
				bits = new int[] {24};
				values = new int[] {0};
			}
		}
	}
	public static void display() throws GameActionException {
		rc.setFlag(encode(bits, values));
		bits = new int[] {24};
		values = new int[] {0};
	}
	public static int encode(int[] bits, int[] values) {
		int flag = 0;
		for (int i=0; i<bits.length; i++) {
			flag = (flag<<bits[i]) + values[i];
		}
		return flag;
	}
}
