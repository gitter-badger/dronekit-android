package org.droidplanner.services.android.core.drone.variables;

import java.util.ArrayList;
import java.util.List;

import org.droidplanner.services.android.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.core.drone.DroneVariable;
import org.droidplanner.services.android.core.helpers.coordinates.Coord2D;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;
import org.droidplanner.services.android.core.survey.CameraInfo;
import org.droidplanner.services.android.core.survey.Footprint;

import com.MAVLink.ardupilotmega.msg_camera_feedback;
import com.MAVLink.ardupilotmega.msg_mount_status;

public class Camera extends DroneVariable {
	private CameraInfo camera = new CameraInfo();
	private List<Footprint> footprints = new ArrayList<Footprint>();
	private double gimbal_pitch;

	public Camera(MavLinkDrone myDrone) {
		super(myDrone);
	}

	public void newImageLocation(msg_camera_feedback msg) {
		footprints.add(new Footprint(camera, msg));
		myDrone.notifyDroneEvent(DroneEventsType.FOOTPRINT);
	}

    public List<Footprint> getFootprints(){
        return footprints;
    }

	public Footprint getLastFootprint() {
		return footprints.get(footprints.size() - 1);
	}

	public CameraInfo getCamera() {
		return camera;
	}

	public Footprint getCurrentFieldOfView() {
		double altitude = myDrone.getAltitude().getAltitude();
		Coord2D position = myDrone.getGps().getPosition();
		//double pitch = myDrone.getOrientation().getPitch() - gimbal_pitch;
		double pitch = myDrone.getOrientation().getPitch();
		double roll = myDrone.getOrientation().getRoll();
		double yaw = myDrone.getOrientation().getYaw();
		return new Footprint(camera, position, altitude, pitch, roll, yaw);
	}

	public void updateMountOrientation(msg_mount_status msg_mount_status) {
		gimbal_pitch = 90 - msg_mount_status.pointing_a / 100;
	}

}
