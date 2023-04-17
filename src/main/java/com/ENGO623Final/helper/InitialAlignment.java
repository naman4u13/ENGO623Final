package com.ENGO623Final.helper;

import java.util.ArrayList;
import java.util.stream.IntStream;

import org.ejml.simple.SimpleMatrix;

import com.ENGO623Final.Util.LatLonUtil;
import com.ENGO623Final.Util.Matrix;
import com.ENGO623Final.Util.Rotation;
import com.ENGO623Final.models.ImuSensor;

public class InitialAlignment {

	// Self-Alignment
	// Refer Paul D. Groves Book
	public static SimpleMatrix process(ArrayList<ImuSensor> dataList, int m,double[] llh0) throws Exception {
		double[] avgAcc = new double[3];
		double[] avgGyro = new double[3];
		for (int i = 0; i < m; i++) {
			avgAcc[0] += dataList.get(i).getAccX();
			avgAcc[1] += dataList.get(i).getAccY();
			avgAcc[2] += dataList.get(i).getAccZ();
			avgGyro[0] += dataList.get(i).getGyroX();
			avgGyro[1] += dataList.get(i).getGyroY();
			avgGyro[2] += dataList.get(i).getGyroZ();
		}
		IntStream.range(0, 3).forEach(i -> avgAcc[i] /= m);
		IntStream.range(0, 3).forEach(i -> avgGyro[i] /= m);
		// Accelerometer Leveling
		double pitch = Math.atan(-avgAcc[0] / Math.sqrt(Math.pow(avgAcc[1], 2) + Math.pow(avgAcc[2], 2)));
		double roll = Math.atan2(-avgAcc[1], -avgAcc[2]);

		// Gyro-Compassing
		double sin_yaw = -(avgGyro[1] * Math.cos(roll)) + (avgGyro[2] * Math.sin(roll));
		double cos_yaw = (avgGyro[0] * Math.cos(pitch)) + (avgGyro[1] * Math.sin(roll) * Math.sin(pitch))
				+ (avgGyro[2] * Math.cos(roll) * Math.sin(pitch));
		double yaw = Math.atan2(sin_yaw, cos_yaw);
//		double lat = llh0[0];
//		double alt = llh0[2];
//		double g = LatLonUtil.getGravity(lat, alt);
//		double r = Math.signum(avgAcc[2]) * Math.asin(avgAcc[1] / g);
//		double p = Math.asin(-avgAcc[0] / g);// -Math.signum(avgAcc[2])*Math.asin(avgAcc[0]/g);
//		double y = Math.atan2(-avgGyro[1], avgGyro[0]);
		// yaw = y;
		SimpleMatrix dcm = new SimpleMatrix(Rotation.euler2dcm(new double[] { roll, pitch, yaw }));
		//SimpleMatrix _dcm = new SimpleMatrix(Rotation.euler2dcm(new double[] { r, p, y }));
		dcm = Rotation.reorthonormDcm(dcm);
		double[] temp = Rotation.dcm2euler(Matrix.matrix2Array(dcm));
//		if ((Math.abs(temp[0] - roll) > 1e-6) || (Math.abs(temp[1] - pitch) > 1e-6)
//				|| (Math.abs(temp[2] - yaw) > 1e-6)) {
//			throw new Exception("Wrong DCM and Euler conversion");
//		}
		return dcm;
	}

	public static SimpleMatrix process2(ArrayList<ImuSensor> dataList, int m, double[] llh0) throws Exception {
		double[] avgAcc = new double[3];
		double[] avgGyro = new double[3];
		for (int i = 0; i < m; i++) {
			avgAcc[0] += dataList.get(i).getAccX();
			avgAcc[1] += dataList.get(i).getAccY();
			avgAcc[2] += dataList.get(i).getAccZ();
			avgGyro[0] += dataList.get(i).getGyroX();
			avgGyro[1] += dataList.get(i).getGyroY();
			avgGyro[2] += dataList.get(i).getGyroZ();
		}
		IntStream.range(0, 3).forEach(i -> avgAcc[i] /= m);
		IntStream.range(0, 3).forEach(i -> avgGyro[i] /= m);
		double[] _v_b = Matrix.crossProduct(avgAcc, avgGyro);
		SimpleMatrix g_b = new SimpleMatrix(3, 1, true, avgAcc);
		SimpleMatrix w_b = new SimpleMatrix(3, 1, true, avgGyro);
		SimpleMatrix v_b = new SimpleMatrix(3, 1, true, _v_b);
		SimpleMatrix bodyFrame = new SimpleMatrix(0, 0);
		bodyFrame = bodyFrame.concatRows(g_b.transpose(), w_b.transpose(), v_b.transpose());
		double lat = llh0[0];
		double alt = llh0[2];
		double g = LatLonUtil.getGravity(lat, alt);
		final double w_ie = LatLonUtil.omega_ie;
		double cosL = Math.cos(lat);
		double sinL = Math.sin(lat);
		double[][] _navFrame = new double[][] { { 0, 0, -g }, { w_ie * cosL, 0, -w_ie * sinL },
				{ 0, -g * w_ie * cosL, 0 } };
//		double[][] _navFrameInv = new double[][] { { -Math.tan(lat) / g, 1 / (w_ie *cosL), 0 },
//				{ 0, 0, -1 / (g * w_ie *cosL) }, { -1 / g, 0, 0 } };
		SimpleMatrix navFrameInv = new SimpleMatrix(_navFrame).invert();
		SimpleMatrix dcm = navFrameInv.mult(bodyFrame);
		dcm = Rotation.reorthonormDcm(dcm);
		return dcm;

	}

}
