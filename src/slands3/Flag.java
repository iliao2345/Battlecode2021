package slands3;
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
			bits = new int[] {1, 1, 3, 1, 1, 6, 6, 5};
			values = new int[] {
					ECInfo.retain_guards_flag?1:0,
					ECInfo.exterminate_flag?1:0,
					0,
					ECInfo.build_role,
					(ECInfo.info_transfer_timer>0)?1:0,
					(ECInfo.targetted_loc.x%128)/2,
					(ECInfo.targetted_loc.y%128)/2,
					Math.min(31, Math.max(0, (int)(4*Math.log(ECInfo.unit_price/4.0+1))))
			};
		}
		else if (Info.type==RobotType.POLITICIAN || Info.type==RobotType.MUCKRAKER) {
			if (Role.is_relay_chain) {
				bits = new int[] {1, 5, 5, 7, 3, 3};
				values = new int[] {
						1,
						RelayChain.signal_loc.x%128/4,
						RelayChain.signal_loc.y%128/4,
						(Info.n_enemy_muckrakers>0)? RelayChain.signal_id_127:127,
						RelayChain.signal_size,
						RelayChain.pressure
				};
			}
			else if (Role.is_guard) {
				int muckraker_conviction = 0;
				if (Guard.target_muckraker!=null) {
					muckraker_conviction = Math.min(15, (int)Math.ceil(4*Math.log(Guard.target_muckraker.conviction/4.0+1)));
				}
				bits = new int[] {2, 14, 3, 4, 1};
				values = new int[] {
						1,
						0,
						Guard.defense_layer,
						muckraker_conviction,
						Guard.reached_outpost?1:0
				};
			}
			else if (Role.is_burier) {
				bits = new int[] {3, 2, 6, 6, 6, 1};
				values = new int[] {
						1,
						0,
						Math.min(63, (int)Math.ceil(8*Math.log(Burier.target_ec_kill_conviction/10.0+1))),
						(Burier.target_ec.location.x%128)/2,
						(Burier.target_ec.location.y%128)/2,
						Burier.need_support?1:0
				};
			}
			else if (Role.is_targetter) {
				bits = new int[] {4, 1, 19};
				values = new int[] {
						1,
						Targetter.move_out_of_the_way_flag?1:0,
						0
				};
			}
			else if (Role.is_exterminator) {
				bits = new int[] {5, 14, 4, 1};
				values = new int[] {
						1,
						0,
						Exterminator.pressure,
						0
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
			bits = new int[] {6, 3, 6, 6, 3};
			values = new int[] {
					1,
					(int)Math.ceil((Slanderer.target_ec_kill_conviction-0.5)/72.0),
					(Slanderer.target_ec.location.x%128)/2,
					(Slanderer.target_ec.location.y%128)/2,
					Math.min(7, Slanderer.pressure)
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
