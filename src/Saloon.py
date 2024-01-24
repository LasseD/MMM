from pybricks.pupdevices import Motor, InfraredSensor, ColorDistanceSensor
from pybricks.parameters import Button, Color, Direction, Port, Side, Stop
from pybricks.tools import wait

motorOuter = Motor(Port.A)
motorInner = Motor(Port.B)
sensorInner = InfraredSensor(Port.C)
sensorOuter = ColorDistanceSensor(Port.D)

SPEED_TRACK_SLOW = 100
SPEED_TRACK = 140
SPEED_TRACK_FAST = 300
TICK = 50
ANGLE_TARGET = 274
ANGLE_OVERSHOOT = 643
ANGLE_RESET_INNER_TRACK = 165

def reset():
    motorInner.run_angle(SPEED_TRACK_FAST, 2*ANGLE_RESET_INNER_TRACK)
    motorInner.run_angle(-SPEED_TRACK_FAST, ANGLE_RESET_INNER_TRACK)

def seesFigure():
    return sensorInner.distance() < 20

def invite():
    motorOuter.run(SPEED_TRACK)
    figureSeen = True
    angleLastFigure = motorOuter.angle()-ANGLE_OVERSHOOT
    while True: # Wait until interaction is triggered
        wait(TICK)
        if seesFigure(): # Figure seen:
            angleLastFigure = motorOuter.angle()
            if not figureSeen:
                print('Figure seen', sensorInner.distance())
                motorOuter.run(SPEED_TRACK_SLOW)
                sensorOuter.light.on(Color.GREEN)
                figureSeen = True
        if not figureSeen:
            continue
        if motorOuter.angle()-angleLastFigure > ANGLE_OVERSHOOT:
            print('Overshoot figure!')
            figureSeen = False
            sensorOuter.light.on(Color.RED)
            motorOuter.run(SPEED_TRACK)
            continue
        # Figure seen, and not too late:
        if sensorOuter.distance() < 80:            
            sensorOuter.light.on(Color.RED)
            while seesFigure():
                angleLastFigure = motorOuter.angle()
                wait(TICK)
            motorOuter.stop()
            angleDiff = motorOuter.angle()-angleLastFigure
            print('Activate at angle', angleDiff)
            motorOuter.run_angle(SPEED_TRACK_FAST, -(angleDiff-ANGLE_TARGET))            
            motorInner.run_angle(-SPEED_TRACK_FAST, 1500)
            motorOuter.run(SPEED_TRACK_SLOW)
            motorInner.run_angle(-SPEED_TRACK_FAST, 1000)
            motorOuter.run(SPEED_TRACK)
            motorInner.run_angle(SPEED_TRACK_FAST, ANGLE_RESET_INNER_TRACK)
            return

def leave():
    sensorOuter.light.on(Color.RED)
    motorOuter.run(SPEED_TRACK)
    timeLastFigure = 0
    while timeLastFigure < 3500:
        wait(TICK)
        timeLastFigure = timeLastFigure + TICK
        if seesFigure():
            timeLastFigure = 0
    motorOuter.stop()
    motorInner.run_angle(SPEED_TRACK_FAST, 1000)
    motorOuter.run(SPEED_TRACK)
    wait(1000)
    motorInner.run_time(SPEED_TRACK_SLOW, 2000)
    motorInner.run_angle(-SPEED_TRACK_FAST, ANGLE_RESET_INNER_TRACK)

# Uncomment lines below for testing:
#reset()
#invite()
#leave()
#raise SystemExit()

reset()
while True:
    leave()
    invite()
    wait(10*1000)
