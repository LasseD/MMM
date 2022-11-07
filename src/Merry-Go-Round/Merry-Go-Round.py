import hub as primitiveHub
import utime

dT = 0.015 # Loop time
MIN_V = 16

from spike import PrimeHub, LightMatrix, Button, StatusLight, ForceSensor, MotionSensor, Speaker, ColorSensor, App, DistanceSensor, Motor, MotorPair
from spike.control import wait_for_seconds, wait_until, Timer
from math import *

hub = PrimeHub()

class SoftMotorControl:
    def __init__(self, port, vMax, relative = True): # Relative or absolute: Absolute moves to position in [0,360[
        self.port = port
        self.relative = relative
        self.positionIndex = 1 if relative else 2
        self.p2 = self.p0 = self.getPosition()
        self.vMin = MIN_V
        self.vMax = vMax
    def getPosition(self):
        ret = self.port.motor.get()[self.positionIndex]
        if ret < 0 and not self.relative:
            ret = ret + 360
        return ret
    def reset(self):
        self.p2 = self.getPosition()
    def move(self,angle,timeMS):
        # Positions: p0 -> p -> p2
        # Time:    t0 -> t -> t2
        self.t0 = self.tPrev = utime.ticks_ms()
        self.t2 = self.t0 + timeMS

        self.p0 = self.p2
        if self.relative:
            self.p2 = self.p2 + angle
        else:
            self.p2 = angle

        self.fwd = self.p2 > self.p0
        self.pPrev = self.p0
        self.avgSpeed = 1000 * (self.p2-self.p0) / timeMS
        self.v = self.vMin

        #print(self.port.motor.get(), self.positionIndex, self.p0,'->',self.p2,'in',timeMS,'relative:',self.relative)
    def update(self):
        p = self.getPosition()
        t = utime.ticks_ms()

        if self.p2 == self.p0: # No move.
            #print('No Move',t-self.t0,'Position',p,"V",self.v)
            self.port.motor.float()
            return True

        doneMove = (p-self.p0) / (self.p2-self.p0)
        if doneMove >= 0.98: # Moved all the way!
            self.port.motor.float()
            #print('Done Time',t-self.t0,'Position',p,"V",self.v,"vMin",self.vMin)
            return True

        if p == self.pPrev: # Not yet moved!
            self.v = self.v+1
            self.port.pwm(self.v if self.fwd else -self.v)
            self.vMin = min(self.vMax, max(self.vMin, self.v)) # Ensure we don't go over max, and also not under current vMin!
            #print("Bump minimal movement power current",self.v,"vMin",self.vMin,"vMax",self.vMax,self.relative,"%moved",doneMove*100)
            return False

        time = (t-self.t0)/(self.t2-self.t0)
        if time > 5: # Never reached goal!
            self.port.motor.float()
            print('TIMEOUT! time',t,"t0",self.t0,"t2",self.t2,"Move",p,"->",self.p2,"V",self.v,"vMin",self.vMin,"vMax",self.vMax,"%moved",doneMove*100)
            raise Exception('Timeout')

        speed = 0 if p==self.pPrev else (t-self.tPrev) / (p-self.pPrev)

        if doneMove > 0.7: # Decelerate:
            self.v = self.vMin + (self.vMax-self.vMin) * (1 - doneMove)/0.3
            #print('A',self.relative,self.v,self.vMin,self.vMax)
        elif time < 0.3 and speed < self.avgSpeed: # Accellerate:
            self.v = min(self.vMax, self.v + 5)
            #print('B',self.relative,self.v,self.vMin,self.vMax)
        elif speed > self.avgSpeed:
            self.v = max(self.v-2,self.vMin)
            #print('C',self.relative,self.v,self.vMin,self.vMax)
        elif speed < self.avgSpeed:
            self.v = min(self.vMax, self.v+2)
            #print('D',self.relative,self.v,self.vMin,self.vMax)
        self.port.pwm(self.v if self.fwd else -self.v)
        #print('Time',t-self.t0,'Position',p,'%time',time,"speed",self.v)
        self.tPrev = t
        self.pPrev = p
        return False

class DualMotorControl:
    def __init__(self, mc1, mc2):
        self.mc1 = mc1
        self.mc2 = mc2
    def move(self,angle1,angle2,timeMS = 1000):
        self.mc1.move(angle1, timeMS)
        self.mc2.move(angle2, timeMS)
        a = False
        b = False
        while not (a and b):
            wait_for_seconds(dT)
            a = a or self.mc1.update()
            b = b or self.mc2.update()
    def move1(self,angle1,timeMS = 1000):
        self.mc1.move(angle1, timeMS)
        while not self.mc1.update():
            wait_for_seconds(dT)
    def move2(self,angle2,timeMS = 1000):
        self.mc2.move(angle2, timeMS)
        while not self.mc2.update():
            wait_for_seconds(dT)
    def reset(self):
        self.mc1.reset()
        self.mc2.reset()

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

lifter = Motor('B')
lifter.set_default_speed(7)

car = Motor('D')
car.set_default_speed(20)

carResetter = primitiveHub.port.D

LIFT_IN = 67
LIFT_ALL_UP = 90
LIFT_CLEAR = 140
LIFT_HALF_DOWN = 155
LIFT_DOWN = 198
CAR_SPACING_TO_FREE_LIFT_WHILE_UP = 80
CAR_SPACING_TO_FREE_LIFT_WHILE_DOWN = 20

light_sensor = ColorSensor('E')

track = Motor('F')

aLifter = SoftMotorControl(primitiveHub.port.B, 25, False) # False for absolute rotations
aCar = SoftMotorControl(primitiveHub.port.D, 40, True)
lifterCar = DualMotorControl(aLifter,aCar)

rotator = Motor('A')
rotator.set_default_speed(37)
rotatorResetter = primitiveHub.port.A

def track_in():
    track.start_at_power(-30)
 
def track_out():
    track.start_at_power(30)

def in_cup():
    track.stop()
    lifterCar.move(LIFT_ALL_UP, 10) # Lift all the way up before moving car
    track_out()
    lifterCar.move(LIFT_IN,270) # Push minigif in
    lifterCar.move2(-280-CAR_SPACING_TO_FREE_LIFT_WHILE_UP) # Pull completely out
    lifterCar.move1(LIFT_ALL_UP) # Ensure lift is up before car moves toward cup
    lifterCar.move(LIFT_DOWN,CAR_SPACING_TO_FREE_LIFT_WHILE_UP) # Return

def out_cup():
    wait_for_seconds(3)
    track_out()
    # Prepare lift to get under the minifig:
    lifterCar.move(LIFT_ALL_UP, 10)
    lifterCar.move2(-CAR_SPACING_TO_FREE_LIFT_WHILE_UP)
    lifterCar.move1(LIFT_IN)
    # Push and pull:
    lifterCar.move2(295+CAR_SPACING_TO_FREE_LIFT_WHILE_UP-10)
    lifterCar.move(LIFT_ALL_UP,50)
    lifterCar.move2(-295 -CAR_SPACING_TO_FREE_LIFT_WHILE_UP -50)
    # Return:
    lifterCar.move(LIFT_HALF_DOWN,CAR_SPACING_TO_FREE_LIFT_WHILE_UP+60)
    lifterCar.move(LIFT_DOWN,-60)
    wait_for_seconds(3) # Ensure out

banana = 140
large = 40
def rotate(steps, power = 30):
    rotator.run_for_rotations(-steps*banana/large/4, power)

def invite():
    track_in()
    seen = 0
    while seen < 4:
        if light_sensor.get_color() == 'black' or light_sensor.get_color() == None:
            seen = 0
        else:
            seen = seen+1
    track.stop()
    track.run_for_rotations(-2, 35)

def resetRotator():
    rotator.run_for_degrees(-100)
    driveToStalled(rotatorResetter, 35)
    rotator.run_for_degrees(-29)

def reset():
    if lifter.get_position() > 185:
        lifter.run_to_position(LIFT_DOWN)
        driveToStalled(carResetter, -25)
        car.run_for_degrees(CAR_SPACING_TO_FREE_LIFT_WHILE_DOWN)
    else:
        lifter.run_to_position(LIFT_ALL_UP)
        driveToStalled(carResetter, -25)
        car.run_for_degrees(CAR_SPACING_TO_FREE_LIFT_WHILE_UP)
        lifter.run_to_position(LIFT_DOWN)
    lifterCar.reset()
    resetRotator()

#reset()
#invite()
#in_cup()
#out_cup()
#resetRotator()
#rotate(1)
#raise SystemExit

while True:
    try:
        track_out()
        reset()
        while True:
            for _ in range(4):
                rotate(1)
                out_cup()
            wait_for_seconds(10) # Cool down
            for _ in range(4):
                rotate(1)
                invite()
                in_cup()
            rotate(24, 36)
    except:
        for i in range(3):
            hub.speaker.beep(52, 0.5)
            wait_for_seconds(0.2)
        hub.speaker.beep(48, 0.5)
