package membrane3;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameActionExceptionType;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class Combat {
	public static RobotController rc;
	
	public static int compute_optimal_empower_radius() {
		// 1 2 4 5 8 9
		int n_1 = 0;
		int n_2 = 0;
		int n_4 = 0;
		int n_5 = 0;
		int n_8 = 0;
		int n_9 = 0;
		for (int i=0; i<Info.sensable_robots.length; i++) {
			switch (Info.loc.distanceSquaredTo(Info.sensable_robots[i].location)) {
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
		int damage_1 = (Info.conviction-GameConstants.EMPOWER_TAX)/n_1;
		int damage_2 = (Info.conviction-GameConstants.EMPOWER_TAX)/n_2;
		int damage_4 = (Info.conviction-GameConstants.EMPOWER_TAX)/n_4;
		int damage_5 = (Info.conviction-GameConstants.EMPOWER_TAX)/n_5;
		int damage_8 = (Info.conviction-GameConstants.EMPOWER_TAX)/n_8;
		int damage_9 = (Info.conviction-GameConstants.EMPOWER_TAX)/n_9;
		int kills_1 = -1;
		int kills_2 = -1;
		int kills_4 = -1;
		int kills_5 = -1;
		int kills_8 = -1;
		int kills_9 = -1;
		int costs_1 = -GameConstants.EMPOWER_TAX;
		int costs_2 = -GameConstants.EMPOWER_TAX;
		int costs_4 = -GameConstants.EMPOWER_TAX;
		int costs_5 = -GameConstants.EMPOWER_TAX;
		int costs_8 = -GameConstants.EMPOWER_TAX;
		int costs_9 = -GameConstants.EMPOWER_TAX;
		for (int i=0; i<Info.friendly_ecs.length; i++) {
			switch (Info.loc.distanceSquaredTo(Info.friendly_ecs[i].location)) {
			case 1: {costs_1 += damage_1; break;}
			case 2: {costs_2 += damage_2; break;}
			case 4: {costs_4 += damage_4; break;}
			case 5: {costs_5 += damage_5; break;}
			case 8: {costs_8 += damage_8; break;}
			case 9: {costs_9 += damage_9; break;}
			}
		}
		for (int i=0; i<Info.friendly_politicians.length; i++) {
			int influence = Info.friendly_politicians[i].influence;
			int conviction = Info.friendly_politicians[i].conviction;
			switch (Info.loc.distanceSquaredTo(Info.friendly_politicians[i].location)) {
			case 1: {costs_1 += Math.max(damage_1, influence-conviction);
			costs_2 += Math.max(damage_2, influence-conviction);
			costs_4 += Math.max(damage_4, influence-conviction);
			costs_5 += Math.max(damage_5, influence-conviction);
			costs_8 += Math.max(damage_8, influence-conviction);
			costs_9 += Math.max(damage_9, influence-conviction); break;}
			case 2: {costs_2 += Math.max(damage_2, influence-conviction);
			costs_4 += Math.max(damage_4, influence-conviction);
			costs_5 += Math.max(damage_5, influence-conviction);
			costs_8 += Math.max(damage_8, influence-conviction);
			costs_9 += Math.max(damage_9, influence-conviction); break;}
			case 4: {costs_4 += Math.max(damage_4, influence-conviction);
			costs_5 += Math.max(damage_5, influence-conviction);
			costs_8 += Math.max(damage_8, influence-conviction);
			costs_9 += Math.max(damage_9, influence-conviction); break;}
			case 5: {costs_5 += Math.max(damage_5, influence-conviction);
			costs_8 += Math.max(damage_8, influence-conviction);
			costs_9 += Math.max(damage_9, influence-conviction); break;}
			case 8: {costs_8 += Math.max(damage_8, influence-conviction);
			costs_9 += Math.max(damage_9, influence-conviction); break;}
			case 9: {costs_9 += Math.max(damage_9, influence-conviction);}
			}
		}
		for (int i=0; i<Info.friendly_muckrakers.length; i++) {
			int influence = Info.friendly_muckrakers[i].influence;
			int conviction = Info.friendly_muckrakers[i].conviction;
			switch (Info.loc.distanceSquaredTo(Info.friendly_muckrakers[i].location)) {
			case 1: {costs_1 += Math.max(damage_1, influence-conviction);
			costs_2 += Math.max(damage_2, influence-conviction);
			costs_4 += Math.max(damage_4, influence-conviction);
			costs_5 += Math.max(damage_5, influence-conviction);
			costs_8 += Math.max(damage_8, influence-conviction);
			costs_9 += Math.max(damage_9, influence-conviction); break;}
			case 2: {costs_2 += Math.max(damage_2, influence-conviction);
			costs_4 += Math.max(damage_4, influence-conviction);
			costs_5 += Math.max(damage_5, influence-conviction);
			costs_8 += Math.max(damage_8, influence-conviction);
			costs_9 += Math.max(damage_9, influence-conviction); break;}
			case 4: {costs_4 += Math.max(damage_4, influence-conviction);
			costs_5 += Math.max(damage_5, influence-conviction);
			costs_8 += Math.max(damage_8, influence-conviction);
			costs_9 += Math.max(damage_9, influence-conviction); break;}
			case 5: {costs_5 += Math.max(damage_5, influence-conviction);
			costs_8 += Math.max(damage_8, influence-conviction);
			costs_9 += Math.max(damage_9, influence-conviction); break;}
			case 8: {costs_8 += Math.max(damage_8, influence-conviction);
			costs_9 += Math.max(damage_9, influence-conviction); break;}
			case 9: {costs_9 += Math.max(damage_9, influence-conviction);}
			}
		}
		for (int i=0; i<Info.enemy_ecs.length; i++) {
			switch (Info.loc.distanceSquaredTo(Info.enemy_ecs[i].location)) {
			case 1: {costs_1 += damage_1; break;}
			case 2: {costs_2 += damage_2; break;}
			case 4: {costs_4 += damage_4; break;}
			case 5: {costs_5 += damage_5; break;}
			case 8: {costs_8 += damage_8; break;}
			case 9: {costs_9 += damage_9; break;}
			}
		}
		for (int i=0; i<Info.enemy_politicians.length; i++) {
			int influence = Info.enemy_politicians[i].influence;
			int conviction = Info.enemy_politicians[i].conviction;
			switch (Info.loc.distanceSquaredTo(Info.enemy_politicians[i].location)) {
			case 1: {kills_1 += (damage_1>conviction)?2:0; costs_1 += Math.max(damage_1, influence+conviction);
			 kills_2 += (damage_2>conviction)?2:0; costs_2 += Math.max(damage_2, influence+conviction);
			 kills_4 += (damage_4>conviction)?2:0; costs_4 += Math.max(damage_4, influence+conviction);
			 kills_5 += (damage_5>conviction)?2:0; costs_5 += Math.max(damage_5, influence+conviction);
			 kills_8 += (damage_8>conviction)?2:0; costs_8 += Math.max(damage_8, influence+conviction);
			 kills_9 += (damage_9>conviction)?2:0; costs_9 += Math.max(damage_9, influence+conviction); break;}
			case 2: {kills_2 += (damage_2>conviction)?2:0; costs_2 += Math.max(damage_2, influence+conviction);
			 kills_4 += (damage_4>conviction)?2:0; costs_4 += Math.max(damage_4, influence+conviction);
			 kills_5 += (damage_5>conviction)?2:0; costs_5 += Math.max(damage_5, influence+conviction);
			 kills_8 += (damage_8>conviction)?2:0; costs_8 += Math.max(damage_8, influence+conviction);
			 kills_9 += (damage_9>conviction)?2:0; costs_9 += Math.max(damage_9, influence+conviction); break;}
			case 4: {kills_4 += (damage_4>conviction)?2:0; costs_4 += Math.max(damage_4, influence+conviction);
			 kills_5 += (damage_5>conviction)?2:0; costs_5 += Math.max(damage_5, influence+conviction);
			 kills_8 += (damage_8>conviction)?2:0; costs_8 += Math.max(damage_8, influence+conviction);
			 kills_9 += (damage_9>conviction)?2:0; costs_9 += Math.max(damage_9, influence+conviction); break;}
			case 5: {kills_5 += (damage_5>conviction)?2:0; costs_5 += Math.max(damage_5, influence+conviction);
			 kills_8 += (damage_8>conviction)?2:0; costs_8 += Math.max(damage_8, influence+conviction);
			 kills_9 += (damage_9>conviction)?2:0; costs_9 += Math.max(damage_9, influence+conviction); break;}
			case 8: {kills_8 += (damage_8>conviction)?2:0; costs_8 += Math.max(damage_8, influence+conviction);
			 kills_9 += (damage_9>conviction)?2:0; costs_9 += Math.max(damage_9, influence+conviction); break;}
			case 9: {kills_9 += (damage_9>conviction)?2:0; costs_9 += Math.max(damage_9, influence+conviction); break;}
			}
		}
		for (int i=0; i<Info.enemy_muckrakers.length; i++) {
			int conviction = Info.enemy_muckrakers[i].conviction;
			switch (Info.loc.distanceSquaredTo(Info.enemy_muckrakers[i].location)) {
			case 1: {kills_1 += (damage_1>conviction)?1:0; costs_1 += Math.max(damage_1, conviction);
			kills_2 += (damage_2>conviction)?1:0; costs_2 += Math.max(damage_2, conviction);
			kills_4 += (damage_4>conviction)?1:0; costs_4 += Math.max(damage_4, conviction);
			kills_5 += (damage_5>conviction)?1:0; costs_5 += Math.max(damage_5, conviction);
			kills_8 += (damage_8>conviction)?1:0; costs_8 += Math.max(damage_8, conviction);
			kills_9 += (damage_9>conviction)?1:0; costs_9 += Math.max(damage_9, conviction); break;}
			case 2: {kills_2 += (damage_2>conviction)?1:0; costs_2 += Math.max(damage_2, conviction);
			kills_4 += (damage_4>conviction)?1:0; costs_4 += Math.max(damage_4, conviction);
			kills_5 += (damage_5>conviction)?1:0; costs_5 += Math.max(damage_5, conviction);
			kills_8 += (damage_8>conviction)?1:0; costs_8 += Math.max(damage_8, conviction);
			kills_9 += (damage_9>conviction)?1:0; costs_9 += Math.max(damage_9, conviction); break;}
			case 4: {kills_4 += (damage_4>conviction)?1:0; costs_4 += Math.max(damage_4, conviction);
			kills_5 += (damage_5>conviction)?1:0; costs_5 += Math.max(damage_5, conviction);
			kills_8 += (damage_8>conviction)?1:0; costs_8 += Math.max(damage_8, conviction);
			kills_9 += (damage_9>conviction)?1:0; costs_9 += Math.max(damage_9, conviction); break;}
			case 5: {kills_5 += (damage_5>conviction)?1:0; costs_5 += Math.max(damage_5, conviction);
			kills_8 += (damage_8>conviction)?1:0; costs_8 += Math.max(damage_8, conviction);
			kills_9 += (damage_9>conviction)?1:0; costs_9 += Math.max(damage_9, conviction); break;}
			case 8: {kills_8 += (damage_8>conviction)?1:0; costs_8 += Math.max(damage_8, conviction);
			kills_9 += (damage_9>conviction)?1:0; costs_9 += Math.max(damage_9, conviction); break;}
			case 9: {kills_9 += (damage_9>conviction)?1:0; costs_9 += Math.max(damage_9, conviction); break;}
			}
		}
		int best_radius = 1;
		int best_kills = kills_1;
		if (kills_2>best_kills) {best_radius = 2; best_kills = kills_2;}
		if (kills_4>best_kills) {best_radius = 4; best_kills = kills_4;}
		if (kills_5>best_kills) {best_radius = 5; best_kills = kills_5;}
		if (kills_8>best_kills) {best_radius = 8; best_kills = kills_8;}
		if (kills_9>best_kills) {best_radius = 9; best_kills = kills_9;}
		return best_radius;
	}
}
