package micro;
import battlecode.common.*;

public class Role {
	public static RobotController rc;
	public static boolean is_relay_chain = false;
	
	public static void attach_to_relay_chain() throws GameActionException {
		if (!is_relay_chain) {
			is_relay_chain = true;
			RelayChain.source_dist = 32;
			RelayChain.target_dist = 191.99;
			RelayChain.update();
		}
	}
	
	public static void detach_from_relay_chain() throws GameActionException {
		if (is_relay_chain) {
			is_relay_chain = false;
		}
	}
}
