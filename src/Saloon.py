from pybricks.pupdevices import Motor, InfraredSensor, ColorDistanceSensor
from pybricks.parameters import Button, Color, Direction, Port, Side, Stop
from pybricks.tools import wait

motorOuter = Motor(Port.A)
motorInner = Motor(Port.B)
sensorInner = InfraredSensor(Port.C)
sensorOuter = ColorDistanceSensor(Port.D)

SPEED_TRACK_SLOW = 60
SPEED_TRACK = 140
SPEED_TRACK_FAST = 300
TICK = 50
ANGLE_TARGET = 305
ANGLE_OVERSHOOT = 600
ANGLE_RESET_INNER_TRACK = 127

def reset():
    motorInner.run_time(SPEED_TRACK_FAST, 1500)
    motorInner.run_angle(-SPEED_TRACK_FAST, ANGLE_RESET_INNER_TRACK)

def seesFigure():
    d = sensorInner.distance()
    #print(d)
    return d < 14 or d > 50

def invite():
    motorOuter.run(SPEED_TRACK)
    figureSeen = True
    angleLastFigure = -ANGLE_OVERSHOOT
    while True: # Wait until interaction is triggered
        wait(TICK)
        if seesFigure(): # Figure seen:
            angleLastFigure = motorOuter.angle()
            if not figureSeen:
                print('Figure seen', sensorInner.distance())
                sensorOuter.light.on(Color.GREEN)
                figureSeen = True
        if not figureSeen: # Not seen figure:
            continue
        if motorOuter.angle()-angleLastFigure > ANGLE_OVERSHOOT: # Overshoot:
            print('Overshoot figure!')
            figureSeen = False
            sensorOuter.light.on(Color.RED)
            motorOuter.run(SPEED_TRACK)
            continue
        if sensorOuter.distance() < 80: # Activate
            sensorOuter.light.on(Color.RED)
            print('Activate at angle', motorOuter.angle(), sensorInner.distance())
            motorOuter.run(SPEED_TRACK)
            saw = True            
            while saw:
                saw = False
                for i in range(0, 5):
                    if seesFigure():
                        saw = True
                        angleLastFigure = motorOuter.angle()
                        break
                    wait(TICK)
            motorOuter.stop()
            angleDiff = motorOuter.angle()-angleLastFigure
            motorOuter.run_angle(SPEED_TRACK_FAST, ANGLE_TARGET-angleDiff)
            motorInner.run_time(-SPEED_TRACK_FAST, 5000)
            motorOuter.run(SPEED_TRACK)
            reset()
            return

def leave():
    print('Leave saloon')
    sensorOuter.light.on(Color.RED)
    motorOuter.run(SPEED_TRACK)
    timeLastFigure = 0
    while timeLastFigure < 3800:
        wait(TICK)
        timeLastFigure = timeLastFigure + TICK
        if seesFigure():
            print('Figure seen. Resetting countdown')
            timeLastFigure = 0
    print('Free: Visitor leaves')
    motorOuter.stop()
    motorOuter.run(SPEED_TRACK_SLOW)
    motorInner.run_time(SPEED_TRACK_FAST, 3000)
    motorOuter.run(SPEED_TRACK)
    motorInner.run_time(SPEED_TRACK_SLOW, 2000)
    motorInner.run_angle(SPEED_TRACK_FAST, -ANGLE_RESET_INNER_TRACK) # Reset

# Uncomment lines below for testing:
#reset()
#invite()
#leave()
#raise SystemExit()

reset()
while True:
    leave()
    invite()
    wait(5000) # 5 seconds to shop
