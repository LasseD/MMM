from pybricks.pupdevices import Motor, InfraredSensor
from pybricks.parameters import Button, Color, Direction, Port, Side, Stop
from pybricks.tools import wait, StopWatch

motorOuter = Motor(Port.A)
motorInner = Motor(Port.B)
motorLift = Motor(Port.D)
sensor = InfraredSensor(Port.C)
watch = StopWatch()

SPEED_TRACK_SLOW = 75
SPEED_TRACK = 140
SPEED_TRACK_FAST = 300
doorOpen = False

def liftDown():
    global doorOpen
    motorLift.run_until_stalled(-150, duty_limit=30)
    doorOpen = True

def closeDoor():
    global doorOpen
    if not doorOpen:
        return # Already closed
    motorLift.run_angle(250, 95, wait=False)
    doorOpen = False

def openDoor():
    global doorOpen
    if doorOpen:
        return # Already open
    motorLift.run_angle(250, -95, wait=True)
    doorOpen = True

def reset():
    motorInner.run_angle(SPEED_TRACK_FAST, 700)
    motorInner.run_angle(SPEED_TRACK_FAST, -350)
    motorOuter.run(SPEED_TRACK)
    liftDown()
    
def invite():
    motorOuter.run(SPEED_TRACK)
    while sensor.distance() > 10: # While no visitors
        wait(50)
    motorOuter.run(SPEED_TRACK_SLOW)
    while sensor.distance() < 20: # Wait until after the sensor
        wait(50)
    motorOuter.stop()
    motorOuter.run_angle(SPEED_TRACK_FAST, -30)
    openDoor()
    motorInner.run(-SPEED_TRACK_FAST)
    motorOuter.run_angle(SPEED_TRACK_SLOW, 300)
    wait(1200)
    motorInner.stop()
    motorInner.run_angle(SPEED_TRACK_FAST, 350)
    motorOuter.run(SPEED_TRACK)

def leave():
    motorOuter.run(SPEED_TRACK)
    watch.reset()
    while watch.time() < 2500:
        if sensor.distance() < 26:
            watch.reset()
        wait(100)
    motorOuter.stop()
    motorInner.run_angle(SPEED_TRACK_FAST, 1000)
    motorOuter.run(SPEED_TRACK)
    closeDoor()
    wait(1000)
    motorInner.run_angle(SPEED_TRACK_FAST, -360)

reset()
while True:
    leave()
    invite()
    motorLift.run_angle(200, 780)
    wait(2500)
    motorLift.run_angle(300, -100)
    liftDown()
