from pybricks.hubs import PrimeHub
from pybricks.pupdevices import InfraredSensor, Motor, ColorDistanceSensor
from pybricks.parameters import Port, Direction, Button, Stop, Color
from pybricks.tools import wait

track = Motor(Port.E)
track.run(-140)
start = ColorDistanceSensor(Port.F)

SPEED_FAST = 500
SPEED_SLOW = 130

hub = PrimeHub()
class Lift:
    def __init__(self, motor, sensorPort):
        self.motor = motor
        self.sensor = InfraredSensor(sensorPort)
        # States:
        #  0: Not resetting/await train
        #  1: Await train to pass
        #  2: Await motor to be done
        self.state = 0
        self.capacity = 0 # How many trains still fit onto lift
    def runAngle(self, angle, speed, waitAtEnd):
        self.motor.run_angle(speed, angle, Stop.HOLD, waitAtEnd)
    def resetTrain(self):
        if self.capacity == 0:
            return # Can't fit any!
        if self.state == 0:
            if self.sensor.distance() <= 20: # Train detected:
                self.motor.run(SPEED_FAST)
                self.state = 1
        elif self.state == 1:
            if self.sensor.distance() > 20: # Train passed:
                self.state = 2
                self.motor.run_angle(SPEED_FAST, 450, Stop.COAST, False)
        else: # self.state == 2:
            if self.motor.done():
                self.state = 0
                self.capacity -= 1

liftA = Lift(Motor(Port.C, Direction.COUNTERCLOCKWISE), Port.D)
liftB = Lift(Motor(Port.A, Direction.COUNTERCLOCKWISE), Port.B)
lifts = [liftA, liftB]

def setup():
    for lift in lifts:
        lift.capacity = 1
    while any(lift.capacity > 0 for lift in lifts):
        for lift in lifts:
            lift.resetTrain()

while True:
    start.light.on(Color.GREEN) # Indicate that interaction is now possible
    cnt = 0
    while not (start.distance() < 100 or cnt > 600 or Button.RIGHT in hub.buttons.pressed()):
        if Button.LEFT in hub.buttons.pressed():
            setup()
        wait(100) # Wait for button to be pressed for a run
        cnt = cnt+1
    start.light.on(Color.RED) # Interaction disabled
    # Move trains to the top:
    lifts[1].runAngle(2400, SPEED_SLOW, waitAtEnd=False)
    lifts[0].runAngle(2800, SPEED_SLOW, waitAtEnd=True)
    wait(1000)
    # Start run:
    lifts[0].runAngle(1400, SPEED_FAST, waitAtEnd=False)
    wait(1900)
    lifts[1].runAngle(1400, SPEED_FAST, waitAtEnd=True)
    # End run:
    setup()
