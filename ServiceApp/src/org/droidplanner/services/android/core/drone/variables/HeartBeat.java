package org.droidplanner.services.android.core.drone.variables;

import com.MAVLink.common.msg_heartbeat;

import org.droidplanner.services.android.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.services.android.core.drone.DroneInterfaces.Handler;
import org.droidplanner.services.android.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.services.android.core.drone.DroneVariable;
import org.droidplanner.services.android.core.drone.autopilot.MavLinkDrone;

import timber.log.Timber;

public class HeartBeat extends DroneVariable implements OnDroneListener {

    private static final long HEARTBEAT_NORMAL_TIMEOUT = 5000; //ms
    private static final long HEARTBEAT_LOST_TIMEOUT = 15000; //ms
    private static final long HEARTBEAT_IMU_CALIBRATION_TIMEOUT = 35000; //ms

    public static final int INVALID_MAVLINK_VERSION = -1;

    public HeartbeatState heartbeatState = HeartbeatState.FIRST_HEARTBEAT;
    private byte sysid = 1;
    private byte compid = 1;

    /**
     * Stores the version of the mavlink protocol.
     */
    private short mMavlinkVersion = INVALID_MAVLINK_VERSION;

    public enum HeartbeatState {
        FIRST_HEARTBEAT, LOST_HEARTBEAT, NORMAL_HEARTBEAT, IMU_CALIBRATION
    }

    public final Handler watchdog;
    public final Runnable watchdogCallback = new Runnable() {
        @Override
        public void run() {
            onHeartbeatTimeout();
        }
    };

    public HeartBeat(MavLinkDrone myDrone, Handler handler) {
        super(myDrone);
        this.watchdog = handler;
        myDrone.addDroneListener(this);
    }

    public byte getSysid() {
        return sysid;
    }

    public byte getCompid() {
        return compid;
    }

    /**
     * @return the version of the mavlink protocol.
     */
    public short getMavlinkVersion() {
        return mMavlinkVersion;
    }

    public void onHeartbeat(msg_heartbeat msg) {
        sysid = (byte) msg.sysid;
        compid = (byte) msg.compid;
        mMavlinkVersion = msg.mavlink_version;

        switch (heartbeatState) {
            case FIRST_HEARTBEAT:
                notifyConnected();
                Timber.d("Received first heartbeat.");
                myDrone.notifyDroneEvent(DroneEventsType.HEARTBEAT_FIRST);
                break;

            case LOST_HEARTBEAT:
                myDrone.notifyDroneEvent(DroneEventsType.HEARTBEAT_RESTORED);
                break;
        }

        heartbeatState = HeartbeatState.NORMAL_HEARTBEAT;
        restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
    }

    public boolean hasHeartbeat() {
        return heartbeatState != HeartbeatState.FIRST_HEARTBEAT;
    }

    public boolean isConnectionAlive() {
        return heartbeatState != HeartbeatState.LOST_HEARTBEAT;
    }

    @Override
    public void onDroneEvent(DroneEventsType event, MavLinkDrone drone) {
        switch (event) {
            case CALIBRATION_IMU:
                //Set the heartbeat in imu calibration mode.
                heartbeatState = HeartbeatState.IMU_CALIBRATION;
                restartWatchdog(HEARTBEAT_IMU_CALIBRATION_TIMEOUT);
                break;

            case CONNECTION_FAILED:
            case DISCONNECTED:
                notifyDisconnected();
                break;

            default:
                break;
        }
    }

    private void notifyConnected() {
        restartWatchdog(HEARTBEAT_NORMAL_TIMEOUT);
    }

    private void notifyDisconnected() {
        watchdog.removeCallbacks(watchdogCallback);
        heartbeatState = HeartbeatState.FIRST_HEARTBEAT;
        mMavlinkVersion = INVALID_MAVLINK_VERSION;
    }

    private void onHeartbeatTimeout() {
        switch (heartbeatState) {
            case IMU_CALIBRATION:
                restartWatchdog(HEARTBEAT_IMU_CALIBRATION_TIMEOUT);
                myDrone.notifyDroneEvent(DroneEventsType.CALIBRATION_TIMEOUT);
                break;

            case FIRST_HEARTBEAT:
                Timber.d("First heartbeat timeout.");
                myDrone.notifyDroneEvent(DroneEventsType.CONNECTION_FAILED);
                break;

            default:
                heartbeatState = HeartbeatState.LOST_HEARTBEAT;
                restartWatchdog(HEARTBEAT_LOST_TIMEOUT);
                myDrone.notifyDroneEvent(DroneEventsType.HEARTBEAT_TIMEOUT);
                break;
        }
    }

    private void restartWatchdog(long timeout) {
        // re-start watchdog
        watchdog.removeCallbacks(watchdogCallback);
        watchdog.postDelayed(watchdogCallback, timeout);
    }
}
