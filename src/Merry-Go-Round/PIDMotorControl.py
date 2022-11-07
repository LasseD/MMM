import hub as primitiveHub
import utime

# Source code and PID tuning by Ole Caprani:
# https://cs.au.dk/~ocaprani/legolab/Danish.dir/FLLprogrammering/SorteStreger/LeftEdgeDrive/PID%20Controller%20For%20Lego%20Mindstorms%20Robots.pdf
# Use TestMotorControl and the functions in the end to tune your own PID controller.
# The values below work well for a freely spinning M-motor
# Search for Kc => {'Kp': 0.0474, 'Kd': 0.0464706, 'Ki': 0.012087, 'angle': 400, 'timeMS': 2000, 'osc': 43, 'ovs': 0, 'err': 78.7098, 'Kc': 0.079}
Kc = 0.079 # Critical gain
dT = 0.015 # Loop time
Kp = 0.041 # 0.0474
Ki = 0.0088 # 0.012087
Kd = 0.052 # 0.0464706

MIN_POWER = 17

from spike import PrimeHub, LightMatrix, Button, StatusLight, ForceSensor, MotionSensor, Speaker, ColorSensor, App, DistanceSensor, Motor, MotorPair
from spike.control import wait_for_seconds, wait_until, Timer
from math import *

hub = PrimeHub()

class PIDMotorControl:
    def __init__(self, port):
        self.port = port
        self.reset()
    def getPosition(self):
        return self.port.motor.get()[1]
    def reset(self):
        self.pPrev = self.getPosition()
        self.tPrev = utime.ticks_ms()
        self.integral = 0
        self.lastError = 0
    def setSpeed(self,speed): # Degrees per second
        self.speed = speed
        if speed == 0:
            self.port.motor.float()
    def update(self):
        p = self.getPosition()
        t = utime.ticks_ms()
        if t == self.tPrev:
            return # No time has passed
        speed = 1000 * (p-self.pPrev) / (t-self.tPrev)
        error = self.speed - speed
        self.integral = self.integral + error
        derivative = error - self.lastError
        v = Kp*error + Ki*self.integral + Kd*derivative
        v = v+MIN_POWER if v > 0 else v-MIN_POWER
        self.port.pwm(v)
        #print('P',error,'I',self.integral,'D',derivative,'V',v)
        # Update loop variables:
        self.lastError = error
        self.tPrev = t
        self.pPrev = p

class SingleMotorControl:
    def __init__(self, mc):
        self.mc = mc
    def move(self,angle,timeMS):
        #print("turn",angle,"in",timeMS)
        self.mc.reset()
        p0 = self.mc.getPosition()
        self.mc.setSpeed(1000*angle/timeMS)
        until = utime.ticks_ms() + timeMS
        totalError = 0
        maxError = -1000
        ticks = 0
        oscilations = 0
        lastE = -1
        while utime.ticks_ms() < until:
            self.mc.update()
            wait_for_seconds(dT)
            e = self.mc.lastError
            if ticks > 0 and ((e > 0 and lastE < 0) or (e < 0 and lastE > 0)):
                oscilations = oscilations+1
            e = abs(e)
            if ticks > 10:
                totalError = totalError + e
                maxError = max(maxError,e)
            ticks = ticks + 1
            lastE = e
        self.mc.setSpeed(0)
        print('Move',angle,'in',timeMS,'Kp',Kp,'Ki',Ki,'Kd',Kd,'AVG error',totalError/ticks,"OVERSHOOT",self.mc.getPosition()-p0-angle,"Oscilations",oscilations)
        return {"err":totalError/ticks,
                "osc":oscilations,
                "ovs":self.mc.getPosition()-p0-angle,
                'Kc':Kc,
                'Kp':Kp,
                'Ki':Ki,
                'Kd':Kd,
                "angle":angle,
                "timeMS":timeMS
                }

mc = PIDMotorControl(primitiveHub.port.A)
m = SingleMotorControl(mc)

def testPID():    
    global Kd
    print("-"*30, "TEST PID")
    # Search for Kc => {'Kp': 0.0474, 'Kd': 0.0464706, 'Ki': 0.012087, 'angle': 400, 'timeMS': 2000, 'osc': 43, 'ovs': 0, 'err': 78.7098, 'Kc': 0.079}
    # {'Kp': 0.0474, 'Kd': 0.0464706, 'Ki': 0.012087, 'angle': 400, 'timeMS': 2000, 'osc': 43, 'ovs': 0, 'err': 78.7098, 'Kc': 0.079}
    # Kp: 0.041, Ki: 0.0088, Kd: 0.052
    test = "Kd"
    best = {"osc":1000, "where":[]}
    for i in range(42, 80, 1):
        v = Kd = i/10000.0
        print('#'*30,test,v)
        osc = 0
        for time in range(100, 200, 400):
            j = 2
            while j <= 64:
                ret = m.move(j, time)
                osc = osc + ret["osc"]
                j = j*2
        print("OSCILATIONS",test,v,":",osc)
        if osc < best["osc"]:
            best = {"osc":osc, "where":[v]}
        elif osc == best["osc"]:
            best["where"].append(v)
    print("BEST",best)
#testPID()
    
def tunePID(m):
    global Kc, dT, Kp, Ki, Kd
    print("-"*20, "TUNE PID CONTROLLER", "-"*20)
    speeds = ['200', '400', '800']
    tests = ["err","osc","ovs"]
    best = {}
    for test in tests:
        best[test] = {}
        for speed in speeds:
            best[test][speed] = {test: 1000000}
    for i in range(74, 91, 1):
        # Test Critical gain Kc and find the oscilation period Pc:
        Kc = i/1000.0
        print("TESTING Kc = ",Kc)
        Kp = Kc
        Ki = 0
        Kd = 0
        ret = m.move(400,2000)
        if ret["osc"] == 0:
            continue
        Pc = 2/ret["osc"]
        # Set PID according to Ziegler–Nichols method:
        Kp = Kc*0.6
        Ki = 2*Kp*dT/Pc
        Kd = Kp*Pc/(8*dT)
        print("NOTICE FLUIDITY FOR Kc = ",Kc)
        for speed in speeds:
            ret = m.move(int(speed),2000)
            for test in tests:
                b = best[test][speed]
                if abs(b[test]) > abs(ret[test]):
                    best[test][speed] = ret
    print("Done tuning PID Controller")
    for speed in speeds:
        for test in tests:
            print("Best",test,"for speed",speed,best[test][speed])
#tunePID(m)

def findMinimalMovementPower():
    print("Finding minimal motor movement power")
    i = 1
    while True:
        primitiveHub.port.A.pwm(i)
        p0 = primitiveHub.port.A.motor.get()[1]
        wait_for_seconds(0.2)
        p1 = primitiveHub.port.A.motor.get()[1]
        if p0 != p1:
            print("Minimal movement speed found:",i)
            return
        else:
            i = i+1
            print("Testing power",i)
#findMinimalMovementPower()
