package com.ENGO623Final.Util;

import java.util.stream.IntStream;

import org.ejml.simple.SimpleMatrix;

public class Rotation {
	
	public static double[] dcm2euler(double[][] dcm) {

		double yaw = Math.atan2(dcm[0][1], dcm[0][0]);
		double pitch = -Math.asin(dcm[0][2]);
		double roll = Math.atan2(dcm[1][2], dcm[2][2]);
		return new double[] { roll,pitch,yaw };
	}

	public static double[][] euler2dcm(double[] euler) {
		double roll = euler[0];
		double pitch = euler[1];
		double yaw = euler[2];

		double[][] dcm = new double[3][3];
		double cp = Math.cos(pitch);
		double sp = Math.sin(pitch);
		double sr = Math.sin(roll);
		double cr = Math.cos(roll);
		double sy = Math.sin(yaw);
		double cy = Math.cos(yaw);

		dcm[0][0] = cp * cy;
		dcm[1][0] = (sr * sp * cy) - (cr * sy);
		dcm[2][0] = (cr * sp * cy) + (sr * sy);
		dcm[0][1] = cp * sy;
		dcm[1][1] = (sr * sp * sy) + (cr * cy);
		dcm[2][1] = (cr * sp * sy) - (sr * cy);
		dcm[0][2] = -sp;
		dcm[1][2] = sr * cp;
		dcm[2][2] = cr * cp;

		return dcm;
	}

	// Perform reorthogonalization and renormalization of direction cosine matrix
	public static SimpleMatrix reorthonormDcm(SimpleMatrix A) throws Exception {
		SimpleMatrix[] a = new SimpleMatrix[3];
		IntStream.range(0, 3).forEach(i -> a[i] = A.extractVector(true, i).transpose());
		double delta12 = a[0].transpose().mult(a[1]).get(0);
		double delta13 = a[0].transpose().mult(a[2]).get(0);
		double delta23 = a[1].transpose().mult(a[2]).get(0);
		SimpleMatrix[] a_new = new SimpleMatrix[3];
		a_new[0] = a[0].minus(a[1].scale(0.5 * delta12)).minus(a[2].scale(0.5 * delta13));
		a_new[1] = a[1].minus(a[0].scale(0.5 * delta12)).minus(a[2].scale(0.5 * delta23));
		a_new[2] = a[2].minus(a[0].scale(0.5 * delta13)).minus(a[1].scale(0.5 * delta23));
		for (int i = 0; i < 3; i++) {
			double delta = a_new[i].transpose().mult(a_new[i]).get(0);
			a_new[i] = a_new[i].scale(2 / (1 + delta));
			a_new[i] = a_new[i].transpose();
		}
		SimpleMatrix A_new = a_new[0].concatRows(a_new[1], a_new[2]);
		SimpleMatrix check = A_new.mult(A_new.transpose());
		for(int i=0;i<3;i++)
		{
			for(int j=0;j<3;j++)
			{
				if(i==j)
				{
					if(Math.abs(check.get(i,j)-1)>1e-15)
					{
						throw new Exception("Reortho not working");
					}
				}
				else
				{
					if(Math.abs(check.get(i,j))>1e-15)
					{
						throw new Exception("Reortho not working");
					}
				}
			}
		}
		return A_new;
		
	}

}
