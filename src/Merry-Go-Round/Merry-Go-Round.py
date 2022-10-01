import hub as primitiveHub
import utime

from spike import PrimeHub, LightMatrix, Button, StatusLight, ForceSensor, MotionSensor, Speaker, ColorSensor, App, DistanceSensor, Motor, MotorPair
from spike.control import wait_for_seconds, wait_until, Timer
from math import *

hub = PrimeHub()

# t = time, p = point, v = power
class AccelerationMotorControl:
    def __init__(self, port, vMax, toVMaxMS):
        self.port = port
        self.vMax = vMax
        self.toVMaxMS = toVMaxMS
        self.vMin = 0
        print("Motor set up vMax ",vMax," time to vMax ",toVMaxMS)
    def move(self,angle):
        self.p1 = self.port.motor.get()[1]
        self.angle = angle
        self.p2 = self.p1+angle
        self.t0 = utime.ticks_ms()
        self.decelDist = -1
        self.v = self.vMin
        self.stalledFromT = -1
        self.dLast = 0
    def update(self):
        p = self.port.motor.get()[1]
        if(self.vMin == 0 and p != self.p1):
            self.vMin = self.v
            print("Minimal movement power found: ",self.vMin)
        d = abs(p - self.p1)

        if(d >= abs(self.angle)): # Done:
            self.port.motor.float()
            return False

        t = utime.ticks_ms()
        if(self.v > self.vMin and self.vMin > 0 and d == self.dLast): # Stalled:
            if(self.stalledFromT < 0):
                self.stalledFromT = t
        else:
            self.stalledFromT = -1
        if(self.stalledFromT >= 0 and t - self.stalledFromT > 500):
            print("Stalled! ", t, " ", self.stalledFromT)
            self.port.motor.float()
            return False

        if(d < abs(self.p2-self.p1)/2): # Before half way:
            self.v = self.vMin + (t-self.t0) / self.toVMaxMS * (self.vMax-self.vMin)
            if(self.v >= self.vMax):
                self.v = self.vMax
                if(self.decelDist < 0):
                    self.decelDist = abs(p - self.p1)
        else: # After half way:
            if(self.decelDist < 0):
                self.decelDist = abs(self.angle/2)
            if(self.decelDist == 0):
                self.v = self.vMin
            else:
                self.v = self.vMin + abs(p-self.p2)/self.decelDist * (self.vMax-self.vMin)
            if(self.v >= self.vMax):
                self.v = self.vMax

        if(self.angle > 0):
            self.port.pwm(self.v)
        else:
            self.port.pwm(-self.v)
        #print("Speed ",self.v)
        self.dLast = d
        return True

carResetter = primitiveHub.port.D
lifterResetter = primitiveHub.port.B

class DualMotorControl:
    def __init__(self, mc1, mc2):
        self.mc1 = mc1
        self.mc2 = mc2
    def move(self,angle1,angle2):
        self.mc1.move(angle1)
        self.mc2.move(angle2)
        run = True
        while(run):
            wait_for_seconds(0.05)
            a = self.mc1.update()
            b = self.mc2.update()
            run = a or b

def isStalled(motor):
    p1 = motor.get()[1]
    wait_for_seconds(0.2)
    p2 = motor.get()[1]
    return p1 == p2

def driveToStalled(port, speed):
    port.pwm(speed)
    while not isStalled(port.motor):
        pass
    port.motor.float()

def resetLifter():
    driveToStalled(lifterResetter, 25)

def resetCar():
    driveToStalled(carResetter, -25)

def resetCarAndLifter():
    resetCar()
    resetLifter()
    car.run_for_degrees(25)

light_sensor = ColorSensor('E')

track = Motor('F')

lifter = Motor('B')
lifter.set_default_speed(7)
lifter.set_degrees_counted(0)

car = Motor('D')
car.set_default_speed(20)

aLifter = AccelerationMotorControl(primitiveHub.port.B, 25, 600)
aCar = AccelerationMotorControl(primitiveHub.port.D, 40, 100)
lifterCar = DualMotorControl(aLifter,aCar)

rotator = Motor('A')
rotator.set_default_speed(37)
rotatorResetter = primitiveHub.port.A

def track_in():
    track.start_at_power(-25)

def track_out():
    track.start_at_power(25)

def in_cup():
    track.stop()
    # Lift all the way up:
    lifterCar.move(-60,0)
    track_out() # 128, -350
    lifterCar.move(-69,-200)
    # Push and pull:
    car.run_for_degrees(490)
    car.run_for_degrees(-530)
    # Return:
    lifterCar.move(129,240)
    resetCarAndLifter()

def out_cup():
    # Lift all the way up:
    lifterCar.move(-60,0)
    lifterCar.move(-69,-200)
    # Push and pull:
    car.run_for_degrees(480)
    lifterCar.move(19,50)
    car.run_for_degrees(-480-50-20)
    # Return:
    lifterCar.move(110,220)
    resetCarAndLifter()
    wait_for_seconds(3) # Ensure out

banana = 140
large = 40
def rotate(steps):
    rotator.run_for_rotations(-steps*banana/large/4, 30)

def invite():
    track_in()
    seen = 0
    while seen < 4:
        if light_sensor.get_color() == 'black' or light_sensor.get_color() == None:
            seen = 0
        else:
            seen = seen+1
    track.stop()
    track.run_for_rotations(-1.8, 28)

def resetRotator():
    rotator.run_for_degrees(-100)
    driveToStalled(rotatorResetter, 35)
    rotator.run_for_degrees(-33)

#invite()
#in_cup()
#out_cup()
#track.run_for_rotations(10, 28)
#raise SystemExit

while(True):
    resetRotator()
    for _ in range(4):
        rotate(1)
        invite()
        in_cup()
    rotate(16)
    for _ in range(4):
        rotate(1)
        out_cup()
    wait_for_seconds(30) # Cool down
