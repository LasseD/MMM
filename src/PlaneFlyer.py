from pybricks.hubs import InventorHub
from pybricks.pupdevices import Motor, InfraredSensor, ForceSensor
from pybricks.parameters import Button, Color, Direction, Port, Stop
from pybricks.tools import wait, StopWatch

hub = InventorHub()
watch = StopWatch()

track = Motor(Port.E, Direction.COUNTERCLOCKWISE)
innerTrack = Motor(Port.C)
lifter = Motor(Port.B)
turner = Motor(Port.F)

figureSensor = InfraredSensor(Port.A)
heightSensor = ForceSensor(Port.D)

CAPACITY = 4

# Speed:
SPEED_TURN_ADJUST = 80
SPEED_TURN_SLOW = 110
SPEED_TURN_FAST = 450
SPEED_LIFT = 230
SPEED_TRACK = 130
SPEED_INNER_TRACK = 400

# Movements:
FORCE_LOAD = 4.89
FORCE_DOOR_CLOSE = 3.35
FORCE_PIN = 2.992
FORCE_CLEAR = 1.5

TURN_DOOR = 35

# State:
guests = 0

def turn(planeAngle, speed_turner = SPEED_TURN_FAST):
    print('Turning', planeAngle)
    speed_lifter = speed_turner*12/60
    lifter.run_angle(speed_lifter, -planeAngle, Stop.HOLD, wait=False)
    turner.run_angle(speed_turner, planeAngle*60/12, Stop.HOLD, wait=False)
    while not (lifter.done() and turner.done()):
        wait(100)

def liftTo(force):
    watch.reset()
    f = heightSensor.force()
    print('lift', f, '->', force)
    #    raise SystemExit()
    if heightSensor.force() > force:
        lifter.run(-SPEED_LIFT)
        while heightSensor.force() > force:
            wait(50)
    else:
        lifter.run(SPEED_LIFT)
        while heightSensor.force() < force:
            wait(50)
            while(watch.time() > 8000):
                lifter.stop()
                print('DEAD')
                wait(100000) # Dead
    lifter.stop()

def turnToSensor():
    print('Turn to sensor')
    turner.run(SPEED_TURN_ADJUST)
    while heightSensor.force() == 0.0:
        wait(50)
    turner.stop()
    print('Turned to force', heightSensor.force())

def openDoor():
    print('Opening door')
    liftTo(FORCE_CLEAR)
    turn(-TURN_DOOR, SPEED_TURN_SLOW)
    lifter.run_angle(SPEED_LIFT, 230) # Down over pin.
    turn(TURN_DOOR, SPEED_TURN_SLOW)
    liftTo(FORCE_LOAD)

def closeDoor():
    print('Closing door')
    liftTo(FORCE_DOOR_CLOSE)
    innerTrack.run(SPEED_TRACK) # Clear stragglers
    # Close the door:
    turner.run(-SPEED_TURN_SLOW)
    while heightSensor.force() > 0.0:
        wait(50)
    turner.stop()
    lifter.run_angle(SPEED_LIFT, -450) # Clear the pin
    turnToSensor()
    turn(18, SPEED_TURN_SLOW)
    innerTrack.stop()

def init():
    global guests
    f = heightSensor.force()
    print('init', f)
    if f >= FORCE_LOAD:
        print('Already above sensor')
        guests = 0
        return
    guests = 4 # Unknown number of guests!
    if f > 0.0:
        print('Lift clear for init')
        liftTo(0.0)
        turn(50, SPEED_TURN_SLOW)
    while heightSensor.force() > 0.0:
        turn(20, SPEED_TURN_SLOW)
    turnToSensor()
    print('Adjusting under new plane', heightSensor.force())
    turn(18, SPEED_TURN_SLOW)
    liftTo(FORCE_LOAD)
    while guests > 0:
        leave()

def pickup():
    global guests
    track.run(SPEED_TRACK)
    # Wait for guest:
    ticksFree = 0
    while ticksFree <= 5: # Wait until sensor is free for some time:
        wait(100)
        if figureSensor.distance() < 19:
            ticksFree = 0
        else:
            ticksFree = ticksFree+1
    cur = figureSensor.distance()
    prev = cur
    while cur <= prev or cur > 10: # Wait until figure is close to sensor:
        prev = cur
        wait(50)
        cur = figureSensor.distance()
    
    # Get passenger into plane:
    track.stop()
    innerTrack.run(-SPEED_INNER_TRACK)
    track.run_angle(SPEED_TRACK, 200)
    innerTrack.stop()
    innerTrack.run_angle(-SPEED_INNER_TRACK, 1300)
    track.run(SPEED_TRACK)
    guests = guests + 1
    # Close the door:
    closeDoor()
    if guests < CAPACITY: # Position next plane:
        turn(90, SPEED_TURN_SLOW)
        openDoor()

def leave():
    global guests
    openDoor()
    innerTrack.run_angle(SPEED_INNER_TRACK, 400)
    timeFree = 0
    while timeFree < 3000:
        if figureSensor.distance() < 19:
            timeFree = 0 
        wait(50)
        timeFree = timeFree + 50
    innerTrack.run_angle(SPEED_INNER_TRACK, 1000)
    guests = guests - 1
    if guests > 0:
        closeDoor()
        lifter.run_angle(SPEED_LIFT, -200)
        turn(90, SPEED_TURN_SLOW)

#pickup()
#init()
#closeDoor()
#openDoor()
#raise SystemExit()

# Main loop:
print('Plane Flyer Debugging Enabled', figureSensor.distance())
track.run(SPEED_TRACK)
init()
while True:
    while guests < CAPACITY:
        pickup()
    liftTo(0.0)
    turner.control.limits(1000, 120, 560)
    lifter.control.limits(1000, 50, 560)
    turn(4 * 360, SPEED_TURN_FAST)
    turner.control.limits(1000, 2000, 560) # Read using print(turner.control.limits())
    lifter.control.limits(1000, 2000, 560)
    while guests > 0:
        leave()
