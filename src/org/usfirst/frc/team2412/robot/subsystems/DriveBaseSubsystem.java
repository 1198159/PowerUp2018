package org.usfirst.frc.team2412.robot.subsystems;

import org.usfirst.frc.team2412.robot.RobotMap;
import org.usfirst.frc.team2412.robot.commands.DriveCommand;

import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

import edu.wpi.first.wpilibj.GyroBase;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.command.PIDSubsystem;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class DriveBaseSubsystem extends PIDSubsystem {
	// DifferentialDrive for driving.
	private DifferentialDrive robotDrive = RobotMap.robotDrive;
	
	// TalonSRXs for driving.
	private WPI_TalonSRX talons[] = RobotMap.talons;
	
	// Gyroscope for turning.
	private GyroBase gyro = RobotMap.gyro;
	
	// TalonSRXs with encoders.
	private WPI_TalonSRX leftTalon = talons[4];
	private WPI_TalonSRX rightTalon = talons[5];
	
	// Initial encoder values.
	private double startingValueLeft = 0;
	private double startingValueRight = 0;
	
	// Angle to turn to.
	private double turnAngle = 0;
	
	// P for turning.
	private double Tp = 2;
	
	// Different speeds for driving and turning.
	private final double DRIVENORMALSPEED = 0.8;
	private final double TURNNORMALSPEED = 0.6;
	
	private final double DRIVEBABYSPEED = 0.4;
	private final double TURNBABYSPEED = 0.4;
	
	public DriveBaseSubsystem() {
		super(0.00002, 0.00000002, 0.0000001); // Default PID values.
		leftTalon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
		leftTalon.setSelectedSensorPosition(0, 0, 0);
		
		rightTalon.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, 0);
		rightTalon.setSelectedSensorPosition(0, 0, 0);
	}
	
	@Override
	protected void initDefaultCommand() {
		setDefaultCommand(new DriveCommand());
	}
	
	// Driving-related methods.
	public void drive(Joystick stick) {
		drive(stick.getY(), -stick.getTwist()*0.8, false);
	}
	
	public void drive(double y, double x, boolean squaredInputs) {
		if(RobotMap.driveNormally) {
			robotDrive.arcadeDrive(DRIVENORMALSPEED * y, TURNNORMALSPEED * x, squaredInputs);
			System.out.println("Driving normally...");
		} else {
			robotDrive.arcadeDrive(DRIVEBABYSPEED * y, TURNBABYSPEED * x, squaredInputs);
			System.out.println("Driving slowly...");
		}
	}
	
	public void setSideSpeeds(double leftSpeed, double rightSpeed) {
		robotDrive.setSafetyEnabled(false);
		
		talons[0].set(leftSpeed);
		talons[2].set(leftSpeed);
		talons[4].set(leftSpeed);
		
		talons[1].set(rightSpeed);
		talons[3].set(rightSpeed);
		talons[5].set(rightSpeed);
	}
	
	// Gyro-related methods.
	public double getAngle() {
		return gyro.getAngle();
	}
	
	public void resetAngle() {
		gyro.reset();
	}
	
	public void setTurnAngle(double newAngle) {
		turnAngle = newAngle;
	}
	
	// Encoder-related methods.
	public double getRawLeftEncoderValue() {
		return leftTalon.getSelectedSensorPosition(0);
	}
	
	public double getLeftEncoderValue() {
		return getRawLeftEncoderValue() - startingValueLeft;
	}
	
	public void resetLeftEncoder() {
		startingValueLeft = getRawLeftEncoderValue();
	}
	
	public double getRawRightEncoderValue() {
		return rightTalon.getSelectedSensorPosition(0);
	}
	
	public double getRightEncoderValue() {
		return getRawRightEncoderValue() - startingValueRight;
	}
	
	public void resetRightEncoder() {
		startingValueRight = getRawRightEncoderValue();
	}
	
	// PID-related methods.
	@Override
	public double returnPIDInput() {
		double encoderValue = getRightEncoderValue();
		SmartDashboard.putNumber("Encoder position", encoderValue);
		return encoderValue;
	}

	@Override
	protected void usePIDOutput(double output) {
		double angleError = turnAngle - getAngle();
		double turn = -Tp * angleError / 90;
		drive(-output, turn, false);
	}
	
	public void setTp(double Tp) {
		this.Tp = Tp;
	}
}
