from pybricks.hubs import InventorHub
from pybricks.pupdevices import Motor, InfraredSensor
from pybricks.parameters import Port, Stop, Direction
from pybricks.tools import wait, StopWatch

SPEED_TRACK = 140
SPEED_CAR = 200
SPEED_LIFT = 130
SPEED_CUPS = 200
LIFT_IN = -22
LIFT_UP = 0
LIFT_SOMEWHAT_DOWN = 20
LIFT_DOWN = 105
CAR_SPACING_TO_FREE_LIFT_WHILE_UP = 80
CAR_SPACING_TO_FREE_LIFT_WHILE_DOWN = 40

innerTrack = Motor(Port.A)
sensor = InfraredSensor(Port.B)
outerTrack = Motor(Port.C, Direction.COUNTERCLOCKWISE)
outerTrack.run(SPEED_TRACK)
cups = Motor(Port.D)
car = Motor(Port.E)
lift = Motor(Port.F)
lift.control.limits(acceleration=260)
sensorWatch = StopWatch()

def resetRotator():
    cups.run_angle(SPEED_CUPS, -90)
    cups.run_until_stalled(SPEED_CUPS, duty_limit=30)
    cups.run_angle(SPEED_CUPS, -40)

def reset():
    if lift.angle() > 90:
        lift.run_target(SPEED_LIFT, LIFT_DOWN)
        car.run_until_stalled(-SPEED_CAR, duty_limit=40)
        car.run_angle(SPEED_CAR, CAR_SPACING_TO_FREE_LIFT_WHILE_DOWN)
    else:
        lift.run_target(SPEED_LIFT, LIFT_UP)
        car.run_until_stalled(-SPEED_CAR, duty_limit=40)
        car.run_angle(SPEED_CAR, CAR_SPACING_TO_FREE_LIFT_WHILE_UP)
        lift.run_target(SPEED_LIFT, LIFT_DOWN)
    car.reset_angle(0)
    resetRotator()

def liftCar(l, c):
    lift.run_target(SPEED_LIFT, l, wait=False)
    car.run_angle(SPEED_CAR, c, wait=False)
    while not (lift.done() and car.done()):
        wait(25)

def in_cup():
    liftCar(LIFT_UP, 10) # Lift all the way up before moving car
    liftCar(LIFT_IN, 270) # Push minifig in
    car.run_angle(SPEED_CAR, -280-CAR_SPACING_TO_FREE_LIFT_WHILE_UP) # Pull completely out
    lift.run_target(SPEED_LIFT, LIFT_UP) # Ensure lift is up before car moves toward cup
    liftCar(LIFT_DOWN, CAR_SPACING_TO_FREE_LIFT_WHILE_UP) # Return

def out_cup():
    # Prepare lift to get under the minifig:
    liftCar(LIFT_UP, 10)
    car.run_angle(SPEED_CAR, -CAR_SPACING_TO_FREE_LIFT_WHILE_UP)
    lift.run_target(SPEED_LIFT, LIFT_IN)
    # Push and pull:
    car.run_angle(SPEED_CAR, 295+CAR_SPACING_TO_FREE_LIFT_WHILE_UP-10)
    liftCar(LIFT_UP, 50)
    car.run_angle(SPEED_CAR, -295 -CAR_SPACING_TO_FREE_LIFT_WHILE_UP -50)
    # Return:
    liftCar(LIFT_SOMEWHAT_DOWN, CAR_SPACING_TO_FREE_LIFT_WHILE_UP+60)
    liftCar(LIFT_DOWN, -60)

def leave():
    innerTrack.run_angle(SPEED_TRACK, -400, wait=False)
    sensorWatch.reset()
    while not innerTrack.done() or sensorWatch.time() < 2000:
        if sensor.distance() < 16:
            sensorWatch.reset()
        wait(25)
    innerTrack.run_angle(SPEED_TRACK, -500)

def invite():
    while sensor.distance() < 12: # Ensure not to pick up someone exiting sensor
        wait(100)
    while sensor.distance() > 7:
        wait(10)
    outerTrack.stop()
    outerTrack.run_angle(SPEED_TRACK, 80)
    innerTrack.run_angle(1.5*SPEED_TRACK, 900, wait=False)
    outerTrack.run_angle(SPEED_TRACK, 100)
    while not innerTrack.done():
        wait(100)
    outerTrack.run(SPEED_TRACK)

BANANA = 140
LARGE = 40
def rotate(steps, speed = SPEED_CUPS):
    cups.run_angle(speed, -steps*BANANA/LARGE/4 * 360)

# Test methods below:
#print(lift.angle())
#reset()
#in_cup()
#out_cup()
#leave()
#invite()
#rotate(1)
#rotate(24, SPEED_CUPS)
#raise SystemExit()

print('Merry-Go-Roung Debugging Enabled', sensor.distance())
reset()
while True:
    while True:
        for _ in range(4):
            rotate(1)
            out_cup()
            leave()
        for _ in range(4):
            rotate(1)
            invite()
            in_cup()
        cups.control.limits(acceleration=80)
        rotate(24, 450)
        cups.control.limits(acceleration=800)
