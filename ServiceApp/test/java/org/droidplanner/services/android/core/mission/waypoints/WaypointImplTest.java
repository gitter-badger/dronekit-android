package org.droidplanner.services.android.core.mission.waypoints;

import com.MAVLink.common.msg_mission_item;
import com.MAVLink.enums.MAV_CMD;

import junit.framework.TestCase;

import org.droidplanner.services.android.core.helpers.coordinates.Coord3D;
import org.droidplanner.services.android.core.mission.Mission;

import java.util.List;

public class WaypointImplTest extends TestCase {

    public void testPackMissionItem() {
        Mission mission = new Mission(null);
        WaypointImpl item = new WaypointImpl(mission, new Coord3D(0, 1, (2)));

        List<msg_mission_item> listOfMsg = item.packMissionItem();
        assertEquals(1, listOfMsg.size());

        msg_mission_item msg = listOfMsg.get(0);

        assertEquals(MAV_CMD.MAV_CMD_NAV_WAYPOINT, msg.command);
        assertEquals(0.0f, msg.param1);
        assertEquals(0.0f, msg.param2);
        assertEquals(0.0f, msg.param3);
        assertEquals(0.0f, msg.param3);
    }

}
