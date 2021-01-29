package last_working_bot;
import battlecode.common.*;

public class CombatInfo {
	public static int EC_UNIT_EQUIVALENT = 1000;
	public static int NEUTRAL_EC_UNIT_EQUIVALENT = 500;
	public static int TARGETTER_UNBLOCK_EQUIVALENT = 10;
	
	public static RobotController rc;
	public static int last_round_updated_empower_costs = -1;  // for computing self empowerment
	public static int n_1;
	public static int n_2;
	public static int n_4;
	public static int n_5;
	public static int n_8;
	public static int n_9;
	public static int damage_1;
	public static int damage_2;
	public static int damage_4;
	public static int damage_5;
	public static int damage_8;
	public static int damage_9;
	public static int kills_1;
	public static int kills_2;
	public static int kills_4;
	public static int kills_5;
	public static int kills_8;
	public static int kills_9;
	public static int costs_1;
	public static int costs_2;
	public static int costs_4;
	public static int costs_5;
	public static int costs_8;
	public static int costs_9;
	public static double gains_1;  // monetary gains are measured in conviction
	public static double gains_2;
	public static double gains_4;
	public static double gains_5;
	public static double gains_8;
	public static double gains_9;
	public static int optimal_empower_radius;
	public static double optimal_empower_gains;

	public static double inside_gains_1;  // for computing how to move to control another unit's empowerment
	public static double inside_gains_2;
	public static double inside_gains_4;
	public static double inside_gains_5;
	public static double inside_gains_8;
	public static double inside_gains_9;
	public static double outside_gains_1;
	public static double outside_gains_2;
	public static double outside_gains_4;
	public static double outside_gains_5;
	public static double outside_gains_8;
	public static double outside_gains_9;
	public static double[][] move_gains;

	public static void compute_self_empower_gains() {
		if (Info.round_num==last_round_updated_empower_costs) {return;}
		last_round_updated_empower_costs = Info.round_num;
		int[][] outcomes = compute_empower_gains(new RobotInfo(Info.id, Info.friendly, Info.type, Info.influence, Info.conviction, Info.loc), rc.senseNearbyRobots(9), null);
		n_1 = outcomes[0][0];
		n_2 = outcomes[0][1];
		n_4 = outcomes[0][2];
		n_5 = outcomes[0][3];
		n_8 = outcomes[0][4];
		n_9 = outcomes[0][5];
		kills_1 = outcomes[1][0];
		kills_2 = outcomes[1][1];
		kills_4 = outcomes[1][2];
		kills_5 = outcomes[1][3];
		kills_8 = outcomes[1][4];
		kills_9 = outcomes[1][5];
		costs_1 = outcomes[2][0];
		costs_2 = outcomes[2][1];
		costs_4 = outcomes[2][2];
		costs_5 = outcomes[2][3];
		costs_8 = outcomes[2][4];
		costs_9 = outcomes[2][5];
		gains_1 = kills_1*Info.unit_price+costs_1;
		gains_2 = kills_2*Info.unit_price+costs_2;
		gains_4 = kills_4*Info.unit_price+costs_4;
		gains_5 = kills_5*Info.unit_price+costs_5;
		gains_8 = kills_8*Info.unit_price+costs_8;
		gains_9 = kills_9*Info.unit_price+costs_9;
		optimal_empower_radius = 9; optimal_empower_gains = gains_9;
		if (gains_8>optimal_empower_gains) {optimal_empower_radius = 8; optimal_empower_gains = gains_8;}
		if (gains_5>optimal_empower_gains) {optimal_empower_radius = 5; optimal_empower_gains = gains_5;}
		if (gains_4>optimal_empower_gains) {optimal_empower_radius = 4; optimal_empower_gains = gains_4;}
		if (gains_2>optimal_empower_gains) {optimal_empower_radius = 2; optimal_empower_gains = gains_2;}
		if (gains_1>optimal_empower_gains) {optimal_empower_radius = 1; optimal_empower_gains = gains_1;}
		damage_1 = Math.max(0, (int)((Info.conviction-GameConstants.EMPOWER_TAX)*Info.empower_buff)/n_1);
		damage_2 = Math.max(0, (int)((Info.conviction-GameConstants.EMPOWER_TAX)*Info.empower_buff)/n_2);
		damage_4 = Math.max(0, (int)((Info.conviction-GameConstants.EMPOWER_TAX)*Info.empower_buff)/n_4);
		damage_5 = Math.max(0, (int)((Info.conviction-GameConstants.EMPOWER_TAX)*Info.empower_buff)/n_5);
		damage_8 = Math.max(0, (int)((Info.conviction-GameConstants.EMPOWER_TAX)*Info.empower_buff)/n_8);
		damage_9 = Math.max(0, (int)((Info.conviction-GameConstants.EMPOWER_TAX)*Info.empower_buff)/n_9);
	}
	
	public static double[][] compute_move_gains(RobotInfo empowering_robot) {  // computes the difference in optimal conviction gains for the empowering robot for every possible own robot move
		int[][] inside_empower_outcomes = compute_empower_gains(empowering_robot, Info.restricted_sensable_robots, new RobotInfo(Info.id, Info.friendly, Info.type, Info.influence, Info.conviction, empowering_robot.location.add(Direction.EAST)));
		int[][] outside_empower_outcomes = compute_empower_gains(empowering_robot, Info.restricted_sensable_robots, null);

		inside_gains_1 = inside_empower_outcomes[1][0]*Info.unit_price+inside_empower_outcomes[2][0];
		inside_gains_2 = inside_empower_outcomes[1][1]*Info.unit_price+inside_empower_outcomes[2][1];
		inside_gains_4 = inside_empower_outcomes[1][2]*Info.unit_price+inside_empower_outcomes[2][2];
		inside_gains_5 = inside_empower_outcomes[1][3]*Info.unit_price+inside_empower_outcomes[2][3];
		inside_gains_8 = inside_empower_outcomes[1][4]*Info.unit_price+inside_empower_outcomes[2][4];
		inside_gains_9 = inside_empower_outcomes[1][5]*Info.unit_price+inside_empower_outcomes[2][5];
		outside_gains_1 = outside_empower_outcomes[1][0]*Info.unit_price+outside_empower_outcomes[2][0];
		outside_gains_2 = outside_empower_outcomes[1][1]*Info.unit_price+outside_empower_outcomes[2][1];
		outside_gains_4 = outside_empower_outcomes[1][2]*Info.unit_price+outside_empower_outcomes[2][2];
		outside_gains_5 = outside_empower_outcomes[1][3]*Info.unit_price+outside_empower_outcomes[2][3];
		outside_gains_8 = outside_empower_outcomes[1][4]*Info.unit_price+outside_empower_outcomes[2][4];
		outside_gains_9 = outside_empower_outcomes[1][5]*Info.unit_price+outside_empower_outcomes[2][5];
		outside_gains_2 = Math.max(outside_gains_1, outside_gains_2);
		outside_gains_4 = Math.max(outside_gains_2, outside_gains_4);
		outside_gains_5 = Math.max(outside_gains_4, outside_gains_5);
		outside_gains_8 = Math.max(outside_gains_5, outside_gains_8);
		outside_gains_9 = Math.max(outside_gains_8, outside_gains_9);
		inside_gains_8 = Math.max(inside_gains_9, inside_gains_8);
		inside_gains_5 = Math.max(inside_gains_8, inside_gains_5);
		inside_gains_4 = Math.max(inside_gains_5, inside_gains_4);
		inside_gains_2 = Math.max(inside_gains_4, inside_gains_2);
		inside_gains_1 = Math.max(inside_gains_2, inside_gains_1);
		move_gains = new double[3][3];
		for (Direction dir:Direction.allDirections()) {
			switch (Info.loc.add(dir).distanceSquaredTo(empowering_robot.location)) {
			case 1: {move_gains[dir.dx+1][dir.dy+1] = inside_gains_1; break;}
			case 2: {move_gains[dir.dx+1][dir.dy+1] = Math.max(inside_gains_2, outside_gains_1); break;}
			case 4: {move_gains[dir.dx+1][dir.dy+1] = Math.max(inside_gains_4, outside_gains_2); break;}
			case 5: {move_gains[dir.dx+1][dir.dy+1] = Math.max(inside_gains_5, outside_gains_4); break;}
			case 8: {move_gains[dir.dx+1][dir.dy+1] = Math.max(inside_gains_8, outside_gains_5); break;}
			case 9: {move_gains[dir.dx+1][dir.dy+1] = Math.max(inside_gains_9, outside_gains_8); break;}
			default: {move_gains[dir.dx+1][dir.dy+1] = outside_gains_9; break;}
			}
		}
		for (Direction dir:Direction.allDirections()) {
			move_gains[dir.dx+1][dir.dy+1] = Math.max(0, move_gains[dir.dx+1][dir.dy+1]);
		}
		if (empowering_robot.team==Info.enemy) {
			for (Direction dir:Direction.allDirections()) {
				move_gains[dir.dx+1][dir.dy+1] = -move_gains[dir.dx+1][dir.dy+1];
			}
		}
		return move_gains;
	}
	
	public static int[][] compute_empower_gains(RobotInfo empowering_robot, RobotInfo[] considered_robots, RobotInfo additional_robot) {
		if (((int)(empowering_robot.conviction*Info.empower_buff))-GameConstants.EMPOWER_TAX<=0) {
			return new int[][] {
				{1, 1, 1, 1, 1, 1},  // these will always be greater than 0, for division by 0 reasons
				{0, 0, 0, 0, 0, 0},
				{-empowering_robot.conviction, -empowering_robot.conviction, -empowering_robot.conviction, -empowering_robot.conviction, -empowering_robot.conviction, -empowering_robot.conviction}
			};
		}
		MapLocation empower_location = empowering_robot.location;
		int n_1 = 0;
		int n_2 = 0;
		int n_4 = 0;
		int n_5 = 0;
		int n_8 = 0;
		int n_9 = 0;
		for (int i=considered_robots.length; --i>=0;) {
			switch (empower_location.distanceSquaredTo(considered_robots[i].location)) {
			case 1: {n_1++; break;}
			case 2: {n_2++; break;}
			case 4: {n_4++; break;}
			case 5: {n_5++; break;}
			case 8: {n_8++; break;}
			case 9: {n_9++; break;}
			}
		}
		if (additional_robot!=null) {  // copy-pasted from above
			switch (empower_location.distanceSquaredTo(additional_robot.location)) {
			case 1: {n_1++; break;}
			case 2: {n_2++; break;}
			case 4: {n_4++; break;}
			case 5: {n_5++; break;}
			case 8: {n_8++; break;}
			case 9: {n_9++; break;}
			}
		}
		n_2 += n_1;
		n_4 += n_2;
		n_5 += n_4;
		n_8 += n_5;
		n_9 += n_8;
		n_1 = Math.max(n_1, 1);  // prevent division by zero
		n_2 = Math.max(n_2, 1);
		n_4 = Math.max(n_4, 1);
		n_5 = Math.max(n_5, 1);
		n_8 = Math.max(n_8, 1);
		n_9 = Math.max(n_9, 1);
		double buff = Info.empower_buff;
		if (empowering_robot.team==Info.enemy) {buff = Info.enemy_empower_buff;}
		int damage_1 = Math.max(0, (int)((empowering_robot.conviction-GameConstants.EMPOWER_TAX)*buff)/n_1);
		int damage_2 = Math.max(0, (int)((empowering_robot.conviction-GameConstants.EMPOWER_TAX)*buff)/n_2);
		int damage_4 = Math.max(0, (int)((empowering_robot.conviction-GameConstants.EMPOWER_TAX)*buff)/n_4);
		int damage_5 = Math.max(0, (int)((empowering_robot.conviction-GameConstants.EMPOWER_TAX)*buff)/n_5);
		int damage_8 = Math.max(0, (int)((empowering_robot.conviction-GameConstants.EMPOWER_TAX)*buff)/n_8);
		int damage_9 = Math.max(0, (int)((empowering_robot.conviction-GameConstants.EMPOWER_TAX)*buff)/n_9);
		int kills_1 = -1;
		int kills_2 = -1;
		int kills_4 = -1;
		int kills_5 = -1;
		int kills_8 = -1;
		int kills_9 = -1;
		int costs_1 = -empowering_robot.conviction;
		int costs_2 = -empowering_robot.conviction;
		int costs_4 = -empowering_robot.conviction;
		int costs_5 = -empowering_robot.conviction;
		int costs_8 = -empowering_robot.conviction;
		int costs_9 = -empowering_robot.conviction;
		for (int i=considered_robots.length; --i>=0;) {
			if (Clock.getBytecodesLeft()<5000) {break;}
			RobotInfo robot = considered_robots[i];
			if (empowering_robot.team==robot.team) {
				switch (robot.type) {
				case ENLIGHTENMENT_CENTER: {
					switch (empower_location.distanceSquaredTo(robot.location)) {
					case 1: {costs_1 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_1);
					costs_2 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_2);
					costs_4 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_4);
					costs_5 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_5);
					costs_8 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_8);
					costs_9 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_9); break;}
					case 2: {costs_2 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_2);
					costs_4 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_4);
					costs_5 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_5);
					costs_8 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_8);
					costs_9 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_9); break;}
					case 4: {costs_4 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_4);
					costs_5 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_5);
					costs_8 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_8);
					costs_9 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_9); break;}
					case 5: {costs_5 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_5);
					costs_8 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_8);
					costs_9 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_9); break;}
					case 8: {costs_8 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_8);
					costs_9 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_9); break;}
					case 9: {costs_9 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_9); break;}
					}
					break;}
				case POLITICIAN: {
					if (robot.influence==robot.conviction) {break;} 
					int influence = robot.influence;
					int conviction = robot.conviction;
					switch (empower_location.distanceSquaredTo(robot.location)) {
					case 1: {costs_1 += Math.min(damage_1, influence-conviction);
					costs_2 += Math.min(damage_2, influence-conviction);
					costs_4 += Math.min(damage_4, influence-conviction);
					costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 2: {costs_2 += Math.min(damage_2, influence-conviction);
					costs_4 += Math.min(damage_4, influence-conviction);
					costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 4: {costs_4 += Math.min(damage_4, influence-conviction);
					costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 5: {costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 8: {costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 9: {costs_9 += Math.min(damage_9, influence-conviction); break;}
					}
					break;}
				case MUCKRAKER: {
					if (robot.influence==robot.conviction) {break;} 
					int influence = robot.influence;
					int conviction = robot.conviction;
					switch (empower_location.distanceSquaredTo(robot.location)) {
					case 1: {costs_1 += Math.min(damage_1, influence-conviction);
					costs_2 += Math.min(damage_2, influence-conviction);
					costs_4 += Math.min(damage_4, influence-conviction);
					costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 2: {costs_2 += Math.min(damage_2, influence-conviction);
					costs_4 += Math.min(damage_4, influence-conviction);
					costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 4: {costs_4 += Math.min(damage_4, influence-conviction);
					costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 5: {costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 8: {costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 9: {costs_9 += Math.min(damage_9, influence-conviction); break;}
					}
					break;}
				}
			}
			else if (robot.team==Team.NEUTRAL) {
				int conviction = robot.conviction;
				switch (empower_location.distanceSquaredTo(robot.location)) {
				case 1: {kills_1 += (damage_1>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_1 += Math.max(0, Math.min(damage_1-conviction, conviction + (int)((damage_1-conviction)/buff)));
				kills_2 += (damage_2>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_2 += Math.max(0, Math.min(damage_2-conviction, conviction + (int)((damage_2-conviction)/buff)));
				kills_4 += (damage_4>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_4 += Math.max(0, Math.min(damage_4-conviction, conviction + (int)((damage_4-conviction)/buff)));
				kills_5 += (damage_5>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
				kills_8 += (damage_8>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
				kills_9 += (damage_9>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
				case 2: {kills_2 += (damage_2>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_2 += Math.max(0, Math.min(damage_2-conviction, conviction + (int)((damage_2-conviction)/buff)));
				kills_4 += (damage_4>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_4 += Math.max(0, Math.min(damage_4-conviction, conviction + (int)((damage_4-conviction)/buff)));
				kills_5 += (damage_5>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
				kills_8 += (damage_8>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
				kills_9 += (damage_9>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
				case 4: {kills_4 += (damage_4>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_4 += Math.max(0, Math.min(damage_4-conviction, conviction + (int)((damage_4-conviction)/buff)));
				kills_5 += (damage_5>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
				kills_8 += (damage_8>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
				kills_9 += (damage_9>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
				case 5: {kills_5 += (damage_5>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
				kills_8 += (damage_8>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
				kills_9 += (damage_9>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
				case 8: {kills_8 += (damage_8>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
				kills_9 += (damage_9>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
				case 9: {kills_9 += (damage_9>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
				}
			}
			else {
				int kills_equivalent = (robot.type==RobotType.POLITICIAN)? 2 : 1;
				switch (robot.type) {
				case ENLIGHTENMENT_CENTER: {
					int conviction = robot.conviction;
					switch (empower_location.distanceSquaredTo(robot.location)) {
					case 1: {kills_1 += (damage_1>conviction)?EC_UNIT_EQUIVALENT:0; costs_1 += Math.max(0, Math.min(damage_1-conviction, conviction + (int)((damage_1-conviction)/buff)));
					kills_2 += (damage_2>conviction)?EC_UNIT_EQUIVALENT:0; costs_2 += Math.max(0, Math.min(damage_2-conviction, conviction + (int)((damage_2-conviction)/buff)));
					kills_4 += (damage_4>conviction)?EC_UNIT_EQUIVALENT:0; costs_4 += Math.max(0, Math.min(damage_4-conviction, conviction + (int)((damage_4-conviction)/buff)));
					kills_5 += (damage_5>conviction)?EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
					kills_8 += (damage_8>conviction)?EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
					kills_9 += (damage_9>conviction)?EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
					case 2: {kills_2 += (damage_2>conviction)?EC_UNIT_EQUIVALENT:0; costs_2 += Math.max(0, Math.min(damage_2-conviction, conviction + (int)((damage_2-conviction)/buff)));
					kills_4 += (damage_4>conviction)?EC_UNIT_EQUIVALENT:0; costs_4 += Math.max(0, Math.min(damage_4-conviction, conviction + (int)((damage_4-conviction)/buff)));
					kills_5 += (damage_5>conviction)?EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
					kills_8 += (damage_8>conviction)?EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
					kills_9 += (damage_9>conviction)?EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
					case 4: {kills_4 += (damage_4>conviction)?EC_UNIT_EQUIVALENT:0; costs_4 += Math.max(0, Math.min(damage_4-conviction, conviction + (int)((damage_4-conviction)/buff)));
					kills_5 += (damage_5>conviction)?EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
					kills_8 += (damage_8>conviction)?EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
					kills_9 += (damage_9>conviction)?EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
					case 5: {kills_5 += (damage_5>conviction)?EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
					kills_8 += (damage_8>conviction)?EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
					kills_9 += (damage_9>conviction)?EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
					case 8: {kills_8 += (damage_8>conviction)?EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
					kills_9 += (damage_9>conviction)?EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
					case 9: {kills_9 += (damage_9>conviction)?EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
					}
					break;}
				case POLITICIAN: {
					for (int j=Info.n_friendly_ecs; --j>=0;) {
						if (robot.location.isWithinDistanceSquared(Info.friendly_ecs[j].location, 2)) {kills_equivalent++;}
					}
					for (int j=Info.n_enemy_ecs; --j>=0;) {
						if (robot.location.isWithinDistanceSquared(Info.enemy_ecs[j].location, 2)) {kills_equivalent++;}
					}
					for (int j=Info.n_targetters; --j>=0;) {
						if (robot.location.isWithinDistanceSquared(Info.targetters[j].location, 1)) {kills_equivalent+=TARGETTER_UNBLOCK_EQUIVALENT;}
					}
					int influence = robot.influence;
					int conviction = robot.conviction;
					switch (empower_location.distanceSquaredTo(robot.location)) {
					case 1: {kills_1 += (damage_1>conviction)?kills_equivalent:0; costs_1 += Math.min(damage_1, influence+conviction);
					 kills_2 += (damage_2>conviction)?kills_equivalent:0; costs_2 += Math.min(damage_2, influence+conviction);
					 kills_4 += (damage_4>conviction)?kills_equivalent:0; costs_4 += Math.min(damage_4, influence+conviction);
					 kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, influence+conviction);
					 kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, influence+conviction);
					 kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, influence+conviction); break;}
					case 2: {kills_2 += (damage_2>conviction)?kills_equivalent:0; costs_2 += Math.min(damage_2, influence+conviction);
					 kills_4 += (damage_4>conviction)?kills_equivalent:0; costs_4 += Math.min(damage_4, influence+conviction);
					 kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, influence+conviction);
					 kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, influence+conviction);
					 kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, influence+conviction); break;}
					case 4: {kills_4 += (damage_4>conviction)?kills_equivalent:0; costs_4 += Math.min(damage_4, influence+conviction);
					 kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, influence+conviction);
					 kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, influence+conviction);
					 kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, influence+conviction); break;}
					case 5: {kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, influence+conviction);
					 kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, influence+conviction);
					 kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, influence+conviction); break;}
					case 8: {kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, influence+conviction);
					 kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, influence+conviction); break;}
					case 9: {kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, influence+conviction); break;}
					}
					break;}
				case MUCKRAKER: {
					if (empowering_robot.team==Info.friendly) {
						for (int j=Info.n_friendly_ecs; --j>=0;) {
							if (robot.location.isWithinDistanceSquared(Info.friendly_ecs[j].location, 2)) {kills_equivalent++;}
						}
						for (int j=Info.n_enemy_ecs; --j>=0;) {
							if (robot.location.isWithinDistanceSquared(Info.enemy_ecs[j].location, 2)) {kills_equivalent++;}  //--
						}
					}
					else if (empowering_robot.team==Info.enemy) {
						for (int j=Info.n_friendly_ecs; --j>=0;) {
							if (robot.location.isWithinDistanceSquared(Info.friendly_ecs[j].location, 2)) {kills_equivalent++;}  //--
						}
						for (int j=Info.n_enemy_ecs; --j>=0;) {
							if (robot.location.isWithinDistanceSquared(Info.enemy_ecs[j].location, 2)) {kills_equivalent++;}
						}
					}
					for (int j=Info.n_targetters; --j>=0;) {
						if (robot.location.isWithinDistanceSquared(Info.targetters[j].location, 1)) {kills_equivalent+=TARGETTER_UNBLOCK_EQUIVALENT;}
					}
					int conviction = robot.conviction;
					switch (empower_location.distanceSquaredTo(robot.location)) {
					case 1: {kills_1 += (damage_1>conviction)?kills_equivalent:0; costs_1 += Math.min(damage_1, conviction);
					kills_2 += (damage_2>conviction)?kills_equivalent:0; costs_2 += Math.min(damage_2, conviction);
					kills_4 += (damage_4>conviction)?kills_equivalent:0; costs_4 += Math.min(damage_4, conviction);
					kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, conviction);
					kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, conviction);
					kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, conviction); break;}
					case 2: {kills_2 += (damage_2>conviction)?kills_equivalent:0; costs_2 += Math.min(damage_2, conviction);
					kills_4 += (damage_4>conviction)?kills_equivalent:0; costs_4 += Math.min(damage_4, conviction);
					kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, conviction);
					kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, conviction);
					kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, conviction); break;}
					case 4: {kills_4 += (damage_4>conviction)?kills_equivalent:0; costs_4 += Math.min(damage_4, conviction);
					kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, conviction);
					kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, conviction);
					kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, conviction); break;}
					case 5: {kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, conviction);
					kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, conviction);
					kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, conviction); break;}
					case 8: {kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, conviction);
					kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, conviction); break;}
					case 9: {kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, conviction); break;}
					}
					break;}
				}
			}
		}
		if (additional_robot!=null) {  // copy-pasted from above
			RobotInfo robot = additional_robot;
			if (empowering_robot.team==robot.team) {
				switch (robot.type) {
				case ENLIGHTENMENT_CENTER: {
					switch (empower_location.distanceSquaredTo(robot.location)) {
					case 1: {costs_1 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_1);
					costs_2 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_2);
					costs_4 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_4);
					costs_5 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_5);
					costs_8 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_8);
					costs_9 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_9); break;}
					case 2: {costs_2 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_2);
					costs_4 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_4);
					costs_5 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_5);
					costs_8 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_8);
					costs_9 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_9); break;}
					case 4: {costs_4 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_4);
					costs_5 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_5);
					costs_8 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_8);
					costs_9 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_9); break;}
					case 5: {costs_5 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_5);
					costs_8 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_8);
					costs_9 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_9); break;}
					case 8: {costs_8 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_8);
					costs_9 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_9); break;}
					case 9: {costs_9 += Math.max(0, (int)(empowering_robot.conviction-GameConstants.EMPOWER_TAX)/n_9); break;}
					}
					break;}
				case POLITICIAN: {
					if (robot.influence==robot.conviction) {break;} 
					int influence = robot.influence;
					int conviction = robot.conviction;
					switch (empower_location.distanceSquaredTo(robot.location)) {
					case 1: {costs_1 += Math.min(damage_1, influence-conviction);
					costs_2 += Math.min(damage_2, influence-conviction);
					costs_4 += Math.min(damage_4, influence-conviction);
					costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 2: {costs_2 += Math.min(damage_2, influence-conviction);
					costs_4 += Math.min(damage_4, influence-conviction);
					costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 4: {costs_4 += Math.min(damage_4, influence-conviction);
					costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 5: {costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 8: {costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 9: {costs_9 += Math.min(damage_9, influence-conviction); break;}
					}
					break;}
				case MUCKRAKER: {
					if (robot.influence==robot.conviction) {break;} 
					int influence = robot.influence;
					int conviction = robot.conviction;
					switch (empower_location.distanceSquaredTo(robot.location)) {
					case 1: {costs_1 += Math.min(damage_1, influence-conviction);
					costs_2 += Math.min(damage_2, influence-conviction);
					costs_4 += Math.min(damage_4, influence-conviction);
					costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 2: {costs_2 += Math.min(damage_2, influence-conviction);
					costs_4 += Math.min(damage_4, influence-conviction);
					costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 4: {costs_4 += Math.min(damage_4, influence-conviction);
					costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 5: {costs_5 += Math.min(damage_5, influence-conviction);
					costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 8: {costs_8 += Math.min(damage_8, influence-conviction);
					costs_9 += Math.min(damage_9, influence-conviction); break;}
					case 9: {costs_9 += Math.min(damage_9, influence-conviction); break;}
					}
					break;}
				}
			}
			else if (robot.team==Team.NEUTRAL) {
				int conviction = robot.conviction;
				switch (empower_location.distanceSquaredTo(robot.location)) {
				case 1: {kills_1 += (damage_1>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_1 += Math.max(0, Math.min(damage_1-conviction, conviction + (int)((damage_1-conviction)/buff)));
				kills_2 += (damage_2>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_2 += Math.max(0, Math.min(damage_2-conviction, conviction + (int)((damage_2-conviction)/buff)));
				kills_4 += (damage_4>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_4 += Math.max(0, Math.min(damage_4-conviction, conviction + (int)((damage_4-conviction)/buff)));
				kills_5 += (damage_5>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
				kills_8 += (damage_8>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
				kills_9 += (damage_9>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
				case 2: {kills_2 += (damage_2>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_2 += Math.max(0, Math.min(damage_2-conviction, conviction + (int)((damage_2-conviction)/buff)));
				kills_4 += (damage_4>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_4 += Math.max(0, Math.min(damage_4-conviction, conviction + (int)((damage_4-conviction)/buff)));
				kills_5 += (damage_5>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
				kills_8 += (damage_8>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
				kills_9 += (damage_9>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
				case 4: {kills_4 += (damage_4>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_4 += Math.max(0, Math.min(damage_4-conviction, conviction + (int)((damage_4-conviction)/buff)));
				kills_5 += (damage_5>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
				kills_8 += (damage_8>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
				kills_9 += (damage_9>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
				case 5: {kills_5 += (damage_5>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
				kills_8 += (damage_8>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
				kills_9 += (damage_9>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
				case 8: {kills_8 += (damage_8>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
				kills_9 += (damage_9>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
				case 9: {kills_9 += (damage_9>conviction)?NEUTRAL_EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
				}
			}
			else {

				int kills_equivalent = (robot.type==RobotType.POLITICIAN)? 2 : 1;
				for (int j=Info.n_friendly_ecs; --j>=0;) {
					if (robot.location.isWithinDistanceSquared(Info.friendly_ecs[j].location, 2)) {kills_equivalent++;}
				}
				for (int j=Info.n_enemy_ecs; --j>=0;) {
					if (robot.location.isWithinDistanceSquared(Info.enemy_ecs[j].location, 2)) {kills_equivalent++;}
				}
				switch (robot.type) {
				case ENLIGHTENMENT_CENTER: {
					int conviction = robot.conviction;
					switch (empower_location.distanceSquaredTo(robot.location)) {
					case 1: {kills_1 += (damage_1>conviction)?EC_UNIT_EQUIVALENT:0; costs_1 += Math.max(0, Math.min(damage_1-conviction, conviction + (int)((damage_1-conviction)/buff)));
					kills_2 += (damage_2>conviction)?EC_UNIT_EQUIVALENT:0; costs_2 += Math.max(0, Math.min(damage_2-conviction, conviction + (int)((damage_2-conviction)/buff)));
					kills_4 += (damage_4>conviction)?EC_UNIT_EQUIVALENT:0; costs_4 += Math.max(0, Math.min(damage_4-conviction, conviction + (int)((damage_4-conviction)/buff)));
					kills_5 += (damage_5>conviction)?EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
					kills_8 += (damage_8>conviction)?EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
					kills_9 += (damage_9>conviction)?EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
					case 2: {kills_2 += (damage_2>conviction)?EC_UNIT_EQUIVALENT:0; costs_2 += Math.max(0, Math.min(damage_2-conviction, conviction + (int)((damage_2-conviction)/buff)));
					kills_4 += (damage_4>conviction)?EC_UNIT_EQUIVALENT:0; costs_4 += Math.max(0, Math.min(damage_4-conviction, conviction + (int)((damage_4-conviction)/buff)));
					kills_5 += (damage_5>conviction)?EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
					kills_8 += (damage_8>conviction)?EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
					kills_9 += (damage_9>conviction)?EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
					case 4: {kills_4 += (damage_4>conviction)?EC_UNIT_EQUIVALENT:0; costs_4 += Math.max(0, Math.min(damage_4-conviction, conviction + (int)((damage_4-conviction)/buff)));
					kills_5 += (damage_5>conviction)?EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
					kills_8 += (damage_8>conviction)?EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
					kills_9 += (damage_9>conviction)?EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
					case 5: {kills_5 += (damage_5>conviction)?EC_UNIT_EQUIVALENT:0; costs_5 += Math.max(0, Math.min(damage_5-conviction, conviction + (int)((damage_5-conviction)/buff)));
					kills_8 += (damage_8>conviction)?EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
					kills_9 += (damage_9>conviction)?EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
					case 8: {kills_8 += (damage_8>conviction)?EC_UNIT_EQUIVALENT:0; costs_8 += Math.max(0, Math.min(damage_8-conviction, conviction + (int)((damage_8-conviction)/buff)));
					kills_9 += (damage_9>conviction)?EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
					case 9: {kills_9 += (damage_9>conviction)?EC_UNIT_EQUIVALENT:0; costs_9 += Math.max(0, Math.min(damage_9-conviction, conviction + (int)((damage_9-conviction)/buff))); break;}
					}
					break;}
				case POLITICIAN: {
					int influence = robot.influence;
					int conviction = robot.conviction;
					switch (empower_location.distanceSquaredTo(robot.location)) {
					case 1: {kills_1 += (damage_1>conviction)?kills_equivalent:0; costs_1 += Math.min(damage_1, influence+conviction);
					 kills_2 += (damage_2>conviction)?kills_equivalent:0; costs_2 += Math.min(damage_2, influence+conviction);
					 kills_4 += (damage_4>conviction)?kills_equivalent:0; costs_4 += Math.min(damage_4, influence+conviction);
					 kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, influence+conviction);
					 kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, influence+conviction);
					 kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, influence+conviction); break;}
					case 2: {kills_2 += (damage_2>conviction)?kills_equivalent:0; costs_2 += Math.min(damage_2, influence+conviction);
					 kills_4 += (damage_4>conviction)?kills_equivalent:0; costs_4 += Math.min(damage_4, influence+conviction);
					 kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, influence+conviction);
					 kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, influence+conviction);
					 kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, influence+conviction); break;}
					case 4: {kills_4 += (damage_4>conviction)?kills_equivalent:0; costs_4 += Math.min(damage_4, influence+conviction);
					 kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, influence+conviction);
					 kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, influence+conviction);
					 kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, influence+conviction); break;}
					case 5: {kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, influence+conviction);
					 kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, influence+conviction);
					 kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, influence+conviction); break;}
					case 8: {kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, influence+conviction);
					 kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, influence+conviction); break;}
					case 9: {kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, influence+conviction); break;}
					}
					break;}
				case MUCKRAKER: {
					int conviction = robot.conviction;
					switch (empower_location.distanceSquaredTo(robot.location)) {
					case 1: {kills_1 += (damage_1>conviction)?kills_equivalent:0; costs_1 += Math.min(damage_1, conviction);
					kills_2 += (damage_2>conviction)?kills_equivalent:0; costs_2 += Math.min(damage_2, conviction);
					kills_4 += (damage_4>conviction)?kills_equivalent:0; costs_4 += Math.min(damage_4, conviction);
					kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, conviction);
					kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, conviction);
					kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, conviction); break;}
					case 2: {kills_2 += (damage_2>conviction)?kills_equivalent:0; costs_2 += Math.min(damage_2, conviction);
					kills_4 += (damage_4>conviction)?kills_equivalent:0; costs_4 += Math.min(damage_4, conviction);
					kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, conviction);
					kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, conviction);
					kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, conviction); break;}
					case 4: {kills_4 += (damage_4>conviction)?kills_equivalent:0; costs_4 += Math.min(damage_4, conviction);
					kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, conviction);
					kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, conviction);
					kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, conviction); break;}
					case 5: {kills_5 += (damage_5>conviction)?kills_equivalent:0; costs_5 += Math.min(damage_5, conviction);
					kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, conviction);
					kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, conviction); break;}
					case 8: {kills_8 += (damage_8>conviction)?kills_equivalent:0; costs_8 += Math.min(damage_8, conviction);
					kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, conviction); break;}
					case 9: {kills_9 += (damage_9>conviction)?kills_equivalent:0; costs_9 += Math.min(damage_9, conviction); break;}
					}
					break;}
				}
			}
		}
		return new int[][] {
			{n_1, n_2, n_4, n_5, n_8, n_9},  // these will always be greater than 0, for division by 0 reasons
			{kills_1, kills_2, kills_4, kills_5, kills_8, kills_9},
			{costs_1, costs_2, costs_4, costs_5, costs_8, costs_9}
		};
	}
}
