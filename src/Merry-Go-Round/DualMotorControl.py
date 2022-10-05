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
                    print("Deceleration distance found ",self.decelDist)
        else: # After half way:
            if(self.decelDist < 0):
                self.decelDist = abs(self.angle/2)
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

# Test:
m1 = AccelerationMotorControl(primitiveHub.port.C, 60, 1000)
m2 = AccelerationMotorControl(primitiveHub.port.E, 40, 500)
dmc = DualMotorControl(m1,m2)
dmc.move(360,360)
dmc.move(-360,-360)
#n1 = Motor('C')
#n2 = Motor('E')
#n1.run_to_degrees_counted(300)
#n2.run_for_degrees(300)
#n1.run_to_degrees_counted(-300)
#n2.run_for_degrees(-300)
raise SystemExit
