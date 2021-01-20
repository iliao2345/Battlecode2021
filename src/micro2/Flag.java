package micro2;
import battlecode.common.*;

public class Flag {
	public static RobotController rc;
	public static int[] bits = new int[] {24};
	public static int[] values = new int[] {0};
	
	public static void set() {
		if (Info.type==RobotType.ENLIGHTENMENT_CENTER) {
			int weakest_ec_x = 0;
			int weakest_ec_y = 0;
			if (ECInfo.targetted_loc!=null) {
				weakest_ec_x = ECInfo.targetted_loc.x;
				weakest_ec_y = ECInfo.targetted_loc.y;
			}
			bits = new int[] {1, 1, 4, 1, 6, 6, 5};
			values = new int[] {
					ECInfo.guard_flag?1:0,
					ECInfo.exterminate_flag?1:0,
					0,
					(ECInfo.make_targetter_timer>0)?1:0,
					(weakest_ec_x%128)/2,
					(weakest_ec_y%128)/2,
					Math.min(31, Math.max(0, (int)(4*Math.log(ECInfo.unit_price/4.0+1))))
			};
		}
		else if (Info.type==RobotType.POLITICIAN || Info.type==RobotType.MUCKRAKER) {
			if (Role.is_relay_chain) {
				bits = new int[] {1, 6, 5, 8, 3, 1};
				values = new int[] {
						1,
						0,
						RelayChain.source_dist,
						(int)(RelayChain.target_dist),
						RelayChain.muckraker_warning_level,
						(Info.crowdedness>0.5)?1:0
				};
			}
			else if (Role.is_guard) {
				int muckraker_conviction = 0;
				if (Guard.closest_enemy_muckraker!=null) {
					muckraker_conviction = Math.min(15, (int)Math.ceil(4*Math.log(Guard.closest_enemy_muckraker.conviction/4.0+1)));
				}
				bits = new int[] {2, 17, 4, 1};
				values = new int[] {
						1,
						0,
						muckraker_conviction,
						0
				};
			}
			else if (Role.is_burier) {
				bits = new int[] {3, 6, 6, 4, 4, 1};
				values = new int[] {
						1,
						(Burier.target_ec.location.x%128)/2,
						(Burier.target_ec.location.y%128)/2,
						Math.min(15, (int)Math.ceil(2*Math.log(Math.max(0, Burier.sampled_politician_conviction - 10)/2.0+1))),
						Math.min(15, (int)Math.ceil(2*Math.log(Burier.target_ec_kill_conviction/10.0+1))),
						Burier.need_support?1:0
				};
			}
			else if (Role.is_targetter) {
				bits = new int[] {4, 20};
				values = new int[] {
						1,
						0
				};
			}
			else if (Role.is_exterminator) {
				bits = new int[] {5, 14, 5};
				values = new int[] {
						1,
						0,
						Exterminator.target_dist
				};
			}
			else {
				bits = new int[] {24};
				values = new int[] {
						0
				};
			}
		}
		else if (Info.type==RobotType.SLANDERER) {
			bits = new int[] {23, 1};
			values = new int[] {
					0,
					1
			};
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
