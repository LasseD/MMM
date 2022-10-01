import hub as primitiveHub
from spike import PrimeHub, LightMatrix, Button, StatusLight, Speaker, ColorSensor, App, DistanceSensor, Motor
from spike.control import wait_for_seconds, wait_until, Timer
from math import *

hub = PrimeHub()

lifter = Motor('B')
lifter.set_default_speed(7)
lifterResetter = primitiveHub.port.B

car = Motor('D')
car.set_default_speed(20)
carResetter = primitiveHub.port.D

rotator = Motor('A')
rotator.set_default_speed(30)
rotatorResetter = primitiveHub.port.A

banana = 140
large = 40
def rotate(steps):
    rotator.run_for_rotations(-steps*banana/large/4, 30)

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

def resetRotator():
    rotator.run_for_degrees(-100)
    driveToStalled(rotatorResetter, 35)
    rotator.run_for_degrees(-33)

def resetLifter():
    driveToStalled(lifterResetter, 25)

def resetCar():
    driveToStalled(carResetter, -25)

def resetCarAndLifter():
    driveToStalled(lifterResetter, -25)
    resetCar()
    car.run_for_degrees(220)
    resetLifter()
    resetCar()
    car.run_for_degrees(25)
    rotate(1)
    while True:
        hub.right_button.wait_until_pressed()
        rotate(1)

resetCarAndLifter()
resetRotator()
